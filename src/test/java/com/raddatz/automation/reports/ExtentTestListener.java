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

public final class ExtentTestListener implements ITestListener, IExecutionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtentTestListener.class);
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private AutomationConfig config;
    private ExtentReportManager.ReportRun reportRun;

    @Override
    public void onExecutionStart() {
        config = AutomationConfig.load();
        reportRun = ExtentReportManager.create(config);
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = reportRun.extent().createTest(result.getMethod().getMethodName(), result.getMethod().getDescription());
        CURRENT_TEST.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        CURRENT_TEST.get().pass("Passed");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        CURRENT_TEST.get().skip(result.getThrowable());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        CURRENT_TEST.get().fail(result.getThrowable());
        attachScreenshot(result);
    }

    @Override
    public void onExecutionFinish() {
        reportRun.extent().flush();
        if (config.generatePdf()) {
            PdfReportExporter.export(reportRun.htmlFile(), reportRun.directory().resolve("extent-report.pdf"));
        }
        LOGGER.info("Extent report written to {}", reportRun.htmlFile().toAbsolutePath());
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
