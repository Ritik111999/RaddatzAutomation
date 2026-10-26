package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/** Local re-authentication gate for client-approved, owned-data billing entry work. */
public final class BillingSessionPage extends BasePage {
    public BillingSessionPage(Page page) {
        super(page);
    }

    public BillingSessionPage assertLoaded() {
        if (isBillingSessionActive()) {
            return this;
        }
        assertVisible(heading("Enter Billing Mode"));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Password").setExact(true)));
        return this;
    }

    public TimeEntryPage start(String password) {
        if (!isBillingSessionActive()) {
            com.microsoft.playwright.Locator pwdInput = page.getByRole(AriaRole.TEXTBOX,
                    new Page.GetByRoleOptions().setName("Password").setExact(true));
            if (!pwdInput.isVisible()) {
                pwdInput = page.locator("input[type='password'], input[name='password']");
            }
            if (pwdInput.isVisible()) {
                pwdInput.fill(password);
                page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile(".*(Start|Confirm|Submit|Access).*")).setExact(false)).click();
                page.waitForCondition(() -> !page.url().contains("/confirm-sensitive-access"));
            }
        }
        return new TimeEntryPage(page);
    }

    private boolean isBillingSessionActive() {
        return !page.url().contains("/confirm-sensitive-access");
    }
}
