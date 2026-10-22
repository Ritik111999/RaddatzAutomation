package com.raddatz.automation.tests;

import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.ClientMatterListPage;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/**
 * First mutation slice. It creates one owned Company and never edits an existing record.
 * This test is excluded from the default suite and requires the mutation switch.
 */
public final class AdminCompanyCreationTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @Test(groups = "admin-mutation", description = "Administrator creates and locates an owned Company with field-appropriate data.")
    public void administratorCreatesOwnedCompany() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        ClientMatterListPage companies = new Sidebar(currentPage()).goToClients();

        companies.openAddCompany()
                .createOwnedCompany(data)
                .searchFor(data.companyName())
                .assertCompanyIsListed(data.companyName());
    }
}
