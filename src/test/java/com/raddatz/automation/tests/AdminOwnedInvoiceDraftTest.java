package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.InvoiceDraftPage;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.time.Clock;

/**
 * Creates a draft from an owned Matter and owned time entry only. Sending,
 * payment allocation, export, and void operations are intentionally excluded.
 */
public final class AdminOwnedInvoiceDraftTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @TestMetadata(
            module = "FinancialPipeline",
            scenarioId = "RAD-INVC-001",
            title = "Draft Invoice Compilation and PDF Artifact Generation",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Draft invoice is created from billable time, verifies totals and line items, and generates valid downloadable %PDF- document.",
            preconditions = "An owned Matter case and billable time entry exist.",
            testSteps = {
                    "Create parent Company, Property, Matter, and billable time entry chain",
                    "Navigate to Invoices and create draft invoice for owned Matter",
                    "Assert line-item table reflects owned time, hours, rate, and total",
                    "Download invoice PDF artifact and verify valid PDF header structure"
            },
            defectSeverity = TestMetadata.DefectSeverity.CRITICAL
    )
    @Test(groups = "admin-mutation", description = "Administrator creates and verifies an owned invoice draft from owned billable time.")
    public void administratorCreatesOwnedInvoiceDraftFromOwnedTime() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        Path downloadedInvoice = config.artifactDirectory().resolve("downloads")
                .resolve("owned-invoice-" + data.reference() + ".pdf");
        new Sidebar(currentPage()).goToClients().openAddCompany().createOwnedCompany(data)
                .openAddProperty().createOwnedProperty(data);
        new Sidebar(currentPage()).goToClients().openAddMatter().createOwnedMatter(data)
                .openOwnedMatter(data.tenantName(), data.caseNumber())
                .openTimeEntry()
                .start(config.password())
                .createOwnedTimeEntry(data);

        new InvoiceDraftPage(currentPage())
                .open()
                .createOwnedDraft(data)
                .assertOwnedDraft(data)
                .downloadOwnedInvoicePdf(downloadedInvoice);
    }
}
