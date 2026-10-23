package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Read/edit/delete boundary for an Event already scoped by a current-run title. */
public final class EventDetailsPage extends BasePage {
    public EventDetailsPage(Page page) {
        super(page);
    }

    public EventDetailsPage assertLoaded(String expectedTitle) {
        // The detail page also has a section heading named Event Details; the h1 is the route identity.
        assertVisible(page.locator("h1").filter(new com.microsoft.playwright.Locator.FilterOptions()
                .setHasText("Event Details")));
        assertThat(page.locator("textarea[readonly]").filter(new com.microsoft.playwright.Locator.FilterOptions()
                .setHasText(expectedTitle))).hasValue(expectedTitle);
        return this;
    }

    public EventFormPage openEditor() {
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Edit Event")).setExact(false)).click();
        page.waitForURL(Pattern.compile(".*/calendar/\\d+/edit$"));
        return new EventFormPage(page).assertEditLoaded();
    }

    /** Deletes only the Event that this scenario created and has already identity-asserted. */
    public DashboardPage deleteOwnedEvent(String ownedTitle) {
        assertLoaded(ownedTitle);
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Delete Event")).setExact(false)).click();
        assertVisible(heading("Are you sure you want to delete this event? This action cannot be undone."));
        button("Yes").click();
        page.waitForURL("**/dashboard");
        return new DashboardPage(page);
    }
}
