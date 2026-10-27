package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/** Creates, edits, verifies, and removes only a Task owned by this scenario run. */
public final class AdminTaskLifecycleTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @TestMetadata(
            module = "Matter360",
            scenarioId = "RAD-TASK-001",
            title = "Matter Task Lifecycle: Create, Edit, Verify, and Delete",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Matter task is created, appears in central /todos directory, accepts title/description updates, and deletes cleanly without residual data.",
            preconditions = "An owned Matter case exists.",
            testSteps = {
                    "Create parent Company, Property, and Matter chain",
                    "Open Add Task modal and create owned task linked to Matter",
                    "Navigate to /todos central directory and assert task is listed",
                    "Edit task title and description",
                    "Delete revised task and verify absence from directory"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
    @Test(groups = "admin-mutation", description = "Administrator maintains an owned Matter task without touching existing tasks.")
    public void administratorMaintainsOwnedTask() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        new Sidebar(currentPage()).goToClients().openAddCompany().createOwnedCompany(data)
                .openAddProperty().createOwnedProperty(data);
        new Sidebar(currentPage()).goToClients().openAddMatter().createOwnedMatter(data);
        new Sidebar(currentPage()).goToDashboard().openAddTask().createOwnedTask(data);
        new Sidebar(currentPage()).goToTodos()
                .assertTaskListed(data.taskName())
                .editOwnedTask(data.taskName(), data.revisedTaskName(), data.revisedTaskDescription())
                .deleteOwnedTask(data.revisedTaskName());
    }
}
