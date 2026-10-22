package com.raddatz.automation.tests;

import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

public final class ReadOnlySmokeTest extends ReadOnlyTestBase {
    @Test(groups = {"readonly", "smoke"}, description = "Administrator can load the dashboard without modifying application data.")
    public void administratorCanOpenDashboard() {
        signIn().assertLoaded();
    }

    @Test(groups = {"readonly", "smoke"}, description = "Primary navigation routes load without activating create, update, or delete controls.")
    public void primaryNavigationLoadsReadOnlyScreens() {
        signIn();
        Sidebar sidebar = new Sidebar(currentPage());
        sidebar.goToClients().assertLoaded();
        sidebar.goToTodos().assertLoaded();
        sidebar.goToCalendar().assertLoaded();
    }
}
