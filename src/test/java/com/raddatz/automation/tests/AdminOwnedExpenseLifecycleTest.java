package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
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

    @TestMetadata(
            module = "FinancialPipeline",
            scenarioId = "RAD-EXPN-001",
            title = "Case Disbursement Expense Lifecycle: Create, Edit, Reopen Persistence, and Deletion",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Case disbursement expense ($75.00) is saved, survives reload and reopen with revised description, and deletes safely.",
            preconditions = "An owned Matter case and authenticated billing session exist.",
            testSteps = {
                    "Create parent Company, Property, and Matter chain",
                    "Open Matter Time Entry and authenticate billing passkey",
                    "Open Expenses view and record $75.00 expense with ownership description",
                    "Update expense description, reload table, and reopen modal to verify persisted text",
                    "Delete owned expense and confirm table absence"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
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
