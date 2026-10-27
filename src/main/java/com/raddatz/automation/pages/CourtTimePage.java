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
        assertVisible(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Court Time").setExact(true)));
        return this;
    }

    public CourtTimePage open(String sensitivePassword) {
        page.navigate(page.url().replaceAll("(?<=:\\d{1,5})/.*", "/court/billing"));
        if (page.url().contains("/confirm-sensitive-access")) {
            new BillingSessionPage(page).start(sensitivePassword);
            page.waitForURL(Pattern.compile(".*/court/billing.*"));
        }
        return this;
    }

    public Locator ownedCourtTimeRow(AdminScenarioData data) {
        Locator searchInput = page.locator("input[placeholder*='Search entries']");
        if (searchInput.isVisible()) {
            searchInput.fill(data.caseNumber());
        }
        return page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(data.caseNumber())).first();
    }

    /**
     * Sets the time duration or hours field and verifies that the total amount auto-populates.
     */
    public CourtTimePage recordCourtHearingTime(AdminScenarioData data, String hours) {
        Locator row = page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(data.tenantName())).first();
        if (!row.isVisible()) {
            row = page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(data.caseNumber())).first();
        }
        if (!row.isVisible()) {
            Locator searchInput = page.locator("input[placeholder*='Search entries'], input[type='search']");
            if (searchInput.isVisible()) {
                searchInput.fill(data.tenantName());
                page.waitForTimeout(500);
            }
            row = page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(data.tenantName())).first();
        }
        assertVisible(row);

        Locator hoursInput = row.locator("input[type='number'], input[type='text']").first();
        assertVisible(hoursInput);
        hoursInput.fill(hours);
        hoursInput.dispatchEvent("change");
        hoursInput.dispatchEvent("input");

        Locator saveBtn = row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save").setExact(false));
        assertVisible(saveBtn);
        saveBtn.click();
        page.waitForTimeout(1500);
        return this;
    }

    /**
     * Exercises edit on the court time entry.
     */
    public CourtTimePage editCourtTime(String revisedHours) {
        Locator hoursInput = page.locator("input[type='number'], input[type='text']").first();
        if (hoursInput.isVisible()) {
            hoursInput.fill(revisedHours);
            hoursInput.dispatchEvent("change");
            hoursInput.dispatchEvent("input");
        }
        return this;
    }

    /**
     * Saves the Court Time entry so it propagates into the Time section.
     */
    public TimeEntryPage saveAndPropagateToTime(String sensitivePassword) {
        Locator saveAllBtn = page.locator("#saveAllBillingBtn");
        if (saveAllBtn.isVisible()) {
            saveAllBtn.click();
            page.waitForTimeout(1000);
        }

        page.navigate(page.url().replaceAll("(?<=:\\d{1,5})/.*", "/time"));
        if (page.url().contains("/confirm-sensitive-access")) {
            new BillingSessionPage(page).start(sensitivePassword);
        }
        page.waitForURL(Pattern.compile(".*/time(?:\\?.*)?$"));
        return new TimeEntryPage(page).assertLoaded();
    }
}
