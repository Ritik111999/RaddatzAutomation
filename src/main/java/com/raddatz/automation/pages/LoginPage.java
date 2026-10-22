package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;

public final class LoginPage extends BasePage {
    public LoginPage(Page page) {
        super(page);
    }

    public LoginPage open(String baseUrl) {
        page.navigate(baseUrl);
        assertVisible(page.getByLabel("Email address", new Page.GetByLabelOptions().setExact(true)));
        return this;
    }

    public DashboardPage signIn(String username, String password) {
        page.getByLabel("Email address", new Page.GetByLabelOptions().setExact(true)).fill(username);
        page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true)).fill(password);
        button("Sign In").click();
        page.waitForURL("**/dashboard");
        return new DashboardPage(page);
    }
}
