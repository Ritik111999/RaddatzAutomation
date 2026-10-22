package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;

import java.util.regex.Pattern;

public final class DashboardPage extends BasePage {
    public DashboardPage(Page page) {
        super(page);
    }

    public DashboardPage assertLoaded() {
        assertVisible(heading(Pattern.compile("Welcome back, .+!")));
        assertVisible(heading("My Tasks"));
        assertVisible(link("Calendar"));
        return this;
    }
}
