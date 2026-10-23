package com.raddatz.automation.tests;

import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/** Builds one complete, isolated legal-work chain and never touches prior application records. */
public final class AdminTaskAndEventCreationTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @Test(groups = "admin-mutation", description = "Administrator creates an owned Task and Event below an owned Matter.")
    public void administratorCreatesOwnedTaskAndEvent() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        var clients = new Sidebar(currentPage()).goToClients();

        clients.openAddCompany()
                .createOwnedCompany(data)
                .openAddProperty()
                .createOwnedProperty(data);

        new Sidebar(currentPage()).goToClients()
                .openAddMatter()
                .createOwnedMatter(data);

        new Sidebar(currentPage()).goToDashboard()
                .openAddTask()
                .createOwnedTask(data);

        new Sidebar(currentPage()).goToTodos()
                .assertTaskListed(data.taskName());

        new Sidebar(currentPage()).goToDashboard()
                .openAddEvent()
                .createOwnedEvent(data);
    }
}
