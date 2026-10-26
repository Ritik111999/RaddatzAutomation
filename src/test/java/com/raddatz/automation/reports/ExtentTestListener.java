package com.raddatz.automation.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Page;
import com.raddatz.automation.config.AutomationConfig;
import com.raddatz.automation.tests.HasPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ExtentTestListener implements ITestListener, IExecutionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtentTestListener.class);
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private AutomationConfig config;
    private ExtentReportManager.ReportRun reportRun;
    private final List<RaddatzQaPdfReport.Scenario> scenarios = new CopyOnWriteArrayList<>();
    private Instant started;

    @Override
    public void onExecutionStart() {
        config = AutomationConfig.load();
        reportRun = ExtentReportManager.create(config);
        started = Instant.now();
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = reportRun.extent().createTest(result.getMethod().getMethodName(), result.getMethod().getDescription());
        CURRENT_TEST.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        CURRENT_TEST.get().pass("Passed");
        record(result, "PASS");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        CURRENT_TEST.get().skip(result.getThrowable());
        record(result, "SKIP");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        CURRENT_TEST.get().fail(result.getThrowable());
        record(result, "FAIL");
        attachScreenshot(result);
    }

    @Override
    public void onExecutionFinish() {
        reportRun.extent().flush();
        if (config.generatePdf()) {
            PdfReportExporter.export(reportRun.htmlFile(), reportRun.directory().resolve("extent-report.pdf"));
            RaddatzQaPdfReport.write(config, reportRun.directory(), started, Instant.now(), scenarios);
        }
        LOGGER.info("Extent report written to {}", reportRun.htmlFile().toAbsolutePath());
    }

    private void record(ITestResult result, String status) {
        String description = result.getMethod().getDescription();
        scenarios.add(new RaddatzQaPdfReport.Scenario(result.getMethod().getMethodName(),
                description == null ? "" : description,
                status,
                Duration.ofMillis(Math.max(0, result.getEndMillis() - result.getStartMillis())),
                result.getThrowable() == null ? "None recorded" : result.getThrowable().getClass().getSimpleName()
                        + " (cause requires triage; confidential exception content omitted)"));
    }

    private void attachScreenshot(ITestResult result) {
        Object instance = result.getInstance();
        if (!(instance instanceof HasPage hasPage) || hasPage.currentPage() == null) {
            return;
        }
        try {
            Path screenshot = reportRun.directory().resolve(result.getMethod().getMethodName() + "-failure.png");
            Page.ScreenshotOptions options = new Page.ScreenshotOptions().setPath(screenshot).setFullPage(true);
            hasPage.currentPage().screenshot(options);
            if (Files.exists(screenshot)) {
                CURRENT_TEST.get().addScreenCaptureFromPath(screenshot.toString());
            }
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to capture a failure screenshot", exception);
        }
    }
}
