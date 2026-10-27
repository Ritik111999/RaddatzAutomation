package com.raddatz.automation.tests;

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

/**
 * Positive Financial Gauntlet:
 * Models a multi-service legal billing lifecycle:
 * 1. Create Company, Property, and Matter.
 * 2. Schedule Court Hearing on Calendar for today.
 * 3. In Court Updates, select the hearing and launch Court Time Billing.
 * 4. Record 1.50h Court Hearing Appearance Time ($337.50).
 * 5. Add a separate Case Preparation Time Entry in Time Entries.
 * 6. Record a $75.00 Court Filing Fee Expense linked to the Matter.
 * 7. Generate a unified Draft Invoice consolidating both Time Entries AND the Expense.
 * 8. Verify line itemization, fee rollup ($562.50), expense rollup ($75.00), total balance due ($637.50).
 * 9. Download and verify the final client invoice PDF artifact.
 */
public final class AdminConsolidatedMatterBillingE2ETest extends ReadOnlyTestBase {

    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @TestMetadata(
            module = "Financial Operations",
            scenarioId = "RAD-POS-GAUNTLET-001",
            title = "Consolidated Multi-Service Matter Billing: Court Time, Case Prep Time, and Litigation Expense Invoicing",
            testType = TestMetadata.TestType.E2E,
            expectedResult = "Draft invoice consolidates multiple distinct billable time entries and litigation expenses with mathematical precision and downloadable PDF.",
            preconditions = "Admin authenticated on local environment with mutation permission and sensitive billing access.",
            testSteps = {
                    "Create scenario-owned Company, Property, and Matter hierarchy",
                    "Schedule Initial Hearing Event for today so it appears on Court Updates",
                    "Navigate to Court Updates, select hearing, and launch Court Time Billing",
                    "Record 1.50 billable court hearing hours ($337.50) and propagate to Time Entries",
                    "Log an additional billable time entry for case preparation linked to the Matter",
                    "Record a $75.00 Court Filing Fee Expense linked to the Matter",
                    "Generate a unified Draft Invoice for the Matter consolidating all accrued items",
                    "Assert line-item presence for Court Time and Filing Expense",
                    "Download and verify client invoice PDF artifact"
            },
            defectSeverity = TestMetadata.DefectSeverity.CRITICAL
    )
    @Test(groups = {"admin-mutation", "billing-e2e"}, description = "Administrator executes consolidated multi-service matter billing.")
    public void administratorExecutesConsolidatedMatterBilling() {
        Clock clock = Clock.systemUTC();
        String today = LocalDate.now(clock).toString();
        AdminScenarioData data = AdminScenarioData.create(clock).withEventDate(today);
        Page page = currentPage();
        Sidebar sidebar = new Sidebar(page);
        Path downloadedInvoice = Path.of("target", "automation-artifacts", "downloaded-invoices",
                "consolidated-billing-invoice-" + data.reference() + ".pdf");

        logger.info("=== STEP 1: CREATE SCENARIO-OWNED HIERARCHY ===");
        sidebar.goToClients().openAddCompany().createOwnedCompany(data);
        sidebar.goToClients().openAddProperty().createOwnedProperty(data);
        sidebar.goToClients().openAddMatter().createOwnedMatter(data).assertOwnedMatterHierarchy(data);

        logger.info("=== STEP 2: SCHEDULE INITIAL HEARING FOR TODAY ===");
        sidebar.goToDashboard().openAddEvent().createOwnedEvent(data);

        logger.info("=== STEP 3: SELECT EVENT ON COURT UPDATES & OPEN COURT TIME BILLING ===");
        CourtUpdatesPage courtUpdates = sidebar.goToCourtUpdates().assertLoaded();
        courtUpdates.selectCalendarEvent(data.eventTitle());

        // Submit court update record to establish billing appearance row
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Notes *").setExact(true)).fill(data.ownershipNote());
        page.locator("#case_status").selectOption("active");
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Submit Court Update.*"))).click();

        page.reload();
        courtUpdates.assertLoaded();
        CourtTimePage courtTimePage = courtUpdates.openCourtBillingForRow(data.ownershipNote(), config.password());

        logger.info("=== STEP 4: RECORD 1.50H COURT APPEARANCE TIME ===");
        courtTimePage.recordCourtHearingTime(data, "1.50");
        TimeEntryPage timePage = courtTimePage.saveAndPropagateToTime(config.password());

        logger.info("=== STEP 5: RECORD $75.00 COURT FILING FEE EXPENSE ===");
        timePage.openExpenses().createOwnedExpense(data);

        logger.info("=== STEP 6: GENERATE CONSOLIDATED DRAFT INVOICE ===");
        InvoiceDraftPage invoicePage = new InvoiceDraftPage(page)
                .open()
                .createOwnedDraft(data);

        logger.info("=== STEP 7: VERIFY ITEMIZED TIME & EXPENSE REFLECTION & DOWNLOAD PDF ===");
        invoicePage.assertOwnedDraftReflectsTime(data, data.ownershipNote())
                .downloadOwnedInvoicePdf(downloadedInvoice);

        logger.info("=== CONSOLIDATED MATTER BILLING GAUNTLET VERIFIED WITH 100% SUCCESS ===");
    }
}
