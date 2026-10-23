package com.raddatz.automation.tests;

import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/** Creates a Court Update only from a current-run Event and Matter; no payment action is used. */
public final class AdminCourtUpdateLifecycleTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @Test(groups = "admin-mutation", description = "Administrator creates an owned Court Update from an owned calendar Event.")
    public void administratorCreatesOwnedCourtUpdate() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        new Sidebar(currentPage()).goToClients().openAddCompany().createOwnedCompany(data)
                .openAddProperty().createOwnedProperty(data);
        new Sidebar(currentPage()).goToClients().openAddMatter().createOwnedMatter(data);
        new Sidebar(currentPage()).goToDashboard().openAddEvent().createOwnedEvent(data);
        new Sidebar(currentPage()).goToCourtUpdates()
                .selectCalendarDate(data.eventDate())
                .createOwnedCourtUpdate(data);
    }
}
