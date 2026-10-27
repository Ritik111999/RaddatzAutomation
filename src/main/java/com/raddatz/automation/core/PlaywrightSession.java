package com.raddatz.automation.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.raddatz.automation.config.AutomationConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Owns one isolated browser context per TestNG test method.
 * Supports per-thread browser ownership for fast, thread-safe parallel test execution.
 */
public final class PlaywrightSession implements AutoCloseable {
    private static final ThreadLocal<Playwright> THREAD_PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> THREAD_BROWSER = new ThreadLocal<>();
    private static final List<Playwright> ALL_PLAYWRIGHTS = new CopyOnWriteArrayList<>();
    private static final List<Browser> ALL_BROWSERS = new CopyOnWriteArrayList<>();
    private static volatile String authenticatedState;

    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private boolean tracing;
    private boolean shared;
    private boolean restoredAuthentication;

    private PlaywrightSession(Playwright playwright, Browser browser, BrowserContext context, Page page) {
        this.playwright = playwright;
        this.browser = browser;
        this.context = context;
        this.page = page;
    }

    public static PlaywrightSession start(AutomationConfig config) {
        return start(config, Boolean.parseBoolean(System.getProperty("raddatz.reuse.browser", "true")));
    }

    public static PlaywrightSession start(AutomationConfig config, boolean reuseBrowser) {
        if (reuseBrowser) {
            return startShared(config);
        }
        Playwright playwright = Playwright.create();
        BrowserType browserType = switch (config.browser()) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            case "chromium" -> playwright.chromium();
            default -> throw new IllegalArgumentException("Unsupported raddatz.browser: " + config.browser());
        };
        Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(config.headless()));
        Browser.NewContextOptions options = new Browser.NewContextOptions().setViewportSize(1440, 1000);
        String state = authenticatedState;
        if (state != null) {
            options.setStorageState(state);
        }
        BrowserContext context = browser.newContext(options);
        PlaywrightSession session = new PlaywrightSession(playwright, browser, context, context.newPage());
        session.restoredAuthentication = (state != null);
        session.startTracing();
        return session;
    }

    /** One browser per thread in parallel suites, with a fresh context and trace for every scenario. */
    private static PlaywrightSession startShared(AutomationConfig config) {
        Playwright playwright = THREAD_PLAYWRIGHT.get();
        Browser browser = THREAD_BROWSER.get();
        if (browser == null) {
            playwright = Playwright.create();
            BrowserType type = switch (config.browser()) {
                case "firefox" -> playwright.firefox();
                case "webkit" -> playwright.webkit();
                case "chromium" -> playwright.chromium();
                default -> throw new IllegalArgumentException("Unsupported browser: " + config.browser());
            };
            browser = type.launch(new BrowserType.LaunchOptions().setHeadless(config.headless()));
            THREAD_PLAYWRIGHT.set(playwright);
            THREAD_BROWSER.set(browser);
            ALL_PLAYWRIGHTS.add(playwright);
            ALL_BROWSERS.add(browser);
        }
        Browser.NewContextOptions options = new Browser.NewContextOptions().setViewportSize(1440, 1000);
        String state = authenticatedState;
        if (state != null) {
            options.setStorageState(state);
        }
        BrowserContext context = browser.newContext(options);
        PlaywrightSession session = new PlaywrightSession(playwright, browser, context, context.newPage());
        session.shared = true;
        session.restoredAuthentication = (state != null);
        session.startTracing();
        return session;
    }

    public static synchronized void closeSharedBrowser() {
        for (Browser browser : ALL_BROWSERS) {
            try {
                browser.close();
            } catch (Exception ignored) { }
        }
        for (Playwright pw : ALL_PLAYWRIGHTS) {
            try {
                pw.close();
            } catch (Exception ignored) { }
        }
        ALL_BROWSERS.clear();
        ALL_PLAYWRIGHTS.clear();
        THREAD_BROWSER.remove();
        THREAD_PLAYWRIGHT.remove();
        authenticatedState = null;
    }

    public boolean hasRestoredAuthentication() {
        return restoredAuthentication;
    }

    /** Credentials/session state remain in memory only and are shared across worker threads to avoid rate limits. */
    public static void clearAuthenticatedState() {
        authenticatedState = null;
    }

    public void rememberAuthenticatedSession() {
        try {
            authenticatedState = context.storageState();
        } catch (Exception ignored) { }
    }

    public Page page() {
        return page;
    }

    /**
     * Saves a Playwright trace only when a test fails. The trace includes actions,
     * DOM snapshots, screenshots, and source references for reproducible diagnosis.
     */
    public void saveFailureTrace(Path tracePath) {
        if (!tracing) {
            return;
        }
        try {
            Files.createDirectories(tracePath.getParent());
            context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
            tracing = false;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create trace directory: " + tracePath.getParent(), exception);
        }
    }

    private void startTracing() {
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        tracing = true;
    }

    @Override
    public void close() {
        if (tracing) {
            try {
                context.tracing().stop();
            } catch (Exception ignored) { }
            tracing = false;
        }
        try {
            context.close();
        } catch (Exception ignored) { }
        if (!shared) {
            try {
                browser.close();
            } catch (Exception ignored) { }
            try {
                playwright.close();
            } catch (Exception ignored) { }
        }
    }
}
