package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/** Local re-authentication gate for client-approved, owned-data billing entry work. */
public final class BillingSessionPage extends BasePage {
    public BillingSessionPage(Page page) {
        super(page);
    }

    public BillingSessionPage assertLoaded() {
        assertVisible(heading("Enter Billing Mode"));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Password").setExact(true)));
        return this;
    }

    public TimeEntryPage start(String password) {
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Password").setExact(true)).fill(password);
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Start Billing Session").setExact(false)).click();
        page.waitForURL("**/time?matter_id=*");
        return new TimeEntryPage(page).assertLoaded();
    }
}
