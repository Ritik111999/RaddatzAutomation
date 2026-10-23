package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;

import java.util.regex.Pattern;

/** Shared application navigation. Do not add destructive actions here. */
public final class Sidebar extends BasePage {
    public Sidebar(Page page) {
        super(page);
    }

    public DashboardPage goToDashboard() {
        navigationLink("Dashboard").click();
        page.waitForURL("**/dashboard");
        return new DashboardPage(page);
    }

    public ClientMatterListPage goToClients() {
        navigationLink("Clients").click();
        page.waitForURL("**/client-and-matter");
        return new ClientMatterListPage(page);
    }

    public TodoPage goToTodos() {
        navigationLink("To-dos").click();
        page.waitForURL("**/todos/all");
        return new TodoPage(page);
    }

    public CourtUpdatesPage goToCourtUpdates() {
        navigationLink("Court Updates").click();
        page.waitForURL("**/court/updates");
        return new CourtUpdatesPage(page).assertLoaded();
    }

    public CalendarPage goToCalendar() {
        navigationLink("Calendar").click();
        page.waitForURL(Pattern.compile(".*/calendar/view-full-calendar.*"));
        return new CalendarPage(page);
    }

    private Locator navigationLink(String name) {
        return page.locator("#navbarTabs").getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(name).setExact(true));
    }
}
