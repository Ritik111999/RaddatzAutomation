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
import java.util.regex.Pattern;

public abstract class ReadOnlyTestBase implements HasPage {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected AutomationConfig config;
    private static final ThreadLocal<DashboardPage> CURRENT_DASHBOARD = new ThreadLocal<>();
    private static final ThreadLocal<PlaywrightSession> CURRENT_SESSION = new ThreadLocal<>();

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
        boolean reuseBrowser = Boolean.parseBoolean(System.getProperty("raddatz.reuse.browser", "true"))
                || Boolean.parseBoolean(testContext.getSuite().getParameter("reuseBrowser"));
        PlaywrightSession session = PlaywrightSession.start(config, reuseBrowser);
        CURRENT_SESSION.set(session);
        logger.info("Launching local {} scenario against {} on thread [{}]",
                requiresMutationPermission() ? "owned-data" : "read-only", config.baseUrl(), Thread.currentThread().getName());
        try {
            DashboardPage dashboard;
            if (session.hasRestoredAuthentication()) {
                try {
                    currentPage().navigate(config.baseUrl().replaceFirst("/login$", "/dashboard"),
                            new Page.NavigateOptions().setTimeout(15000).setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
                    currentPage().waitForURL(Pattern.compile(".*/dashboard.*"), new Page.WaitForURLOptions().setTimeout(10000));
                    dashboard = new DashboardPage(currentPage());
                } catch (Exception expiredSession) {
                    logger.info("Restored session expired, timed out, or redirected; performing fresh authentication");
                    PlaywrightSession.clearAuthenticatedState();
                    try {
                        dashboard = new LoginPage(currentPage()).open(config.baseUrl()).signIn(config.username(), config.password());
                    } catch (Exception retry) {
                        logger.warn("First authentication attempt delayed; retrying: {}", retry.getMessage());
                        currentPage().waitForTimeout(2000);
                        dashboard = new LoginPage(currentPage()).open(config.baseUrl()).signIn(config.username(), config.password());
                    }
                    session.rememberAuthenticatedSession();
                }
            } else {
                try {
                    dashboard = new LoginPage(currentPage()).open(config.baseUrl()).signIn(config.username(), config.password());
                } catch (Exception retry) {
                    logger.warn("First authentication attempt delayed; retrying: {}", retry.getMessage());
                    currentPage().waitForTimeout(2000);
                    dashboard = new LoginPage(currentPage()).open(config.baseUrl()).signIn(config.username(), config.password());
                }
                session.rememberAuthenticatedSession();
            }
            CURRENT_DASHBOARD.set(dashboard);
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
        return CURRENT_DASHBOARD.get();
    }

    protected boolean requiresMutationPermission() {
        return false;
    }

    @Override
    public Page currentPage() {
        PlaywrightSession session = CURRENT_SESSION.get();
        if (session == null) {
            return null;
        }
        return session.page();
    }

    @AfterMethod(alwaysRun = true)
    public void closeLocalReadOnlySession(ITestResult result) {
        PlaywrightSession session = CURRENT_SESSION.get();
        if (session != null) {
            try {
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
            } finally {
                session.close();
                CURRENT_SESSION.remove();
                CURRENT_DASHBOARD.remove();
            }
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
