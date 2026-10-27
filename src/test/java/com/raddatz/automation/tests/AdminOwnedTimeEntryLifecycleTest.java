package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
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

    @TestMetadata(
            module = "FinancialPipeline",
            scenarioId = "RAD-TIME-001",
            title = "Attorney Billable Time Entry Lifecycle: Create, Edit, Tooltip Verification, and Deletion",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Billable time entry is recorded, identified by exact tooltip description, persists description update, and deletes cleanly.",
            preconditions = "An owned Matter case and authenticated billing session exist.",
            testSteps = {
                    "Create parent Company, Property, and Matter chain",
                    "Open Matter Time Entry view and verify billing passkey",
                    "Record 1.50 billable hours with scenario ownership description",
                    "Locate owned row via exact title tooltip and edit description",
                    "Delete revised time entry and assert table is empty of scenario record"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
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
