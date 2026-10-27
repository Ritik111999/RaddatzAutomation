package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

public final class ReadOnlySmokeTest extends ReadOnlyTestBase {

    @TestMetadata(
            module = "Preflight",
            scenarioId = "RAD-SMK-001",
            title = "Administrative Session Initialization and Dashboard Loading",
            testType = TestMetadata.TestType.READ_ONLY,
            expectedResult = "Dashboard loads with navigation menus, metrics cards, and recent activity tables visible.",
            preconditions = "Valid administrator credentials.",
            testSteps = {
                    "Log in as administrator",
                    "Assert redirect to /dashboard",
                    "Verify dashboard layout elements and quick navigation affordances"
            },
            defectSeverity = TestMetadata.DefectSeverity.CRITICAL
    )
    @Test(groups = {"admin-readonly", "smoke"}, description = "The authenticated administrator can load the dashboard without modifying application data.")
    public void administratorDashboardLoads() {
        dashboard().assertLoaded();
    }

    @TestMetadata(
            module = "Preflight",
            scenarioId = "RAD-SMK-002",
            title = "Read-Only Core Navigation Across Key Hubs",
            testType = TestMetadata.TestType.READ_ONLY,
            expectedResult = "Clients directory, central To-dos, and Calendar screens render without throwing runtime errors or modifying application data.",
            preconditions = "Active administrative session.",
            testSteps = {
                    "Navigate to Clients directory and assert page loaded",
                    "Navigate to To-dos directory and assert page loaded",
                    "Navigate to Calendar view and assert calendar grid loaded"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
    @Test(groups = {"admin-readonly", "smoke"}, description = "Administrator navigation loads without activating create, update, or delete controls.")
    public void administratorNavigationLoadsReadOnlyScreens() {
        Sidebar sidebar = new Sidebar(currentPage());
        sidebar.goToClients().assertLoaded();
        sidebar.goToTodos().assertLoaded();
        sidebar.goToCalendar().assertLoaded();
    }
}

