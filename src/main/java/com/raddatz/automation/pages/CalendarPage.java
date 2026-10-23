package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;


/** Calendar shell and its scenario-owned event entry point. */
public final class CalendarPage extends BasePage {
    public CalendarPage(Page page) {
        super(page);
    }

    public CalendarPage assertLoaded() {
        assertVisible(heading("Calendar"));
        return this;
    }

    public EventFormPage openAddEvent() {
        link("+ Add Event").click();
        page.waitForURL("**/calendar/add-event");
        return new EventFormPage(page).assertLoaded();
    }
}
