package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
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

    @TestMetadata(
            module = "PortfolioHierarchy",
            scenarioId = "RAD-MATT-002",
            title = "Create Multi-Tenant Co-Defendant Matter",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Matter is created with primary tenant and secondary co-defendant tenant card, displaying both in grid row.",
            preconditions = "An owned Company portfolio and Property building exist.",
            testSteps = {
                    "Create parent Company and Property building",
                    "Open Add New Matter form modal",
                    "Populate primary tenant card",
                    "Click Add Tenant and populate second tenant card with distinct contact details",
                    "Save Matter and verify both tenant names appear in matter row"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
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
