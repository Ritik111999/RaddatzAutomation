package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;


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

    public CalendarPage selectEventDate(String date) {
        LocalDate selected = LocalDate.parse(date);
        page.locator("#calendarDateTrigger").click();
        choosePickerValue("calendarPickerYearSelect", Integer.toString(selected.getYear()));
        choosePickerValue("calendarPickerMonthSelect", selected.getMonth().getDisplayName(TextStyle.FULL, Locale.US));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                        .setName(selected.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, uuuu", Locale.US)))
                        .setExact(true))
                .click();
        return this;
    }

    private void choosePickerValue(String nativeControlId, String optionName) {
        // The app leaves the native select in the accessibility tree but places
        // its searchable widget input over it. The widget's documented
        // data-ssd-for relationship is the only stable interactable contract.
        page.locator("input.ssd-input[data-ssd-for='" + nativeControlId + "']").click();
        page.getByRole(AriaRole.LISTBOX).last().getByRole(AriaRole.OPTION,
                        new Locator.GetByRoleOptions().setName(optionName).setExact(true))
                .click();
    }

    public CalendarPage assertOwnedEventVisible(AdminScenarioData data, boolean revised) {
        selectEventDate(data.eventDate());
        page.getByPlaceholder("Search events, cases, people...", new Page.GetByPlaceholderOptions().setExact(true))
                .fill(data.caseNumber());
        Locator card = eventCard(data.caseNumber());
        assertVisible(card);
        assertThat(card).containsText(data.tenantName());
        // The Calendar renders the legal suffix in a separate span with no text-node space.
        assertThat(card).containsText(data.companyName().substring(0, data.companyName().lastIndexOf(',')));
        assertThat(card).containsText("LLC");
        if (revised) assertThat(card).containsText("Initial Return Hearing: Service Review");
        return this;
    }

    public CalendarPage assertOwnedEventAbsent(AdminScenarioData data) {
        selectEventDate(data.eventDate());
        page.getByPlaceholder("Search events, cases, people...", new Page.GetByPlaceholderOptions().setExact(true))
                .fill(data.caseNumber());
        assertThat(eventCard(data.caseNumber())).not().isVisible();
        return this;
    }

    private Locator eventCard(String caseNumber) {
        return page.locator("#columnCalendarGrid [data-event-id]")
                .filter(new Locator.FilterOptions().setHasText(caseNumber));
    }
}
