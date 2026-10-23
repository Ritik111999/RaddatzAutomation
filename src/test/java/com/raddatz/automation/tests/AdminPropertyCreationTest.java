package com.raddatz.automation.tests;

import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.ClientMatterListPage;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/**
 * Creates an isolated Company → Property chain in the same run. This is deliberately
 * outside the default suite and needs explicit mutation permission.
 */
public final class AdminPropertyCreationTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @Test(groups = "admin-mutation", description = "Administrator creates a Property below the owned Company using field-appropriate data.")
    public void administratorCreatesOwnedProperty() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        ClientMatterListPage companies = new Sidebar(currentPage()).goToClients();

        companies.openAddCompany()
                .createOwnedCompany(data)
                .openAddProperty()
                .createOwnedProperty(data);
    }
}
