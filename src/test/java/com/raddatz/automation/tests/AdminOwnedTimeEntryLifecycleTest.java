package com.raddatz.automation.tests;

import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/** Creates, edits, verifies, and removes only a run-owned time entry; no payment action is used. */
public final class AdminOwnedTimeEntryLifecycleTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @Test(groups = "admin-mutation", description = "Administrator manages a billing time entry only for a newly-created owned Matter.")
    public void administratorManagesOwnedTimeEntryLifecycle() {
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
                .openTimeEntry()
                .start(config.password())
                .createOwnedTimeEntry(data)
                .updateOwnedTimeEntry(data.initialTimeEntryDescription(), data.revisedTimeEntryDescription())
                .deleteOwnedTimeEntry(data.revisedTimeEntryDescription());
    }
}
