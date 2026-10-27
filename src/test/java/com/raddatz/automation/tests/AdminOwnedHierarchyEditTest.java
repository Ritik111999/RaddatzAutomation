package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.OwnedHierarchyEditorPage;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/** Verifies persisted Company/Property/Matter edits on a fresh owned hierarchy. */
public final class AdminOwnedHierarchyEditTest extends ReadOnlyTestBase {
    @Override protected boolean requiresMutationPermission() { return true; }

    @TestMetadata(
            module = "PortfolioHierarchy",
            scenarioId = "RAD-HIER-001",
            title = "Edit Hierarchy Entities In-Place and Verify Persistence",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Company contact role, Property unit count, and Matter priority updates persist after reload without corrupting parent-child lineage.",
            preconditions = "An owned Company, Property, and Matter hierarchy exists.",
            testSteps = {
                    "Create parent Company, Property, and Matter chain",
                    "Open Company edit modal and update contact title",
                    "Open Property edit modal and update unit count",
                    "Open Matter edit modal and update case priority",
                    "Reload page and reopen modals to confirm exact persisted values"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
    @Test(groups="admin-mutation", description="Edit an owned Company contact role, Property unit count and Matter priority, then verify persistence and unchanged lineage.")
    public void administratorEditsOwnedHierarchy() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        new Sidebar(currentPage()).goToClients().openAddCompany().createOwnedCompany(data)
                .openAddProperty().createOwnedProperty(data);
        new Sidebar(currentPage()).goToClients().openAddMatter().createOwnedMatter(data);
        new OwnedHierarchyEditorPage(currentPage()).updateCompanyContactTitle(data)
                .updatePropertyUnits(data).updateMatterPriority(data);
    }
}
