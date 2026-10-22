package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;

import java.util.regex.Pattern;

/** Shared application navigation. Do not add destructive actions here. */
public final class Sidebar extends BasePage {
    public Sidebar(Page page) {
        super(page);
    }

    public DashboardPage goToDashboard() {
        link("Dashboard").click();
        page.waitForURL("**/dashboard");
        return new DashboardPage(page);
    }

    public ClientMatterListPage goToClients() {
        link("Clients").click();
        page.waitForURL("**/client-and-matter");
        return new ClientMatterListPage(page);
    }

    public TodoPage goToTodos() {
        link("To-dos").click();
        page.waitForURL("**/todos/all");
        return new TodoPage(page);
    }

    public CalendarPage goToCalendar() {
        link("Calendar").click();
        page.waitForURL(Pattern.compile(".*/calendar/view-full-calendar.*"));
        return new CalendarPage(page);
    }
}
