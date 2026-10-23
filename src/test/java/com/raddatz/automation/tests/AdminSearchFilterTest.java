package com.raddatz.automation.tests;

import com.raddatz.automation.pages.SearchFilterPage;
import org.testng.annotations.Test;

import java.util.UUID;

/** Read-only coverage of search/filter presentation, calendar views, and bounded admin observation screens. */
public final class AdminSearchFilterTest extends ReadOnlyTestBase {
    @Test(groups = "admin-readonly", description = "Company and task searches return empty results for a unique no-match query and reset safely.")
    public void searchAndFilterContractsAreReadOnly() {
        SearchFilterPage page = new SearchFilterPage(currentPage());
        page.goToClients()
                .assertClientSearchNoMatchThenClear(noMatchQuery("CLIENT"))
                .goToTodos()
                .assertTodoFilterContracts()
                .assertTodoSearchNoMatchThenReset(noMatchQuery("TASK"));
    }

    @Test(groups = "admin-readonly", description = "Calendar views and filters can be observed without opening, creating, or changing events.")
    public void calendarViewsAndFilterContractsAreReadOnly() {
        new SearchFilterPage(currentPage())
                .goToCalendar()
                .assertCalendarSearchFiltersAndViews(noMatchQuery("CALENDAR"));
    }

    @Test(groups = "admin-readonly", description = "Workflow contracts are observed without choosing triggers/actions, toggling workflows, or saving a workflow.")
    public void workflowContractsAreObservationOnly() {
        new SearchFilterPage(currentPage())
                .goToWorkflows()
                .assertWorkflowListObservationContract()
                .assertUnsavedWorkflowBoundaryContract();
    }

    @Test(groups = "admin-readonly", description = "Settings destinations are observed without opening restricted configuration controls.")
    public void settingsRemainObservationOnly() {
        new SearchFilterPage(currentPage()).assertSettingsObservationContract();
    }

    @Test(groups = "admin-readonly", description = "Reports are observed after approved sensitive-access setup without generating, exporting, sending, or opening record actions.")
    public void reportsTabsAndFiltersRemainReadOnly() {
        new SearchFilterPage(currentPage())
                .openReportsWithApprovedSessionCredential(config.password())
                .assertReportsReadOnlyTabAndFilterContracts();
    }

    private String noMatchQuery(String scope) {
        return "QA_READONLY_" + scope + "_" + UUID.randomUUID().toString().replace("-", "");
    }
}
