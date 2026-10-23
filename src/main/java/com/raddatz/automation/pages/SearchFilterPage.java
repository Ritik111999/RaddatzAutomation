package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Read-only search, filtering, workflow, and settings observation surface.
 *
 * <p>This page object intentionally has no record, report, export, or configuration mutation methods.
 * It confines its workflow inspection to visible contracts and the unsaved creation form; it never
 * selects a trigger or action, toggles a workflow, or saves a workflow.</p>
 */
public final class SearchFilterPage extends BasePage {
    private static final String CLIENT_SEARCH_PLACEHOLDER = "Search clients, cases, or tenants...";
    private static final String CALENDAR_SEARCH_PLACEHOLDER = "Search events, cases, people...";

    public SearchFilterPage(Page page) {
        super(page);
    }

    public SearchFilterPage goToClients() {
        navigationLink("Clients").click();
        page.waitForURL("**/client-and-matter");
        assertVisible(heading("Companies & Matters"));
        return this;
    }

    public SearchFilterPage assertClientSearchNoMatchThenClear(String query) {
        Locator search = page.getByPlaceholder(CLIENT_SEARCH_PLACEHOLDER,
                new Page.GetByPlaceholderOptions().setExact(true));
        search.fill(query);
        assertVisible(page.getByText("0 cases", new Page.GetByTextOptions().setExact(true)));
        search.fill("");
        assertThat(search).hasValue("");
        assertThat(page.getByText("0 cases", new Page.GetByTextOptions().setExact(true))).not().isVisible();
        return this;
    }

    public SearchFilterPage goToTodos() {
        navigationLink("To-dos").click();
        page.waitForURL("**/todos/all");
        assertVisible(heading("All Tasks"));
        return this;
    }

    public SearchFilterPage assertTodoFilterContracts() {
        buttonMatching(".*Filters.*").click();
        assertVisible(page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Priority").setExact(true)));
        assertVisible(page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Status").setExact(true)));
        assertVisible(page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Assignee").setExact(true)));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Due From").setExact(true)));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Due To").setExact(true)));
        assertVisible(linkMatching(".*Reset.*"));
        return this;
    }

    public SearchFilterPage assertTodoSearchNoMatchThenReset(String query) {
        Locator search = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Search tasks").setExact(true));
        search.fill(query);
        assertVisible(heading("No tasks found"));
        linkMatching(".*Reset.*").click();
        page.waitForURL("**/todos/all");
        assertThat(search).hasValue("");
        assertThat(heading("No tasks found")).not().isVisible();
        return this;
    }

    public SearchFilterPage goToCalendar() {
        navigationLink("Calendar").click();
        page.waitForURL(Pattern.compile(".*/calendar/view-full-calendar\\?view=week"));
        assertVisible(heading("Calendar"));
        return this;
    }

