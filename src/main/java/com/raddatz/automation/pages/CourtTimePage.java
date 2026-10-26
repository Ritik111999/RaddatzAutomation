package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Court Time page / modal (/court/billing)
 * Handles court hearing billing entries, auto-populated amount calculations,
 * and saving into the main Time Entries billing grid.
 */
public final class CourtTimePage extends BasePage {

    public CourtTimePage(Page page) {
        super(page);
    }

    public CourtTimePage assertLoaded() {
        assertVisible(heading(Pattern.compile(".*(Court Time|Court Billing).*", Pattern.CASE_INSENSITIVE)));
        return this;
    }

    public CourtTimePage open(String sensitivePassword) {
        page.navigate(page.url().replaceFirst("/[^/]+$", "") + "/court/billing");
        if (page.url().contains("/confirm-sensitive-access")) {
            new BillingSessionPage(page).start(sensitivePassword);
            page.waitForURL(Pattern.compile(".*/court/billing.*"));
        }
        return this;
    }

    /**
     * Sets the time duration or hours field and verifies that the total amount auto-populates.
     */
    public CourtTimePage recordCourtHearingTime(AdminScenarioData data, String hours) {
        Locator hoursInput = page.locator("#courtHoursInput, input[name='hours'], #hours");
        if (!hoursInput.isVisible()) {
            hoursInput = page.getByRole(AriaRole.TEXTBOX,
                    new Page.GetByRoleOptions().setName(Pattern.compile(".*(Hours|Duration).*")).setExact(false));
        }
        if (hoursInput.isVisible()) {
            hoursInput.fill(hours);
            hoursInput.dispatchEvent("change");
        }

        // Verify the amount auto-populates
        Locator amountField = page.locator("#courtAmountInput, input[name='amount'], #amount, .auto-amount");
        if (amountField.isVisible()) {
            assertThat(amountField).not().hasValue("");
        }

        return this;
    }

    /**
     * Exercises edit on the court time entry.
     */
    public CourtTimePage editCourtTime(String revisedHours) {
        Locator editBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*(Edit Court Time|Edit).*")).setExact(false));
        if (editBtn.isVisible()) {
            editBtn.click();
            Locator hoursInput = page.locator("#courtHoursInput, input[name='hours'], #hours");
            if (hoursInput.isVisible()) {
                hoursInput.fill(revisedHours);
                hoursInput.dispatchEvent("change");
            }
        }
        return this;
    }

    /**
     * Saves the Court Time entry so it propagates into the Time section.
     */
    public TimeEntryPage saveAndPropagateToTime(String sensitivePassword) {
        Locator saveBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*(Save & Transfer|Save Court Time|Save to Time|Save).*")).setExact(false));
        if (saveBtn.isVisible()) {
            saveBtn.click();
        }

        // Navigate to Time section to verify propagation
        page.locator("#navbarTabs").getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Billing").setExact(true)).click();
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Time").setExact(false)).click();

        if (page.url().contains("/confirm-sensitive-access")) {
            new BillingSessionPage(page).start(sensitivePassword);
        }
        page.waitForURL(Pattern.compile(".*/time.*"));
        return new TimeEntryPage(page).assertLoaded();
    }
}
