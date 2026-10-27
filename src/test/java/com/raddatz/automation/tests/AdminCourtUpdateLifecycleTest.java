package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/** Maintains an owned Court Update only from a current-run Event and Matter; no billing handoff is used. */
public final class AdminCourtUpdateLifecycleTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @TestMetadata(
            module = "CourtUpdates",
            scenarioId = "RAD-COURT-001",
            title = "Court Hearing Outcome Update Lifecycle: Create, Edit, Reopen, and Delete",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Court Update hearing outcome is saved with notes, survives reload/reopen, and deletes safely without residual record.",
            preconditions = "An owned Matter and scheduled hearing event exist.",
            testSteps = {
                    "Create parent Company, Property, Matter, and scheduled Event chain",
                    "Navigate to Court Updates and select hearing date",
                    "Open hearing row and save Court Update notes with scenario reference",
                    "Update hearing notes and reload table",
                    "Assert persisted notes match data-full-notes attribute",
                    "Delete Court Update and confirm dismissal and absence"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
    @Test(groups = "admin-mutation", description = "Administrator creates, edits, verifies, and deletes an owned Court Update from an owned calendar Event.")
    public void administratorMaintainsOwnedCourtUpdate() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        String revisedNotes = "Reviewed the return-hearing record, service affidavit, and filing status for "
                + data.caseNumber() + ". Scenario reference " + data.reference() + ".";
        new Sidebar(currentPage()).goToClients().openAddCompany().createOwnedCompany(data)
                .openAddProperty().createOwnedProperty(data);
        new Sidebar(currentPage()).goToClients().openAddMatter().createOwnedMatter(data);
        new Sidebar(currentPage()).goToDashboard().openAddEvent().createOwnedEvent(data);
        new Sidebar(currentPage()).goToCourtUpdates()
                .selectCalendarDate(data.eventDate())
                .createOwnedCourtUpdate(data)
                .updateOwnedCourtUpdate(data, revisedNotes)
                .assertOwnedCourtUpdatePersisted(revisedNotes)
                .deleteOwnedCourtUpdate(revisedNotes);
    }
}
