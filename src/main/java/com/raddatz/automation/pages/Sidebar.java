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

    public WorkflowPage goToWorkflows() {
        navigationLink("Workflows").click();
        page.waitForURL("**/workflow");
        return new WorkflowPage(page).assertLoaded();
    }

    public ClientPortalSpreadsheetsPage goToClientPortalSpreadsheets() {
        page.locator("[data-module='client-portal'] .nav-tab-dropdown-toggle").click();
        page.locator("a[href*='/spreadsheets']").click();
        page.waitForURL("**/spreadsheets");
        return new ClientPortalSpreadsheetsPage(page).assertLoaded();
    }

    public ClientPortalDocumentsPage goToClientPortalDocuments() {
        page.locator("[data-module='client-portal'] .nav-tab-dropdown-toggle").click();
        page.locator("a[href*='/documents']").click();
        page.waitForURL("**/documents");
        return new ClientPortalDocumentsPage(page).assertLoaded();
    }

    private Locator navigationLink(String name) {
        return page.locator("#navbarTabs").getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(name).setExact(true));
    }
}
