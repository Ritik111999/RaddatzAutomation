package com.raddatz.automation.tests;

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
