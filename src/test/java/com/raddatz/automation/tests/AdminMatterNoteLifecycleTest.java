package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
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

    @TestMetadata(
            module = "Matter360",
            scenarioId = "RAD-NOTE-001",
            title = "Matter Case File Notes CRUD and Audit Timestamping",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Matter note is created with timestamp, edits survive page reload, and note is removed cleanly.",
            preconditions = "An owned Matter case exists.",
            testSteps = {
                    "Create parent Company, Property, and Matter chain",
                    "Open Matter Workspace and navigate to Notes tab",
                    "Add new note with scenario ownership text",
                    "Edit note content with revised audit text and verify in-place update",
                    "Delete note and assert absence from Matter notes history"
            },
            defectSeverity = TestMetadata.DefectSeverity.MEDIUM
    )
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
