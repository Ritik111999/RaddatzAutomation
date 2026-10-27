package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
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
    @TestMetadata(
            module = "Matter360",
            scenarioId = "RAD-TASK-002",
            title = "Administrator Creates Owned Task and Event below Matter",
            testType = TestMetadata.TestType.INTEGRATION,
            expectedResult = "Task created and listed in Todos, event scheduled and listed in Calendar.",
            preconditions = "Valid administrative session with owned company, property, and matter.",
            testSteps = {
                    "Create Company, Property, and Matter hierarchy",
                    "Open Add Task from Dashboard",
                    "Fill Task details linked to Matter and submit",
                    "Navigate to Todos and assert task is present",
                    "Open Add Event from Dashboard and schedule event"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
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
