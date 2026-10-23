package com.raddatz.automation.tests;

import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/** Creates, proves Court Updates propagation, edits, then removes only a current-run Event. */
public final class AdminEventLifecycleTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @Test(groups = "admin-mutation", description = "Administrator maintains an owned calendar Event and its Court Updates propagation.")
    public void administratorMaintainsOwnedEvent() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        new Sidebar(currentPage()).goToClients().openAddCompany().createOwnedCompany(data)
                .openAddProperty().createOwnedProperty(data);
        new Sidebar(currentPage()).goToClients().openAddMatter().createOwnedMatter(data);
        new Sidebar(currentPage()).goToDashboard().openAddEvent().createOwnedEvent(data);
        new Sidebar(currentPage()).goToCalendar().assertOwnedEventVisible(data, false);

        new Sidebar(currentPage()).goToCourtUpdates()
                .selectCalendarDate(data.eventDate())
                .openOwnedEventDetails(data.eventTitle())
                .openEditor()
                .updateOwnedEvent(data);
        new Sidebar(currentPage()).goToCalendar().assertOwnedEventVisible(data, true);
        new Sidebar(currentPage()).goToCourtUpdates().selectCalendarDate(data.eventDate())
                .openOwnedEventDetails(data.revisedEventTitle()).deleteOwnedEvent(data.revisedEventTitle());

        new Sidebar(currentPage()).goToCourtUpdates()
                .selectCalendarDate(data.eventDate())
                .assertEventAbsent(data.revisedEventTitle());
        new Sidebar(currentPage()).goToCalendar().assertOwnedEventAbsent(data);
    }
}
