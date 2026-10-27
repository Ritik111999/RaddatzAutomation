package com.raddatz.automation.tests;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.*;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * End-to-End Court Updates and Billing Workflow:
 * 1. Schedule hearing event for today so it is immediately visible on Court Updates URL.
 * 2. Select that particular event under Calendar Entries (assert selection, prefilled matter & event IDs).
 * 3. Add a case To-Do directly from within Court Updates and verify linked To-Do display.
 * 4. Use the Edit Event hook (#addCourtDateRowBtn) to edit the event / schedule extra date with return_to routing.
 * 5. Save and submit the Court Update form, asserting persistence and To-Do popover badge.
 * 6. Launch Court Time Billing directly from the row action icon (with sensitive session authentication).
 * 7. Record 1.50 billable court hours, verify auto-calculated billing amount, and save entry.
 * 8. Propagate into Draft Invoicing, asserting court time line-item reflection and PDF artifact download.
 */
public final class AdminCourtUpdateBillingE2ETest extends ReadOnlyTestBase {

    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @TestMetadata(
            module = "CourtUpdates & Billing",
            scenarioId = "RAD-COURT-BILL-001",
            title = "Complete Court Updates to Billing Lifecycle: Event Selection, Linked To-Do, Event Edit, Court Update Submit, Court Time Billing, and Draft Invoice Line Items",
            testType = TestMetadata.TestType.E2E,
            expectedResult = "Hearing event selected on Court Updates, case To-Do linked, event edited via return_to hook, court update submitted, court time recorded, and line items verified in draft invoice.",
            preconditions = "Admin authenticated on local environment with mutation permission and sensitive billing access.",
            testSteps = {
                    "Create scenario-owned parent Company, Property, and Matter",
                    "Schedule initial Hearing Event for today's date so it is immediately present on Court Updates URL",
                    "Navigate to Court Updates (/court/updates) and select the specific hearing under Calendar Entries",
                    "Assert event is highlighted as selected, and matter/event hidden fields are prefilled",
                    "Add case To-Do via #addTodoBtn modal and verify it renders in linked To-Do section",
                    "Click Edit Event (#addCourtDateRowBtn) to open event edit form with return_to routing",
                    "Update event notes and submit with redirect back to Court Updates",
                    "Submit Court Update form and assert row presence in Court Updates List table",
                    "Click Open Court Time billing action on table row and confirm sensitive session",
                    "Record 1.50 billable court hours, assert auto-calculated total, and save billing",
                    "Propagate to Draft Invoices, assert line items reflect court hearing time, and download PDF artifact"
            },
            defectSeverity = TestMetadata.DefectSeverity.CRITICAL
    )
    @Test(groups = {"admin-mutation", "court-billing"}, description = "Administrator executes full Court Updates selection, To-Do linking, Event editing, Court Billing, and Draft Invoicing.")
    public void administratorExecutesCourtUpdateAndBillingJourney() {
        Clock clock = Clock.systemUTC();
        String today = LocalDate.now(clock).toString();
        AdminScenarioData data = AdminScenarioData.create(clock).withEventDate(today);
        Page page = currentPage();
        Sidebar sidebar = new Sidebar(page);
        Path downloadedInvoice = Path.of("target", "automation-artifacts", "downloaded-invoices",
                "court-billing-invoice-" + data.reference() + ".pdf");

        logger.info("=== STEP 1: CREATE SCENARIO-OWNED HIERARCHY ===");
        sidebar.goToClients()
                .openAddCompany()
                .createOwnedCompany(data);

        sidebar.goToClients()
                .openAddProperty()
                .createOwnedProperty(data);

        sidebar.goToClients()
                .openAddMatter()
                .createOwnedMatter(data)
                .assertOwnedMatterHierarchy(data);

        logger.info("=== STEP 2: SCHEDULE HEARING EVENT FOR TODAY ===");
        sidebar.goToDashboard()
                .openAddEvent()
                .createOwnedEvent(data);

        logger.info("=== STEP 3: NAVIGATE TO COURT UPDATES (EVENT IS PRESENT ON TODAY'S DATE) ===");
        CourtUpdatesPage courtUpdates = sidebar.goToCourtUpdates()
                .assertLoaded();

        logger.info("=== STEP 4: SELECT THAT PARTICULAR EVENT IN COURT UPDATES ===");
        courtUpdates.selectCalendarEvent(data.eventTitle());
        assertThat(page.locator("#calendar_event_id_hidden")).not().hasValue("");
        assertThat(page.locator("#matter_id_hidden")).not().hasValue("");

        logger.info("=== STEP 5: ADD LINKED TO-DO FOR THAT CASE ===");
        String courtTodoTitle = "Court Filing Follow-up " + data.reference();
        String courtTodoDueDate = LocalDate.now(clock).plusDays(5).toString();
        courtUpdates.addCourtTodo(courtTodoTitle, courtTodoDueDate);

        logger.info("=== STEP 6: EDIT THAT EVENT VIA RETURN_TO HOOK ===");
        EventFormPage eventForm = courtUpdates.openEditEventForm();
        String extraEventTitle = "Updated Hearing " + data.reference();
        String extraEventDate = LocalDate.now(clock).plusDays(7).toString();
        eventForm.scheduleCourtDateFromCourtUpdates(extraEventTitle, extraEventDate);

        logger.info("=== STEP 7: SAVE AND SUBMIT COURT UPDATE ===");
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Notes *").setExact(true)).fill(data.ownershipNote());
        page.locator("#case_status").selectOption("active");
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Submit Court Update.*"))).click();

        // Verify update saved in table
        page.reload();
        courtUpdates.assertLoaded();
        courtUpdates.assertCourtTodoPopover(data.ownershipNote(), courtTodoTitle);

        logger.info("=== STEP 8: LAUNCH COURT TIME BILLING FROM TABLE ROW ===");
        CourtTimePage courtTimePage = courtUpdates.openCourtBillingForRow(data.ownershipNote(), config.password());
        courtTimePage.recordCourtHearingTime(data, "1.50");

        logger.info("=== STEP 9: PROPAGATE COURT TIME TO TIME SECTION ===");
        TimeEntryPage timePage = courtTimePage.saveAndPropagateToTime(config.password());
        timePage.updateOwnedTimeEntry(data.ownershipNote(), data.revisedTimeEntryDescription());

        logger.info("=== STEP 10: VERIFY INVOICE DRAFT REFLECTS COURT TIME & DOWNLOAD PDF ===");
        new InvoiceDraftPage(page)
                .open()
                .createOwnedDraft(data)
                .assertOwnedDraftReflectsTime(data, data.revisedTimeEntryDescription())
                .downloadOwnedInvoicePdf(downloadedInvoice);

        logger.info("=== COURT UPDATES & BILLING LIFECYCLE COMPLETED WITH 100% SUCCESS ===");
    }
}
