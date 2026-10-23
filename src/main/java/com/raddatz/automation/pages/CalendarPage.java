package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

/** Calendar shell and its scenario-owned event entry point. */
public final class CalendarPage extends BasePage {
    public CalendarPage(Page page) {
        super(page);
    }

    public CalendarPage assertLoaded() {
        assertVisible(heading("Calendar"));
        assertVisible(page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Week.*"))));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Search events, cases, people...").setExact(true)));
        assertVisible(page.getByRole(AriaRole.GROUP,
                new Page.GetByRoleOptions().setName("Calendar date picker").setExact(true)));
        return this;
    }

    public EventFormPage openAddEvent() {
        link("+ Add Event").click();
        page.waitForURL("**/calendar/add-event");
        return new EventFormPage(page).assertLoaded();
    }
}
