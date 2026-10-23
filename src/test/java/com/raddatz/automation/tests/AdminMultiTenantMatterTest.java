package com.raddatz.automation.tests;

import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/** Adds a second owned tenant to one owned Matter; no existing Matter is used. */
public final class AdminMultiTenantMatterTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @Test(groups = "admin-mutation", description = "Administrator adds a fully populated second tenant to an owned Matter.")
    public void administratorAddsOwnedSecondTenantToOwnedMatter() {
        AdminScenarioData matter = AdminScenarioData.create(Clock.systemUTC());
        AdminScenarioData secondTenant = AdminScenarioData.create(Clock.systemUTC())
                .withCompanyAndPropertyFrom(matter, matter);

        new Sidebar(currentPage()).goToClients()
                .openAddCompany()
                .createOwnedCompany(matter)
                .openAddProperty()
                .createOwnedProperty(matter);

        new Sidebar(currentPage()).goToClients()
                .openAddMatter()
                .createOwnedMatterWithAdditionalTenant(matter, secondTenant)
                .assertOwnedMultiTenantMatterListed(matter, secondTenant);
    }
}
