package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;

public final class LoginPage extends BasePage {
    public LoginPage(Page page) {
        super(page);
    }

    public LoginPage open(String baseUrl) {
        page.navigate(baseUrl, new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
        assertVisible(page.getByLabel("Email address", new Page.GetByLabelOptions().setExact(true)));
        return this;
    }

    public DashboardPage signIn(String username, String password) {
        page.getByLabel("Email address", new Page.GetByLabelOptions().setExact(true)).fill(username);
        page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true)).fill(password);
        button("Sign In").click();
        page.waitForTimeout(800);
        if (page.locator(".auth-feedback-error, [role='alert']").isVisible()) {
            String errorMsg = page.locator(".auth-feedback-error, [role='alert']").innerText().replaceAll("\\s+", " ").trim();
            throw new IllegalStateException("Login rejected by server: " + errorMsg);
        }
        if (page.locator("text='Too many attempts'").isVisible() || page.locator("text='Unable to complete your request'").isVisible()) {
            throw new IllegalStateException("Login throttled by server: Too many login attempts in a short period. Please wait 60 seconds.");
        }
        page.waitForURL("**/dashboard");
        return new DashboardPage(page);
    }
}
