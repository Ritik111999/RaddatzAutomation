package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.ClientMatterListPage;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/** Creates an isolated Company → Property → Matter chain in one run. */
public final class AdminMatterCreationTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @TestMetadata(
            module = "PortfolioHierarchy",
            scenarioId = "RAD-MATT-001",
            title = "Open Legal Matter Case Under Owned Property",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Legal matter (case) is created with primary tenant card, docket number, and priority under Property.",
            preconditions = "An owned Company portfolio and Property building exist.",
            testSteps = {
                    "Create parent Company and Property building",
                    "Open Add New Matter form modal",
                    "Select Company, Property, and fill tenant details, unit, docket, and priority",
                    "Submit matter creation",
                    "Search client list and assert tenant name is listed in matter grid"
            },
            defectSeverity = TestMetadata.DefectSeverity.CRITICAL
    )
    @Test(groups = "admin-mutation", description = "Administrator creates an owned Matter under the owned Company and Property.")
    public void administratorCreatesOwnedMatter() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        ClientMatterListPage companies = new Sidebar(currentPage()).goToClients();

        companies.openAddCompany()
                .createOwnedCompany(data)
                .openAddProperty()
                .createOwnedProperty(data);

        new Sidebar(currentPage()).goToClients()
                .openAddMatter()
                .createOwnedMatter(data)
                .searchFor(data.tenantName())
                .assertListedName(data.tenantName());
    }
}
