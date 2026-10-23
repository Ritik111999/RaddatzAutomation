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
        assertVisible(page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions()
                .setName("Search by tenant name, case #, or property...").setExact(true)));
        return this;
    }

    public TimeEntryPage createOwnedTimeEntry(AdminScenarioData data) {
        Locator matterSearch = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions()
                .setName("Search by tenant name, case #, or property...").setExact(true));
        matterSearch.fill(data.caseNumber());
        // The case number is unique per run; realistic tenant names alone can legitimately repeat.
        Locator ownedSuggestion = page.getByText(data.caseNumber(), new Page.GetByTextOptions().setExact(false));
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
        page.reload();
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
        assertVisible(ownedEntryRow(revisedDescription));
        return this;
    }

    public TimeEntryPage deleteOwnedTimeEntry(String ownedDescription) {
        ownedEntryRow(ownedDescription).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete time entry").setExact(true)).click();
        assertVisible(heading("Delete this time entry?"));
        button("Yes, delete").click();
        assertThat(ownedEntryRow(ownedDescription)).not().isVisible();
        return this;
    }

    public ExpensePage openExpenses() {
        page.locator("#navbarTabs").getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Billing").setExact(true)).click();
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Expenses").setExact(true)).click();
        page.waitForURL("**/expenses");
        return new ExpensePage(page).assertLoaded();
    }

    private Locator ownedEntryRow(String description) {
        // The table visually truncates long descriptions, but the application
        // retains the exact owned text in the description element's title.
        return page.getByTitle(description, new Page.GetByTitleOptions().setExact(true))
                .locator("..").locator("..");
    }
}
