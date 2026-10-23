package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.data.UploadFixtures;

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

    public CalendarPage createOwnedEvent(AdminScenarioData data) {
        Locator matterSearch = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Search by tenant name or case type…").setExact(true));
        matterSearch.fill(data.tenantName());
        Locator ownedMatter = page.getByText(data.tenantName(), new Page.GetByTextOptions().setExact(true)).last()
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
        page.locator("input[type='date']").fill(data.eventDate());
        page.locator("input[type='time']").fill(data.eventStartTime());
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("01:00").setExact(true)).fill(data.eventDuration());
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(
                "Add preparation notes, case context, special instructions, or any other relevant information about this court date..."
        ).setExact(true)).fill(data.eventNotes());
        page.locator("input[type='file']").setInputFiles(
                new java.nio.file.Path[]{UploadFixtures.inspectionMemorandum(), UploadFixtures.propertyExteriorPhoto()});

        button("Save & Close").click();
        return new CalendarPage(page).assertLoaded();
    }
}
