package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;

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

    @TestMetadata(
            module = "PortfolioHierarchy",
            scenarioId = "RAD-COMP-001",
            title = "Create and Locate Owned Legal Client Company",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "New property management Company is persisted with realistic business details and listed in client directory.",
            preconditions = "Approved non-production environment.",
            testSteps = {
                    "Navigate to Clients & Matters directory",
                    "Open Add Company form modal",
                    "Fill company name, authorized contacts, address, and ownership reference",
                    "Submit company creation",
                    "Search client directory and assert company is listed in table"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
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
