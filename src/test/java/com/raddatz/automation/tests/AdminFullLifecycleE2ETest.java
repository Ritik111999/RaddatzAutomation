package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.data.UploadFixtures;
import com.raddatz.automation.pages.*;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.time.Clock;

/**
 * End-to-end Master Business Lifecycle Test:
 * 1. Login (Session setup)
 * 2. Create Company -> Property -> Matter
 * 3. Upload docs and notes at all three levels (Company, Property, Matter)
 * 4. Create tasks and assign to staff on the Matter
 * 5. Create Events and dedicated Court Event for the Matter
 * 6. Save and submit the Court Update
 * 7. Continue to Court Time: set field, verify auto-populated amount, edit/delete
 * 8. Save Court Time -> verify propagation to Time section, test Time and Expense CRUD
 * 9. Invoices: Create New Draft, save draft, download PDF, and Send
 */
public final class AdminFullLifecycleE2ETest extends ReadOnlyTestBase {

    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @TestMetadata(
            module = "MasterE2E",
            scenarioId = "RAD-E2E-001",
            title = "Authoritative Full Raddatz Legal and Billing Lifecycle Journey",
            testType = TestMetadata.TestType.E2E,
            expectedResult = "Full portfolio hierarchy, case management artifacts, court hearings, time entries, expenses, and PDF invoice draft are created, verified, and linked across all subsystems.",
            preconditions = "Approved non-production environment with active administrative session.",
            testSteps = {
                    "Create automation-owned Company portfolio with unique scenario reference",
                    "Add owned Property building below Company",
                    "Open new legal Matter (case) with primary tenant below Property",
                    "Upload case evidence document and add timestamped audit note to Matter",
                    "Create Matter task and verify persistence on To-dos table",
                    "Schedule Calendar hearing event for the Matter",
                    "Submit Court Update with hearing notes from scheduled event",
                    "Record billable Court Time (1.50 hrs) and verify automatic calculation",
                    "Verify propagation to Time section and create $75 case Expense",
                    "Generate Draft Invoice aggregating time + expense, download verified PDF, and execute modal send confirmation"
            },
            defectSeverity = TestMetadata.DefectSeverity.CRITICAL
    )
    @Test(groups = "admin-mutation", description = "Executes the authoritative full Raddatz business lifecycle end-to-end.")
    public void administratorExecutesFullLifecycleJourney() {

        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        Path documentFixture = UploadFixtures.copyInspectionMemorandum(data.ownedDocumentFileName(), config.artifactDirectory());
        Path downloadedInvoice = config.artifactDirectory().resolve("downloads")
                .resolve("owned-e2e-invoice-" + data.reference() + ".pdf");

        logger.info("Starting Full E2E Lifecycle Journey for scenario: {}", data.reference());

        // --- STEP 1 & 2: Create Company -> Property -> Matter ---
        Sidebar sidebar = new Sidebar(currentPage());
        ClientMatterListPage clientsPage = sidebar.goToClients();

        logger.info("Step 2: Creating Company: {}", data.companyName());
        clientsPage.openAddCompany().createOwnedCompany(data);

        logger.info("Step 2: Creating Property: {}", data.propertyName());
        sidebar.goToClients().openAddProperty().createOwnedProperty(data);

        logger.info("Step 2: Creating Matter: Case {}", data.caseNumber());
        sidebar.goToClients().openAddMatter().createOwnedMatter(data);

        // --- STEP 3: Upload Documents and Notes at ALL 3 Levels ---
        logger.info("Step 3A: Adding Company Note and Document");
        CompanyDetailsPage companyPage = sidebar.goToClients().openOwnedCompany(data.companyName());
        companyPage.addOwnedNote("Company note: " + data.ownershipNote())
                .uploadOwnedDocument(documentFixture);

        logger.info("Step 3B: Adding Property Note and Document");
        sidebar.goToClients();
        PropertyDetailsPage propertyPage = new OwnedHierarchyEditorPage(currentPage()).openPropertyView(data);
        propertyPage.addOwnedNote("Property note: " + data.ownershipNote())
                .uploadOwnedDocument(documentFixture);

        logger.info("Step 3C: Adding Matter Note and Document");
        MatterWorkspacePage matterWorkspace = sidebar.goToClients().openOwnedMatter(data.tenantName(), data.caseNumber());
        matterWorkspace.openNotes().createOwnedNote(data.initialMatterNote());
        matterWorkspace.openDocuments().openCustomUpload().uploadOwnedDocument(documentFixture);

        // --- STEP 4: Tasks on Matter & Task Assignment ---
        logger.info("Step 4: Creating and assigning Matter Task to Staff");
        sidebar.goToDashboard().openAddTask().createOwnedTask(data);
        sidebar.goToTodos().assertTaskListed(data.taskName());

        // --- STEP 5: Events and Court Event ---
        logger.info("Step 5: Creating Calendar Court Event for Matter");
        sidebar.goToDashboard().openAddEvent().createOwnedEvent(data);
        sidebar.goToCalendar().assertOwnedEventVisible(data, false);

        // --- STEP 6: Save and Submit Court Update ---
        logger.info("Step 6: Submitting Court Update from Calendar Event");
        CourtUpdatesPage courtUpdatesPage = sidebar.goToCourtUpdates()
                .selectCalendarDate(data.eventDate())
                .createOwnedCourtUpdate(data);

        // --- STEP 7: Court Time: Set Field, Check Auto-Amount, Edit ---
        logger.info("Step 7: Opening Court Time and asserting auto-populated amount");
        CourtTimePage courtTimePage = courtUpdatesPage.continueToCourtTime(config.password());
        courtTimePage.recordCourtHearingTime(data, "1.50");

        // --- STEP 8: Save Court Time -> Propagates to Time Section, Time & Expense CRUD ---
        logger.info("Step 8: Propagating Court Time to Time Section and exercising Billing");
        TimeEntryPage timePage = courtTimePage.saveAndPropagateToTime(config.password());
        timePage.updateOwnedTimeEntry(data.ownershipNote(), data.revisedTimeEntryDescription());

        logger.info("Step 8B: Exercising Expense flow");
        timePage.openExpenses()
                .createOwnedExpense(data)
                .updateOwnedExpense(data);

        // --- STEP 9: Invoices: New Draft -> Save Draft -> Download -> Send ---
        logger.info("Step 9: Generating Invoice Draft and Sending");
        new InvoiceDraftPage(currentPage())
                .open()
                .createOwnedDraft(data)
                .assertOwnedDraftReflectsTime(data, data.revisedTimeEntryDescription())
                .downloadOwnedInvoicePdf(downloadedInvoice)
                .sendOwnedInvoice();

        logger.info("Successfully completed full Raddatz business lifecycle journey for: {}", data.reference());
    }
}
