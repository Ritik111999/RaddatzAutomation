package com.raddatz.automation.tests;

import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

public final class ReadOnlySmokeTest extends ReadOnlyTestBase {
    @Test(groups = {"admin-readonly", "smoke"}, description = "The authenticated administrator can load the dashboard without modifying application data.")
    public void administratorDashboardLoads() {
        dashboard().assertLoaded();
    }

    @Test(groups = {"admin-readonly", "smoke"}, description = "Administrator navigation loads without activating create, update, or delete controls.")
    public void administratorNavigationLoadsReadOnlyScreens() {
        Sidebar sidebar = new Sidebar(currentPage());
        sidebar.goToClients().assertLoaded();
        sidebar.goToTodos().assertLoaded();
        sidebar.goToCalendar().assertLoaded();
    }
}
