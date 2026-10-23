package com.raddatz.automation.tests;

import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.OwnedHierarchyEditorPage;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/** Verifies persisted Company/Property/Matter edits on a fresh owned hierarchy. */
public final class AdminOwnedHierarchyEditTest extends ReadOnlyTestBase {
    @Override protected boolean requiresMutationPermission() { return true; }

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
