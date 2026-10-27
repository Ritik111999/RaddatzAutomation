package com.raddatz.automation.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Page;
import com.raddatz.automation.config.AutomationConfig;
import com.raddatz.automation.core.reporting.TestMetadata;
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
        TestMetadata meta = extractMetadata(result);
        String testName = meta != null ? "[" + meta.scenarioId() + "] " + meta.title() : result.getMethod().getMethodName();
        String description = meta != null ? meta.expectedResult() : result.getMethod().getDescription();
        ExtentTest test = reportRun.extent().createTest(testName, description);
        if (meta != null) {
            test.assignCategory(meta.module());
            test.info("<b>Type:</b> " + meta.testType().label());
            if (!meta.preconditions().isBlank()) {
                test.info("<b>Preconditions:</b> " + meta.preconditions());
            }
            if (meta.testSteps().length > 0) {
                StringBuilder stepsHtml = new StringBuilder("<b>Test Steps:</b><ol>");
                for (String step : meta.testSteps()) {
                    stepsHtml.append("<li>").append(step).append("</li>");
                }
                stepsHtml.append("</ol>");
                test.info(stepsHtml.toString());
            }
            test.info("<b>Expected Result:</b> " + meta.expectedResult());
        }
        CURRENT_TEST.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.pass("Passed");
        }
        record(result, "PASS");
        CURRENT_TEST.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        if (test == null && reportRun != null) {
            TestMetadata meta = extractMetadata(result);
            String testName = meta != null ? "[" + meta.scenarioId() + "] " + meta.title() : result.getMethod().getMethodName();
            test = reportRun.extent().createTest(testName, result.getMethod().getDescription());
        }
        if (test != null) {
            test.skip(result.getThrowable());
        }
        record(result, "SKIP");
        CURRENT_TEST.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        if (test == null && reportRun != null) {
            TestMetadata meta = extractMetadata(result);
            String testName = meta != null ? "[" + meta.scenarioId() + "] " + meta.title() : result.getMethod().getMethodName();
            test = reportRun.extent().createTest(testName, result.getMethod().getDescription());
            CURRENT_TEST.set(test);
        }
        if (test != null) {
            TestMetadata meta = extractMetadata(result);
            if (meta != null && meta.reportDefectOnFailure()) {
                test.info("<span style='color:red;font-weight:bold;'>Defect Severity: " + meta.defectSeverity().label() + "</span>");
            }
            test.fail(result.getThrowable());
        }
        record(result, "FAIL");
        attachScreenshot(result);
        CURRENT_TEST.remove();
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
        TestMetadata meta = extractMetadata(result);
        scenarios.add(new RaddatzQaPdfReport.Scenario(result.getMethod().getMethodName(),
                description == null ? "" : description,
                status,
                Duration.ofMillis(Math.max(0, result.getEndMillis() - result.getStartMillis())),
                result.getThrowable() == null ? "None recorded" : result.getThrowable().getClass().getSimpleName()
                        + " (cause requires triage; confidential exception content omitted)",
                meta));
    }

    private TestMetadata extractMetadata(ITestResult result) {
        if (result == null || result.getMethod() == null || result.getMethod().getConstructorOrMethod() == null) {
            return null;
        }
        return result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(TestMetadata.class);
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
