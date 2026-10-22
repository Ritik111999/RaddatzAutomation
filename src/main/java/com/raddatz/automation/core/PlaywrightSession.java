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
    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private boolean tracing;

    private PlaywrightSession(Playwright playwright, Browser browser, BrowserContext context, Page page) {
        this.playwright = playwright;
        this.browser = browser;
        this.context = context;
        this.page = page;
    }

    public static PlaywrightSession start(AutomationConfig config) {
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
        browser.close();
        playwright.close();
    }
}
