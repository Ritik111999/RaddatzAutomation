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
        boolean runLive,
        boolean generatePdf,
        Path artifactDirectory) {

    public static AutomationConfig load() {
        return new AutomationConfig(
                value("raddatz.base.url", "RADDATZ_BASE_URL", "https://app.raddatz-law.com"),
                value("raddatz.username", "RADDATZ_USERNAME", ""),
                value("raddatz.password", "RADDATZ_PASSWORD", ""),
                value("raddatz.browser", "RADDATZ_BROWSER", "chromium").toLowerCase(Locale.ROOT),
                booleanValue("raddatz.headless", "RADDATZ_HEADLESS", true),
                booleanValue("raddatz.run.live", "RADDATZ_RUN_LIVE", false),
                booleanValue("raddatz.report.pdf", "RADDATZ_REPORT_PDF", true),
                Path.of(value("raddatz.artifacts.dir", "RADDATZ_ARTIFACTS_DIR", "target/automation-artifacts")));
    }

    public void requireLiveCredentials() {
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Set RADDATZ_USERNAME and RADDATZ_PASSWORD before enabling live tests.");
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
