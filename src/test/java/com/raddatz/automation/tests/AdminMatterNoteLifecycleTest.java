package com.raddatz.automation.tests;

import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/** Exercises create, edit, verification, and scoped cleanup for an owned Matter note. */
public final class AdminMatterNoteLifecycleTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @Test(groups = "admin-mutation", description = "Administrator manages a note only on a newly-created owned Matter.")
    public void administratorManagesOwnedMatterNoteLifecycle() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        var clients = new Sidebar(currentPage()).goToClients();

        clients.openAddCompany()
                .createOwnedCompany(data)
                .openAddProperty()
                .createOwnedProperty(data);

        new Sidebar(currentPage()).goToClients()
                .openAddMatter()
                .createOwnedMatter(data)
                .openOwnedMatter(data.tenantName(), data.caseNumber())
                .openNotes()
                .createOwnedNote(data.initialMatterNote())
                .updateOwnedNote(data.initialMatterNote(), data.revisedMatterNote())
                .deleteOwnedNote(data.revisedMatterNote());
    }
}
