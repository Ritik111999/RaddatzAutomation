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
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        page.waitForURL("**/expenses");
        assertLoaded();
        return this;
    }

    public ExpensePage deleteOwnedExpense(AdminScenarioData data) {
        Locator deleteBtn = ownedExpenseRow(data.caseNumber()).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete expense").setExact(true));
        assertVisible(deleteBtn);
        String onclick = deleteBtn.getAttribute("onclick");
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(onclick != null ? onclick : "");
        if (m.find()) {
            String expenseId = m.group();
            page.evaluate("(id) => {" +
                    "return fetch('/expenses/' + id + '/delete', {" +
                    "  method: 'POST'," +
                    "  headers: {" +
                    "    'X-CSRF-TOKEN': document.querySelector('meta[name=\"csrf-token\"]')?.getAttribute('content')," +
                    "    'X-Requested-With': 'XMLHttpRequest'," +
                    "    'Accept': 'application/json'," +
                    "    'Content-Type': 'application/json'" +
                    "  }" +
                    "}).then(r => r.json());" +
                    "}", expenseId);
            page.waitForTimeout(1000);
            page.reload();
            assertLoaded();
            assertThat(ownedExpenseRow(data.caseNumber())).not().isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        }
        return this;
    }

    private Locator ownedExpenseRow(String caseNumber) {
        // Expense descriptions are intentionally clipped in the grid. The unique
        // Matter's displayed case number stays available as the row's ownership scope.
        return page.getByTitle(caseNumber, new Page.GetByTitleOptions().setExact(false))
                .locator("..").locator("..");
    }
}
