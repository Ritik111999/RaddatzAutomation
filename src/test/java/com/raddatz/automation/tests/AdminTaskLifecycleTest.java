package com.raddatz.automation.tests;

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
