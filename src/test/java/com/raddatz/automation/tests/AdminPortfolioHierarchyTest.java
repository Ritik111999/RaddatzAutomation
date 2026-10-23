package com.raddatz.automation.tests;

import com.raddatz.automation.data.AdminScenarioFamily;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/**
 * Proves the portfolio relationship through the actual Add Property and Add New
 * Matter entry points. It creates one Company only; no existing record is used.
 */
public final class AdminPortfolioHierarchyTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @Test(groups = "admin-mutation", description = "Administrator creates two Properties, three Matters, a Task, and an Event under one owned Company.")
    public void administratorBuildsOwnedPortfolioHierarchy() {
        AdminScenarioFamily family = AdminScenarioFamily.create(Clock.systemUTC());
        var first = family.companyAndFirstProperty();
        var secondProperty = family.secondPropertyAndMatter();
        var secondMatter = family.secondMatterAtFirstProperty();

        var clients = new Sidebar(currentPage()).goToClients();
        clients.openAddCompany()
                .createOwnedCompany(first);

        new Sidebar(currentPage()).goToClients()
                .openAddProperty()
                .createOwnedProperty(first);
        new Sidebar(currentPage()).goToClients()
                .openAddProperty()
                .createOwnedProperty(secondProperty);

        new Sidebar(currentPage()).goToClients()
                .openAddMatter()
                .createOwnedMatter(first)
                .assertOwnedMatterHierarchy(first);
        new Sidebar(currentPage()).goToClients()
                .openAddMatter()
                .createOwnedMatter(secondProperty)
                .assertOwnedMatterHierarchy(secondProperty);
        new Sidebar(currentPage()).goToClients()
                .openAddMatter()
                .createOwnedMatter(secondMatter)
                .assertOwnedMatterHierarchy(secondMatter);

        new Sidebar(currentPage()).goToDashboard()
                .openAddTask()
                .createOwnedTask(first);
        new Sidebar(currentPage()).goToTodos()
                .assertTaskListed(first.taskName());
        new Sidebar(currentPage()).goToDashboard()
                .openAddEvent()
                .createOwnedEvent(first);
    }
}
