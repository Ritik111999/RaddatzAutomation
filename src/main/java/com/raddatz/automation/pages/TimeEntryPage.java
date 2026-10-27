package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Permitted billing-entry lifecycle, always scoped to the run-specific description. */
public final class TimeEntryPage extends BasePage {
    public TimeEntryPage(Page page) {
        super(page);
    }

    public TimeEntryPage assertLoaded() {
        assertVisible(heading("Time Entry Analytics"));
        assertVisible(matterSearchInput());
        return this;
    }

    public TimeEntryPage createOwnedTimeEntry(AdminScenarioData data) {
        Locator matterSearch = matterSearchInput();
        assertVisible(matterSearch);
        matterSearch.fill(data.tenantName());
        page.waitForTimeout(500);
        Locator ownedSuggestion = page.getByText(data.tenantName(), new Page.GetByTextOptions().setExact(false)).first();
        if (!ownedSuggestion.isVisible()) {
            matterSearch.fill(data.caseNumber());
            page.waitForTimeout(500);
            ownedSuggestion = page.getByText(data.caseNumber(), new Page.GetByTextOptions().setExact(false)).first();
        }
        assertVisible(ownedSuggestion);
        ownedSuggestion.click();
        // These explicit application ids are required because the spin buttons lack labels.
        page.locator("#hoursInput").fill("1.50");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions()
                .setName("Work performed — shows on the bill").setExact(true)).fill(data.initialTimeEntryDescription());
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save & New").setExact(false)).click();
        assertVisible(page.getByText("Time entry saved successfully!", new Page.GetByTextOptions().setExact(true)));
        // The entry table is populated by a separate request after the save redirect.
        page.reload(new Page.ReloadOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
        assertLoaded();
        assertVisible(ownedEntryRow(data.initialTimeEntryDescription()));
        return this;
    }

    public TimeEntryPage updateOwnedTimeEntry(String originalDescription, String revisedDescription) {
        ownedEntryRow(originalDescription).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Edit time entry").setExact(true)).click();
        Locator dialog = page.getByRole(AriaRole.DIALOG);
        assertVisible(dialog.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Edit Time Entry").setExact(false)));
        dialog.locator("#edit_description").fill(revisedDescription);
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Update Entry").setExact(false)).click();
        assertVisible(page.getByText("Time entry updated successfully!", new Page.GetByTextOptions().setExact(true)));
        // The table is refreshed from the server to prove this is not merely
        // the optimistic modal state retained by the client.
        page.reload(new Page.ReloadOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
        assertLoaded();
        assertVisible(ownedEntryRow(revisedDescription));
        return this;
    }

    public TimeEntryPage deleteOwnedTimeEntry(String ownedDescription) {
        ownedEntryRow(ownedDescription).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete time entry").setExact(true)).click();
        assertVisible(heading("Delete this time entry?"));
        button("Yes, delete").click();
        assertThat(ownedEntryRow(ownedDescription)).not().isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        return this;
    }

    private Locator matterSearchInput() {
        return page.locator("input[placeholder*='Search by tenant name']").first();
    }

    public ExpensePage openExpenses() {
        try {
            page.locator("#navbarTabs").getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("Billing").setExact(true)).click();
            page.getByRole(AriaRole.LINK,
                    new Page.GetByRoleOptions().setName("Expenses").setExact(true)).click();
            page.waitForURL("**/expenses");
        } catch (Exception e) {
            page.navigate(page.url().replaceAll("(?<=:\\d{1,5})/.*", "/expenses"));
        }
        return new ExpensePage(page).assertLoaded();
    }

    public TimeEntryPage assertPropagatedTimeEntry(String description) {
        assertVisible(ownedEntryRow(description));
        return this;
    }

    public Locator ownedEntryRow(String description) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d{8}-\\d+").matcher(description);
        if (m.find()) {
            String ref = m.group();
            return page.locator("tr").filter(new Locator.FilterOptions().setHas(
                    page.locator("[title*='" + ref + "'], td:has-text('" + ref + "')"))).first();
        }
        String snippet = description.length() > 25 ? description.substring(0, 25) : description;
        return page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(snippet)).first();
    }
}
