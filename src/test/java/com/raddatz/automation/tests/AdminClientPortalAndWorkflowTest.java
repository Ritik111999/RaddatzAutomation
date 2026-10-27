package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.pages.ClientPortalDocumentsPage;
import com.raddatz.automation.pages.ClientPortalSpreadsheetsPage;
import com.raddatz.automation.pages.Sidebar;
import com.raddatz.automation.pages.WorkflowPage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Validates the Client Portal (Spreadsheets & Documents) and Workflows modules.
 * These areas provide read-only data visualization, view switching, and workflow contract definition.
 */
public final class AdminClientPortalAndWorkflowTest extends ReadOnlyTestBase {

    @TestMetadata(
            module = "ClientPortal",
            scenarioId = "RAD-PORTAL-001",
            title = "Client Portal Property Spreadsheets Directory and Property Ledger View",
            testType = TestMetadata.TestType.READ_ONLY,
            expectedResult = "Property Spreadsheets table loads with record counters, search filtering functions, and property detail ledger opens cleanly.",
            preconditions = "Authenticated administrative session.",
            testSteps = {
                    "Open Client Portal dropdown in navbar and navigate to Spreadsheets (/spreadsheets)",
                    "Assert table columns, record counter, and search input",
                    "Execute live search query and clear filter",
                    "Click first property link and assert property-level spreadsheet ledger loads (/property/{id}/spreadsheet)"
            },
            defectSeverity = TestMetadata.DefectSeverity.MEDIUM
    )
    @Test(groups = "admin-readonly", description = "Administrator explores Property Spreadsheets directory, search filter, and property ledger view.")
    public void administratorVerifiesClientPortalSpreadsheets() {
        Sidebar sidebar = new Sidebar(currentPage());
        ClientPortalSpreadsheetsPage spreadsheetsPage = sidebar.goToClientPortalSpreadsheets();

        spreadsheetsPage.assertLoaded()
                .assertTableColumns();

        int totalCount = spreadsheetsPage.getRecordCount();
        assertTrue(totalCount >= 0, "Record count must be non-negative");

        spreadsheetsPage.searchSpreadsheet("Waterfront")
                .clearSearch();

        spreadsheetsPage.openFirstPropertySpreadsheet()
                .assertPropertySpreadsheetViewLoaded();
    }

    @TestMetadata(
            module = "ClientPortal",
            scenarioId = "RAD-PORTAL-002",
            title = "Client Portal Document Management Hub and View Switching",
            testType = TestMetadata.TestType.READ_ONLY,
            expectedResult = "Document Management hub loads and switches between Folder View and List View seamlessly.",
            preconditions = "Authenticated administrative session.",
            testSteps = {
                    "Open Client Portal dropdown in navbar and navigate to Documents (/documents)",
                    "Assert Document Management heading and view switch controls",
                    "Toggle to List View and back to Folder View"
            },
            defectSeverity = TestMetadata.DefectSeverity.LOW
    )
    @Test(groups = "admin-readonly", description = "Administrator navigates to Client Portal Documents and toggles Folder/List view modes.")
    public void administratorVerifiesClientPortalDocuments() {
        Sidebar sidebar = new Sidebar(currentPage());
        ClientPortalDocumentsPage docsPage = sidebar.goToClientPortalDocuments();

        docsPage.assertLoaded()
                .switchToListMode()
                .switchToFolderMode();
    }

    @TestMetadata(
            module = "Workflows",
            scenarioId = "RAD-WORKFLOW-001",
            title = "Central Workflows Directory and Workflow Creation Contract Validation",
            testType = TestMetadata.TestType.READ_ONLY,
            expectedResult = "Workflows list renders status and trigger columns, and Create Workflow form exposes contract boundaries.",
            preconditions = "Authenticated administrative session.",
            testSteps = {
                    "Navigate to Workflows (/workflow) via top navigation bar",
                    "Assert Workflows table headers (Workflow Name, Trigger, Actions, Status)",
                    "Click New Workflow button to navigate to /workflow/create",
                    "Assert Create New Workflow form, trigger options (To-Do, Calendar Event), and Save button"
            },
            defectSeverity = TestMetadata.DefectSeverity.MEDIUM
    )
    @Test(groups = "admin-readonly", description = "Administrator observes central Workflows list and validates creation form contracts.")
    public void administratorVerifiesWorkflowsDirectoryAndCreationContracts() {
        Sidebar sidebar = new Sidebar(currentPage());
        WorkflowPage workflowPage = sidebar.goToWorkflows();

        workflowPage.assertLoaded()
                .assertTableColumns()
                .openCreateWorkflow()
                .assertCreateWorkflowLoaded();
    }
}
