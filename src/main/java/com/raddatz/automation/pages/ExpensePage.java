package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import com.raddatz.automation.data.AdminScenarioData;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Permitted owned-expense lifecycle; receipt, export, and Expense Code administration remain unused. */
public final class ExpensePage extends BasePage {
    private static final String CONFIGURED_EXPENSE_CODE = "JFGTJ - Expense Code TEST Description";

    public ExpensePage(Page page) {
        super(page);
    }

    public ExpensePage assertLoaded() {
        assertVisible(heading("Expense Search & Management"));
        assertVisible(page.locator("#expenseMatterSearch"));
        return this;
    }

    public ExpensePage createOwnedExpense(AdminScenarioData data) {
        Locator matterSearch = page.locator("#expenseMatterSearch");
        matterSearch.fill(data.caseNumber());
        Locator ownedSuggestion = page.getByText(data.caseNumber(), new Page.GetByTextOptions().setExact(false));
        assertVisible(ownedSuggestion);
        ownedSuggestion.click();
        assertThat(page.locator("#expenseMatterIdInput")).not().hasValue("");

        page.locator("#expenseUserId").selectOption(new SelectOption().setLabel("Mark Lee"));
        page.locator("#expenseDateInput").fill(data.openingDate());
        // The configured expense code is reference/master data. This test neither creates nor changes it.
        page.locator("#expenseCodeSelect").selectOption(new SelectOption().setLabel(CONFIGURED_EXPENSE_CODE));
        page.locator("#descriptionInput").fill(data.initialExpenseDescription());
        page.locator("#quantityInput").fill("2.00");
        page.locator("#pricePerUnitInput").fill("37.50");
        page.locator("#saveExpenseBtn").click();
        page.reload();
        assertLoaded();
        assertVisible(ownedExpenseRow(data.caseNumber()));
        return this;
    }

    public ExpensePage updateOwnedExpense(AdminScenarioData data) {
        ownedExpenseRow(data.caseNumber()).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Edit expense").setExact(true)).click();
        Locator dialog = page.locator("#editModal");
        assertVisible(dialog);
        assertThat(dialog.locator("#edit_description")).hasValue(data.initialExpenseDescription());
        dialog.locator("#edit_description").fill(data.revisedExpenseDescription());
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile(".*Update Expense"))).click();
        // The local build may retain the edit modal after a successful update.
        // Escape is its normal keyboard dismissal; close it before reloading
        // the server-backed list rather than assuming a presentation side effect.
        page.keyboard().press("Escape");
        assertThat(dialog).not().isVisible();
        page.reload();
        assertLoaded();
        ownedExpenseRow(data.caseNumber()).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Edit expense").setExact(true)).click();
        assertThat(page.locator("#editModal").locator("#edit_description")).hasValue(data.revisedExpenseDescription());
        page.locator("#editModal").getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Close").setExact(true)).click();
        // In the current local build the visible header close button does not
        // dismiss this Bootstrap modal after a reopened edit. Escape is the
        // normal keyboard dismissal and keeps the subsequent delete scoped to
        // the owned row without bypassing the modal.
        page.keyboard().press("Escape");
        assertThat(page.locator("#editModal")).not().isVisible();
        return this;
    }

    public ExpensePage deleteOwnedExpense(AdminScenarioData data) {
        ownedExpenseRow(data.caseNumber()).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete expense").setExact(true)).click();
        assertVisible(heading("Delete this expense entry?"));
        button("Yes, delete").click();
        assertThat(ownedExpenseRow(data.caseNumber())).not().isVisible();
        return this;
    }

    private Locator ownedExpenseRow(String caseNumber) {
        // Expense descriptions are intentionally clipped in the grid. The unique
        // Matter's displayed case number stays available as the row's ownership scope.
        return page.getByTitle(caseNumber, new Page.GetByTitleOptions().setExact(false))
                .locator("..").locator("..");
    }
}