    public SearchFilterPage assertCalendarSearchFiltersAndViews(String query) {
        Locator search = page.getByPlaceholder(CALENDAR_SEARCH_PLACEHOLDER,
                new Page.GetByPlaceholderOptions().setExact(true));
        assertVisible(search);
        assertVisible(button("Month view"));
        assertVisible(button("Week view"));
        assertVisible(button("Day view"));
        assertVisible(link("Agenda view"));
        assertVisible(button("Court Dates"));
        assertVisible(page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Search people...").setExact(true)));
        assertVisible(page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Search courtroom...").setExact(true)));

        search.fill(query);
        assertThat(search).hasValue(query);
        search.fill("");
        assertThat(search).hasValue("");

        button("Month view").click();
        page.waitForURL(Pattern.compile(".*/calendar/view-full-calendar\\?view=month"));
        assertVisible(page.getByRole(AriaRole.REGION,
                new Page.GetByRoleOptions().setName("Monthly calendar, scroll horizontally to see all days").setExact(true)));
        button("Week view").click();
        page.waitForURL(Pattern.compile(".*/calendar/view-full-calendar\\?view=week"));
        button("Day view").click();
        page.waitForURL(Pattern.compile(".*/calendar/view-full-calendar\\?view=day"));
        return this;
    }

    public SearchFilterPage goToWorkflows() {
        navigationLink("Workflows").click();
        page.waitForURL("**/workflow");
        assertVisible(heading("Workflows"));
        return this;
    }

    public SearchFilterPage assertWorkflowListObservationContract() {
        Locator table = page.getByRole(AriaRole.TABLE);
        assertVisible(table);
        for (String column : List.of("Workflow Name", "Trigger", "Actions")) {
            assertVisible(table.getByRole(AriaRole.COLUMNHEADER,
                    new Locator.GetByRoleOptions().setName(
                            Pattern.compile("^" + column + ": activate to sort.*$"))));
        }
        assertVisible(table.getByRole(AriaRole.COLUMNHEADER,
                new Locator.GetByRoleOptions().setName("Status").setExact(true)));
        assertVisible(link("+ New Workflow"));
        return this;
    }

    public SearchFilterPage assertUnsavedWorkflowBoundaryContract() {
        link("+ New Workflow").click();
        page.waitForURL("**/workflow/create");
        assertVisible(heading("Create New Workflow"));
        assertVisible(page.getByText("Trigger Type *", new Page.GetByTextOptions().setExact(true)));
        assertVisible(button("— Select trigger —"));
        assertVisible(buttonMatching(".*To-Do$"));
        assertVisible(buttonMatching(".*Calendar Event$"));
        assertVisible(page.getByRole(AriaRole.CHECKBOX,
                new Page.GetByRoleOptions().setName("Email assignee when created").setExact(true)));
        assertVisible(button("Save Workflow"));
        return this;
    }

    public SearchFilterPage assertSettingsObservationContract() {
        button("Settings").click();
        for (String destination : List.of(
                "Staff Management", "Reports", "Custom Fields", "Email Templates", "User Permissions",
                "Backup/Export Data", "Masters", "Theme Settings", "Create Document Template")) {
            assertVisible(link(destination));
        }
        return this;
    }

    public SearchFilterPage openReportsGateWithoutAuthenticating() {
        link("Reports").click();
        page.waitForURL("**/confirm-sensitive-access/reports");
        assertVisible(heading("Confirm your password"));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Password").setExact(true)));
        assertVisible(button("Continue to Reports"));
        return this;
    }

    /**
     * Completes the Reports-specific sensitive-access gate as authorized session setup, not as an
     * authentication test. It never invokes report generation, export, sending, or record actions.
     */
    public SearchFilterPage openReportsWithApprovedSessionCredential(String password) {
        assertSettingsObservationContract();
        link("Reports").click();
        page.waitForURL("**/confirm-sensitive-access/reports");
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Password").setExact(true)).fill(password);
        button("Continue to Reports").click();
        page.waitForURL("**/reports");
        assertVisible(heading("Reports"));
        return this;
    }

    public SearchFilterPage assertReportsReadOnlyTabAndFilterContracts() {
        assertReportShell();

        openReportTab("Time & Billing", "time");
        assertVisible(heading("Time Entries"));
        assertVisible(page.getByPlaceholder("Search attorney, client, matter…",
                new Page.GetByPlaceholderOptions().setExact(true)));

        openReportTabMatching("^.*Staff Workload$", "staff");
        assertVisible(heading("Staff Workload Overview"));
        assertVisible(page.getByPlaceholder("Search by staff name…",
                new Page.GetByPlaceholderOptions().setExact(true)));

        openReportTabMatching("^.*Case Activity$", "cases");
        assertVisible(heading("Case Activity Overview"));
        assertVisible(page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("All Statuses").setExact(true)));
        assertVisible(page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("All Types").setExact(true)));

        openReportTab("Client Statements", "clients");
        assertVisible(heading("Client Statements"));
        assertVisible(page.getByPlaceholder("Search client, email, invoice #…",
                new Page.GetByPlaceholderOptions().setExact(true)));

        openReportTab("Client Notes", "client_notes");
        assertVisible(heading("Client Notes"));
        assertVisible(page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("All Clients").setExact(true)));
        assertVisible(page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("All Properties").setExact(true)));
        assertVisible(page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("All Matters").setExact(true)));
        assertVisible(page.getByPlaceholder("Search matter, client, property, or note…",
                new Page.GetByPlaceholderOptions().setExact(true)));

        openReportTabMatching("^.*System Activity Log$", "activity");
        assertVisible(heading("Activity Log"));
        assertVisible(page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("All Users").setExact(true)));
        assertVisible(page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("All Actions").setExact(true)));
        assertVisible(page.getByPlaceholder("Search user, email, action, module, details…",
                new Page.GetByPlaceholderOptions().setExact(true)));

        openReportTabMatching("^.*Financial Summary$", "financial");
        assertVisible(heading("Recent Transactions"));
        assertVisible(page.getByPlaceholder("Search invoices...",
                new Page.GetByPlaceholderOptions().setExact(true)));
        return this;
    }

    private void assertReportShell() {
        assertVisible(heading("Reports"));
        // The two date inputs have visible text labels but no accessible name in the current PWA.
        assertThat(page.locator("input[type='date']")).hasCount(2);
        for (String tab : List.of("Time & Billing", "Client Statements", "Client Notes")) {
            assertVisible(link(tab));
        }
        assertVisible(linkMatching("^.*Financial Summary$"));
        assertVisible(linkMatching("^.*Staff Workload$"));
        assertVisible(linkMatching("^.*Case Activity$"));
        assertVisible(linkMatching("^.*System Activity Log$"));
    }

    private void openReportTab(String name, String tab) {
        link(name).click();
        page.waitForURL(Pattern.compile(".*/reports\\?tab=" + tab));
    }

    private void openReportTabMatching(String expression, String tab) {
        linkMatching(expression).click();
        page.waitForURL(Pattern.compile(".*/reports\\?tab=" + tab));
    }

    private Locator navigationLink(String name) {
        return page.getByRole(AriaRole.NAVIGATION,
                        new Page.GetByRoleOptions().setName("Main navigation").setExact(true))
                .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(name).setExact(true));
    }

    private Locator buttonMatching(String expression) {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(expression)));
    }

    private Locator linkMatching(String expression) {
        return page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile(expression)));
    }
}
