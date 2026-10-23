package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

/** Entry point for the tabbed workspace of a scenario-owned Matter. */
public final class MatterWorkspacePage extends BasePage {
    public MatterWorkspacePage(Page page) {
        super(page);
    }

    public MatterWorkspacePage assertLoaded(String tenantName) {
        assertVisible(heading(tenantName));
        assertVisible(notesLink());
        return this;
    }

    public MatterNotesPage openNotes() {
        notesLink().click();
        page.waitForURL("**/notes");
        return new MatterNotesPage(page).assertLoaded();
    }

    public MatterDocumentsPage openDocuments() {
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("Documents$")).setExact(false)).click();
        page.waitForURL("**/documents");
        return new MatterDocumentsPage(page).assertLoaded();
    }

    public BillingSessionPage openTimeEntry() {
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("Add Time Entry$")).setExact(false)).click();
        page.waitForURL("**/confirm-sensitive-access/billing");
        return new BillingSessionPage(page).assertLoaded();
    }


    private com.microsoft.playwright.Locator notesLink() {
        // The UI prepends a presentational icon to the accessible link name.
        return page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("Notes$")).setExact(false));
    }
}
