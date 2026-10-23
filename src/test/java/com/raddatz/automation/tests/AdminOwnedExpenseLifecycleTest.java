package com.raddatz.automation.tests;

import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.time.Clock;

/** Maintains one owned expense attached to a fresh Matter; receipt/export/payment features are deliberately excluded. */
public final class AdminOwnedExpenseLifecycleTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @Test(groups = "admin-mutation", description = "Administrator manages an owned expense only for a newly-created owned Matter.")
    public void administratorMaintainsOwnedExpenseLifecycle() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        new Sidebar(currentPage()).goToClients().openAddCompany().createOwnedCompany(data)
                .openAddProperty().createOwnedProperty(data);
        new Sidebar(currentPage()).goToClients().openAddMatter().createOwnedMatter(data)
                .openOwnedMatter(data.tenantName(), data.caseNumber())
                .openTimeEntry()
                .start(config.password())
                .openExpenses()
                .createOwnedExpense(data)
                .updateOwnedExpense(data)
                .deleteOwnedExpense(data);
    }
}
