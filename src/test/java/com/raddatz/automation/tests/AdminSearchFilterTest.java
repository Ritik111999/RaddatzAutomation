package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.pages.SearchFilterPage;
import org.testng.annotations.Test;

import java.util.UUID;

/** Read-only coverage of search/filter presentation, calendar views, and bounded admin observation screens. */
public final class AdminSearchFilterTest extends ReadOnlyTestBase {

    @TestMetadata(
            module = "ReadonlyAdministration",
            scenarioId = "RAD-RO-001",
            title = "Global Client and Task Search Empty State and Filter Reset",
            testType = TestMetadata.TestType.READ_ONLY,
            expectedResult = "Unique no-match search query yields empty state and clears safely without data leakage.",
            preconditions = "Active administrative session.",
            testSteps = {
                    "Navigate to Clients directory and execute no-match search query",
                    "Assert zero matching rows and clear search query",
                    "Navigate to To-dos directory and test filter contracts",
                    "Execute no-match search query on To-dos and assert empty state"
            },
            defectSeverity = TestMetadata.DefectSeverity.LOW
    )
    @Test(groups = "admin-readonly", description = "Company and task searches return empty results for a unique no-match query and reset safely.")
    public void searchAndFilterContractsAreReadOnly() {
        SearchFilterPage page = new SearchFilterPage(currentPage());
        page.goToClients()
                .assertClientSearchNoMatchThenClear(noMatchQuery("CLIENT"))
                .goToTodos()
                .assertTodoFilterContracts()
                .assertTodoSearchNoMatchThenReset(noMatchQuery("TASK"));
    }

    @TestMetadata(
            module = "ReadonlyAdministration",
            scenarioId = "RAD-RO-002",
            title = "Calendar Views Switching and Date Range Filtering Observation",
            testType = TestMetadata.TestType.READ_ONLY,
            expectedResult = "Calendar switches between Month, Week, and Day views without altering scheduled hearings.",
            preconditions = "Active administrative session.",
            testSteps = {
                    "Navigate to Calendar view",
                    "Assert view switchers (Month, Week, Day)",
                    "Test date search and verify zero mutation"
            },
            defectSeverity = TestMetadata.DefectSeverity.LOW
    )
    @Test(groups = "admin-readonly", description = "Calendar views and filters can be observed without opening, creating, or changing events.")
    public void calendarViewsAndFilterContractsAreReadOnly() {
        new SearchFilterPage(currentPage())
                .goToCalendar()
                .assertCalendarSearchFiltersAndViews(noMatchQuery("CALENDAR"));
    }

    @TestMetadata(
            module = "ReadonlyAdministration",
            scenarioId = "RAD-RO-003",
            title = "Automated Workflows Observation and Boundary Contract Enforcement",
            testType = TestMetadata.TestType.READ_ONLY,
            expectedResult = "Workflow triggers and action contracts are displayed without triggering execution or saving workflows.",
            preconditions = "Active administrative session.",
            testSteps = {
                    "Navigate to Workflows list",
                    "Assert workflow contracts and inspect creation boundary",
                    "Verify no workflows are triggered or mutated"
            },
            defectSeverity = TestMetadata.DefectSeverity.LOW
    )
    @Test(groups = "admin-readonly", description = "Workflow contracts are observed without choosing triggers/actions, toggling workflows, or saving a workflow.")
    public void workflowContractsAreObservationOnly() {
        new SearchFilterPage(currentPage())
                .goToWorkflows()
                .assertWorkflowListObservationContract()
                .assertUnsavedWorkflowBoundaryContract();
    }

    @TestMetadata(
            module = "ReadonlyAdministration",
            scenarioId = "RAD-RO-004",
            title = "Administrative Settings Menu and Restricted Control Boundaries",
            testType = TestMetadata.TestType.READ_ONLY,
            expectedResult = "Permitted settings links are visible while restricted configuration controls remain untouched.",
            preconditions = "Active administrative session.",
            testSteps = {
                    "Open Settings navigation menu",
                    "Assert visible destinations (templates, profile, company)",
                    "Ensure forbidden endpoints (integrations, imports, bulk deletes) are untouched"
            },
            defectSeverity = TestMetadata.DefectSeverity.MEDIUM
    )
    @Test(groups = "admin-readonly", description = "Settings destinations are observed without opening restricted configuration controls.")
    public void settingsRemainObservationOnly() {
        new SearchFilterPage(currentPage()).assertSettingsObservationContract();
    }

    @TestMetadata(
            module = "ReadonlyAdministration",
            scenarioId = "RAD-RO-005",
            title = "Executive Reports Shell and Filter Presentation",
            testType = TestMetadata.TestType.READ_ONLY,
            expectedResult = "Report tabs and filters render without exporting, dispatching emails, or mutating case records.",
            preconditions = "Active administrative session and report passkey authentication.",
            testSteps = {
                    "Open Reports module and authenticate session credential",
                    "Assert report tab headers and filter dropdowns",
                    "Verify no exports or actions are dispatched"
            },
            defectSeverity = TestMetadata.DefectSeverity.LOW
    )
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
