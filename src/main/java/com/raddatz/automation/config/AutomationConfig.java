package com.raddatz.automation.config;

import java.nio.file.Path;
import java.util.Locale;

/** Reads runtime settings without ever persisting credentials. */
public record AutomationConfig(
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
}
