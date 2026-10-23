package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

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
        page.locator("#case_status").selectOption("active");
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Submit Court Update.*"))).click();
        assertVisible(ownedCourtUpdateRow(data.ownershipNote()));
        return this;
    }

    /** Updates and proves persistence only for a notes-scoped Court Update owned by this run. */
    public CourtUpdatesPage updateOwnedCourtUpdate(AdminScenarioData data, String revisedNotes) {
        ownedCourtUpdateRow(data.ownershipNote()).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Edit court update").setExact(true)).click();
        Locator notes = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Notes *").setExact(true));
        assertThat(notes).hasValue(data.ownershipNote());
        notes.fill(revisedNotes);
        page.locator("#case_status").selectOption("active");
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Update Court Update.*"))).click();
        assertVisible(ownedCourtUpdateRow(revisedNotes));
        page.reload();
        assertLoaded();
        assertVisible(ownedCourtUpdateRow(revisedNotes));
        return this;
    }

    /** Reopens the owned update after reload and cancels, proving note/status persistence without another mutation. */
    public CourtUpdatesPage assertOwnedCourtUpdatePersisted(String revisedNotes) {
        ownedCourtUpdateRow(revisedNotes).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Edit court update").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Notes *").setExact(true))).hasValue(revisedNotes);
        assertThat(page.locator("#case_status")).hasValue("active");
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Cancel Edit").setExact(true)).click();
        return this;
    }

    /** Deletes only the exact notes-scoped Court Update created by this scenario. */
    public CourtUpdatesPage deleteOwnedCourtUpdate(String revisedNotes) {
        ownedCourtUpdateRow(revisedNotes).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete court update").setExact(true)).click();
        assertVisible(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Delete this court update?").setExact(true)));
        button("Yes, delete").click();
        assertThat(ownedCourtUpdateRow(revisedNotes)).not().isVisible();
        page.reload();
        assertLoaded();
        assertThat(ownedCourtUpdateRow(revisedNotes)).not().isVisible();
        return this;
    }

    /** Verifies the selected Event has propagated to Court Updates before opening its own detail route. */
    public EventDetailsPage openOwnedEventDetails(String eventTitle) {
        Locator ownedEvent = page.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText(eventTitle));
        assertVisible(ownedEvent);
        ownedEvent.click();
        Locator viewEvent = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("View").setExact(true));
        assertVisible(viewEvent);
        String eventDetailsUrl = viewEvent.getAttribute("href");
        if (eventDetailsUrl == null || eventDetailsUrl.isBlank()) {
            throw new IllegalStateException("The selected owned Event did not expose a detail route.");
        }
        page.navigate(eventDetailsUrl);
        page.waitForURL(Pattern.compile(".*/calendar/\\d+$"));
        return new EventDetailsPage(page).assertLoaded(eventTitle);
    }

    /** Confirms an owned Event is no longer exposed on the selected Court Updates date. */
    public CourtUpdatesPage assertEventAbsent(String eventTitle) {
        assertThat(page.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText(eventTitle))).not().isVisible();
        return this;
    }

    private Locator ownedCourtUpdateRow(String notes) {
        // The grid clips long notes. Its preview button retains the exact full
        // value, including the run reference, in this observed attribute.
        String escaped = notes.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\a ").replace("\r", "\\d ");
        return page.locator("button[data-full-notes=\"" + escaped + "\"]")
                .locator("..").locator("..");
    }
}
