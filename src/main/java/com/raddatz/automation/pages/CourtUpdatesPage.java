package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

import java.util.regex.Pattern;

/** Court updates are limited to current-run Events and Matters; payment controls are never used. */
public final class CourtUpdatesPage extends BasePage {
    public CourtUpdatesPage(Page page) {
        super(page);
    }

    public CourtUpdatesPage assertLoaded() {
        assertVisible(heading("Court Updates"));
        assertVisible(heading("Calendar Entries"));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Matter *").setExact(true)));
        return this;
    }

    public CourtUpdatesPage selectCalendarDate(String date) {
        page.locator("#courtUpdatesCalendarDate").fill(date);
        page.waitForURL(Pattern.compile(".*calendar_date=" + Pattern.quote(date) + ".*"));
        return this;
    }

    public CourtUpdatesPage createOwnedCourtUpdate(AdminScenarioData data) {
        // Each calendar entry is a button; scope it by this run's unique hearing title.
        Locator ownedEvent = page.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText(data.eventTitle()));
        assertVisible(ownedEvent);
        ownedEvent.click();
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Notes *").setExact(true)).fill(data.ownershipNote());
        page.locator("select").filter(new Locator.FilterOptions().setHas(
                page.getByRole(AriaRole.OPTION,
                        new Page.GetByRoleOptions().setName("Active").setExact(true))))
                .selectOption("active");
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Submit Court Update.*"))).click();
        Locator ownedRow = page.getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(data.ownershipNote()));
        assertVisible(ownedRow);
        return this;
    }
}
