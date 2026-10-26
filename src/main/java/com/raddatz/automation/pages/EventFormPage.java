package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

import java.util.regex.Pattern;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Calendar event form; notification, SMS, invite, recurrence, and upload controls remain off. */
public final class EventFormPage extends BasePage {
    public EventFormPage(Page page) {
        super(page);
    }

    public EventFormPage assertLoaded() {
        assertVisible(heading("Add Event"));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Search by tenant name or case type…").setExact(true)));
        return this;
    }

    public EventFormPage assertEditLoaded() {
        assertVisible(heading("Edit Event"));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Case number Property v. tenant, event type, courtroom").setExact(true)));
        return this;
    }

    /** Reads the Add Event form's native validation attributes without selecting a Matter or saving. */
    public Map<String, ObservedFieldConstraint> observeValidationConstraints() {
        Map<String, ObservedFieldConstraint> constraints = new LinkedHashMap<>();
        constraints.put("matterSearch", observeConstraint("matterSearch", page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Search by tenant name or case type…").setExact(true))));
        constraints.put("courtRoom", observeConstraint("courtRoom", page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Court Room").setExact(true))));
        constraints.put("judgeName", observeConstraint("judgeName", page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter judge name").setExact(true))));
        constraints.put("eventTitle", observeConstraint("eventTitle", page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Case number Property v. tenant, event type, courtroom").setExact(true))));
        constraints.put("eventDate", observeConstraint("eventDate",
                page.locator("input[name='court_dates[0][event_date]']")));
        constraints.put("startTime", observeConstraint("startTime", page.locator("input[type='time']")));
        constraints.put("duration", observeConstraint("duration", page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("01:00").setExact(true))));
        return Map.copyOf(constraints);
    }

    public CalendarPage createOwnedEvent(AdminScenarioData data) {
        Locator matterSearch = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Search by tenant name or case type…").setExact(true));
        // The case number is unique to this run. Tenant names are intentionally realistic and can recur.
        matterSearch.fill(data.caseNumber());
        Locator ownedMatter = page.getByText(data.tenantName(), new Page.GetByTextOptions().setExact(true))
                .locator("..");
        assertVisible(ownedMatter);
        ownedMatter.click();
        assertThat(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Auto-populates when case is selected").setExact(true)))
                .hasValue(data.propertyName());

        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Court Room").setExact(true)).fill(data.eventCourtRoom());
        page.locator("select").filter(new Locator.FilterOptions().setHas(
                page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(data.eventType()).setExact(true))))
                .selectOption(data.eventType());
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter judge name").setExact(true)).fill(data.eventJudgeName());
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Case number Property v. tenant, event type, courtroom").setExact(true))
                .fill(data.eventTitle());
        page.locator("select").filter(new Locator.FilterOptions().setHas(
                page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(data.eventCategory()).setExact(true))))
                .selectOption(data.eventCategory());
        page.locator("input[name='court_dates[0][event_date]']").fill(data.eventDate());
        page.locator("input[type='time']").fill(data.eventStartTime());
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("01:00").setExact(true)).fill(data.eventDuration());
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(
                "Add preparation notes, case context, special instructions, or any other relevant information about this court date..."
        ).setExact(true)).fill(data.eventNotes());
        button("Save & Close").click();
        return new CalendarPage(page).assertLoaded();
    }

    /** Updates only title and case-preparation notes; it never changes notification or document controls. */
    public EventDetailsPage updateOwnedEvent(AdminScenarioData data) {
        assertEditLoaded();
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Case number Property v. tenant, event type, courtroom").setExact(true))
                .fill(data.revisedEventTitle());
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(
                "Add preparation notes, case context, special instructions, or any other relevant information about this court date..."
        ).setExact(true)).fill(data.revisedEventNotes());
        button("Update Event").click();
        page.waitForURL(Pattern.compile(".*/calendar/\\d+$"));
        return new EventDetailsPage(page).assertLoaded(data.revisedEventTitle());
    }
}
