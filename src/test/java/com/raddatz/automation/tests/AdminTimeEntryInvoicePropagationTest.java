package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.InvoiceDraftPage;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/**
 * Verifies that an edit to a current-run billable entry survives reload and is
 * consumed by the owned Matter's draft-invoice source table. No send, payment,
 * allocation, void, or settlement control is used.
 */
public final class AdminTimeEntryInvoicePropagationTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @TestMetadata(
            module = "FinancialPipeline",
            scenarioId = "RAD-PROP-002",
            title = "Billable Time Entry Modification and Draft Invoice Line-Item Propagation",
            testType = TestMetadata.TestType.INTEGRATION,
            expectedResult = "Revised description on billable time entry propagates into draft invoice line item table without data loss.",
            preconditions = "An owned Matter case, billable time entry, and billing session exist.",
            testSteps = {
                    "Create parent Company, Property, and Matter chain",
                    "Record billable time entry and update description",
                    "Open Invoices and initiate new Draft Invoice for owned Matter",
                    "Assert revised billable time description is present in draft line items"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
    @Test(groups = "admin-mutation", description = "Updated owned time entry is reloaded and reflected in its owned draft invoice.")
    public void administratorSeesUpdatedOwnedTimeEntryInDraftInvoice() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        new Sidebar(currentPage()).goToClients().openAddCompany().createOwnedCompany(data)
                .openAddProperty().createOwnedProperty(data);
        new Sidebar(currentPage()).goToClients().openAddMatter().createOwnedMatter(data)
                .openOwnedMatter(data.tenantName(), data.caseNumber())
                .openTimeEntry()
                .start(config.password())
                .createOwnedTimeEntry(data)
                .updateOwnedTimeEntry(data.initialTimeEntryDescription(), data.revisedTimeEntryDescription());

        new InvoiceDraftPage(currentPage())
                .open()
                .createOwnedDraft(data)
                .assertOwnedDraftReflectsTime(data, data.revisedTimeEntryDescription());
    }
}
