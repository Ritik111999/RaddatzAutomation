package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
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

    @TestMetadata(
            module = "Matter360",
            scenarioId = "RAD-EVNT-001",
            title = "Matter Calendar Event Lifecycle and Court Updates Synchronization",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Calendar event appears on monthly/weekly calendar, reflects on Court Updates hearing list, persists title edits, and deletes cleanly across both views.",
            preconditions = "An owned Matter case exists.",
            testSteps = {
                    "Create parent Company, Property, and Matter chain",
                    "Open Add Event modal and schedule hearing event linked to Matter",
                    "Assert event card on Calendar with case number and tenant details",
                    "Navigate to Court Updates and verify event appears for scheduled date",
                    "Update event title and notes, verify sync on Calendar",
                    "Delete event from Court Updates and confirm absence from both Calendar and Court Updates"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
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
