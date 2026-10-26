package com.raddatz.automation.tests;

import com.microsoft.playwright.Page;
import com.raddatz.automation.config.AutomationConfig;
import com.raddatz.automation.core.PlaywrightSession;
import com.raddatz.automation.pages.DashboardPage;
import com.raddatz.automation.pages.LoginPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.ITestResult;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterSuite;

import java.lang.reflect.Method;

public abstract class ReadOnlyTestBase implements HasPage {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected AutomationConfig config;
    private DashboardPage dashboard;
    private PlaywrightSession session;

    @BeforeMethod(alwaysRun = true)
    public void startLocalReadOnlySession(Method testMethod, ITestContext testContext) {
        config = AutomationConfig.load();
        if (!config.runLocal()) {
            throw new SkipException("Local tests are disabled. Run with -Draddatz.run.local=true and environment credentials.");
        }
        config.requireBrowserExecution(requiresMutationPermission());
        if (requiresMutationPermission()) {
            config.requireMutationPermission();
            config.requireApprovedMutationScenario(getClass().getName(), testMethod.getName());
        }
        config.requireLocalCredentials();
        boolean reuseBrowser = Boolean.getBoolean("raddatz.reuse.browser")
                || Boolean.parseBoolean(testContext.getSuite().getParameter("reuseBrowser"));
        session = PlaywrightSession.start(config, reuseBrowser);
        logger.info("Launching local {} scenario against {}", requiresMutationPermission() ? "owned-data" : "read-only", config.baseUrl());
        try {
            if (session.hasRestoredAuthentication()) {
                currentPage().navigate(config.baseUrl().replaceFirst("/login$", "/dashboard"));
                currentPage().waitForURL("**/dashboard");
                dashboard = new DashboardPage(currentPage());
            } else {
                dashboard = new LoginPage(currentPage()).open(config.baseUrl()).signIn(config.username(), config.password());
                session.rememberAuthenticatedSession();
            }
        } catch (RuntimeException | Error failure) {
            try {
                session.saveFailureTrace(config.artifactDirectory().resolve("traces")
                        .resolve(getClass().getSimpleName() + "-session-setup-" + System.nanoTime() + ".zip"));
            } catch (RuntimeException traceFailure) {
                failure.addSuppressed(traceFailure);
            }
            throw failure;
        }
    }

    /** Authentication is a fixture; no test is dedicated to validating a user-login journey. */
    protected DashboardPage dashboard() {
        return dashboard;
    }

    protected boolean requiresMutationPermission() {
        return false;
    }

    @Override
    public Page currentPage() {
        if (session == null) {
            return null;
        }
        return session.page();
    }

    @AfterMethod(alwaysRun = true)
    public void closeLocalReadOnlySession(ITestResult result) {
        if (session != null) {
            if (result.getStatus() == ITestResult.SUCCESS) {
                session.rememberAuthenticatedSession();
            }
            if (result.getStatus() == ITestResult.FAILURE) {
                var tracePath = config.artifactDirectory()
                        .resolve("traces")
                        .resolve(safeFileName(result) + ".zip");
                session.saveFailureTrace(tracePath);
                logger.error("Failure trace saved to {}", tracePath.toAbsolutePath());
            }
            session.close();
            session = null;
            dashboard = null;
        }
    }

    private String safeFileName(ITestResult result) {
        return (getClass().getSimpleName() + "-" + result.getMethod().getMethodName())
                .replaceAll("[^A-Za-z0-9._-]", "_");
    }

    @AfterSuite(alwaysRun = true)
    public void closeSuiteBrowser() {
        PlaywrightSession.closeSharedBrowser();
        com.raddatz.automation.data.OwnedRecordIds.clear();
    }
}
