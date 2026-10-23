package com.raddatz.automation.config;

import java.nio.file.Path;
import java.util.Locale;

/** Reads runtime settings without ever persisting credentials. */
public record AutomationConfig(
        ExecutionMode executionMode,
        String baseUrl,
        String username,
        String password,
        String browser,
        boolean headless,
        boolean runLocal,
        boolean allowMutation,
        boolean generatePdf,
        Path artifactDirectory) {

    public static AutomationConfig load() {
        return new AutomationConfig(
                ExecutionMode.from(value("raddatz.execution.mode", "RADDATZ_EXECUTION_MODE", "DOCS_ONLY")),
                value("raddatz.base.url", "RADDATZ_BASE_URL", "http://192.168.1.12:8000/login"),
                value("raddatz.username", "RADDATZ_USERNAME", ""),
                value("raddatz.password", "RADDATZ_PASSWORD", ""),
                value("raddatz.browser", "RADDATZ_BROWSER", "chromium").toLowerCase(Locale.ROOT),
                booleanValue("raddatz.headless", "RADDATZ_HEADLESS", true),
                booleanValue("raddatz.run.local", "RADDATZ_RUN_LOCAL", false),
                booleanValue("raddatz.allow.mutation", "RADDATZ_ALLOW_MUTATION", false),
                booleanValue("raddatz.report.pdf", "RADDATZ_REPORT_PDF", true),
                Path.of(value("raddatz.artifacts.dir", "RADDATZ_ARTIFACTS_DIR", "target/automation-artifacts")));
    }

    public void requireLocalCredentials() {
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Set RADDATZ_USERNAME and RADDATZ_PASSWORD before enabling local tests.");
        }
    }

    public void requireBrowserExecution(boolean mutation) {
        if (executionMode == ExecutionMode.DOCS_ONLY) {
            throw new IllegalStateException("Browser execution is disabled in DOCS_ONLY mode. Set an explicit approved execution mode.");
        }
        if (mutation && executionMode != ExecutionMode.NONPROD_APPROVED) {
            throw new IllegalStateException("Mutations require NONPROD_APPROVED mode and an approved non-production target.");
        }
        if (!baseUrl.startsWith("http://192.168.1.12:8000/")) {
            throw new IllegalStateException("The configured target is not an approved Raddatz local origin.");
        }
    }

    public void requireMutationPermission() {
        if (!allowMutation) {
            throw new IllegalStateException(
                    "Data-changing admin scenarios are disabled. Set -Draddatz.allow.mutation=true only for owned test data.");
        }
    }

    private static String value(String property, String environment, String fallback) {
        String systemValue = System.getProperty(property);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }
        String environmentValue = System.getenv(environment);
        return environmentValue == null || environmentValue.isBlank() ? fallback : environmentValue.trim();
    }

    private static boolean booleanValue(String property, String environment, boolean fallback) {
        return Boolean.parseBoolean(value(property, environment, Boolean.toString(fallback)));
    }

    public enum ExecutionMode {
        DOCS_ONLY,
        PROD_OBSERVE,
        NONPROD_APPROVED;

        static ExecutionMode from(String value) {
            try {
                return ExecutionMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("Unknown RADDATZ_EXECUTION_MODE. Use DOCS_ONLY, PROD_OBSERVE, or NONPROD_APPROVED.", exception);
            }
        }
    }
}
