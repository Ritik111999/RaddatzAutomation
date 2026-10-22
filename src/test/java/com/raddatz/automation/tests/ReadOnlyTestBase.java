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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class ReadOnlyTestBase implements HasPage {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected AutomationConfig config;
    private PlaywrightSession session;

    @BeforeMethod(alwaysRun = true)
    public void startLocalReadOnlySession() {
        config = AutomationConfig.load();
        if (!config.runLocal()) {
            throw new SkipException("Local tests are disabled. Run with -Draddatz.run.local=true and environment credentials.");
        }
        config.requireLocalCredentials();
        session = PlaywrightSession.start(config);
        logger.info("Launching local read-only test against {}", config.baseUrl());
    }

    protected DashboardPage signIn() {
        return new LoginPage(currentPage()).open(config.baseUrl()).signIn(config.username(), config.password());
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
            if (result.getStatus() == ITestResult.FAILURE) {
                var tracePath = config.artifactDirectory()
                        .resolve("traces")
                        .resolve(safeFileName(result) + ".zip");
                session.saveFailureTrace(tracePath);
                logger.error("Failure trace saved to {}", tracePath.toAbsolutePath());
            }
            session.close();
            session = null;
        }
    }

    private String safeFileName(ITestResult result) {
        return (getClass().getSimpleName() + "-" + result.getMethod().getMethodName())
                .replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
