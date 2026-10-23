package com.raddatz.automation.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.raddatz.automation.config.AutomationConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

/** Owns one isolated browser context per TestNG test method. */
public final class PlaywrightSession implements AutoCloseable {
    private static Playwright sharedPlaywright;
    private static Browser sharedBrowser;
    private static Thread sharedOwner;
    private static String authenticatedState;
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
        return start(config, Boolean.getBoolean("raddatz.reuse.browser"));
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
        BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 1000));
        PlaywrightSession session = new PlaywrightSession(playwright, browser, context, context.newPage());
        session.startTracing();
        return session;
    }

    /** One browser for a serial suite, with a fresh context and trace for every scenario. */
    private static synchronized PlaywrightSession startShared(AutomationConfig config) {
        if (sharedOwner != null && sharedOwner != Thread.currentThread()) {
            throw new IllegalStateException("Shared-browser suites must run on one TestNG thread.");
        }
        if (sharedBrowser == null) {
            sharedOwner = Thread.currentThread();
            sharedPlaywright = Playwright.create();
            BrowserType type = switch (config.browser()) {
                case "firefox" -> sharedPlaywright.firefox();
                case "webkit" -> sharedPlaywright.webkit();
                case "chromium" -> sharedPlaywright.chromium();
                default -> throw new IllegalArgumentException("Unsupported browser: " + config.browser());
            };
            sharedBrowser = type.launch(new BrowserType.LaunchOptions().setHeadless(config.headless()));
        }
        Browser.NewContextOptions options = new Browser.NewContextOptions().setViewportSize(1440, 1000);
        if (authenticatedState != null) options.setStorageState(authenticatedState);
        BrowserContext context = sharedBrowser.newContext(options);
        PlaywrightSession session = new PlaywrightSession(sharedPlaywright, sharedBrowser, context, context.newPage());
        session.shared = true;
        session.restoredAuthentication = authenticatedState != null;
        session.startTracing();
        return session;
    }

    public static synchronized void closeSharedBrowser() {
        try {
            if (sharedBrowser != null) sharedBrowser.close();
        } finally {
            sharedBrowser = null;
            if (sharedPlaywright != null) sharedPlaywright.close();
            sharedPlaywright = null;
            sharedOwner = null;
            authenticatedState = null;
        }
    }

    public boolean hasRestoredAuthentication() {
        return restoredAuthentication;
    }

    /** Credentials/session state remain in memory only and are discarded at suite teardown. */
    public void rememberAuthenticatedSession() {
        if (shared) authenticatedState = context.storageState();
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
            context.tracing().stop();
            tracing = false;
        }
        context.close();
        if (!shared) {
            browser.close();
            playwright.close();
        }
    }
}
