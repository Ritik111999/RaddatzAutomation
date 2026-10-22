package com.raddatz.automation.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.raddatz.automation.config.AutomationConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ExtentReportManager {
    private static final DateTimeFormatter RUN_ID = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ExtentReportManager() {
    }

    public static ReportRun create(AutomationConfig config) {
        try {
            Path directory = config.artifactDirectory().resolve("run_" + RUN_ID.format(LocalDateTime.now()));
            Files.createDirectories(directory);
            Path html = directory.resolve("extent-report.html");
            ExtentSparkReporter spark = new ExtentSparkReporter(html.toString());
            spark.config().setDocumentTitle("Raddatz PWA Automation");
            spark.config().setReportName("Read-only Playwright/TestNG execution");
            ExtentReports extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Browser", config.browser());
            extent.setSystemInfo("Headless", Boolean.toString(config.headless()));
            return new ReportRun(extent, directory, html);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create report directory", exception);
        }
    }

    public record ReportRun(ExtentReports extent, Path directory, Path htmlFile) {
    }
}
