package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

import java.util.regex.Pattern;
import java.util.LinkedHashMap;
import java.util.Map;

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

    /** Reads blank Court Update controls only; it does not select an Event or submit an update. */
    public Map<String, ObservedFieldConstraint> observeValidationConstraints() {
        Map<String, ObservedFieldConstraint> constraints = new LinkedHashMap<>();
        constraints.put("matter", observeConstraint("matter", page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Matter *").setExact(true))));
        constraints.put("notes", observeConstraint("notes", page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Notes *").setExact(true))));
        constraints.put("caseStatus", observeConstraint("caseStatus", page.locator("#case_status")));
        constraints.put("calendarDate", observeConstraint("calendarDate", page.locator("#courtUpdatesCalendarDate")));
        return Map.copyOf(constraints);
    }

    public CourtUpdatesPage selectCalendarDate(String date) {
        page.locator("#courtUpdatesCalendarDate").fill(date);
        page.waitForURL(Pattern.compile(".*calendar_date=" + date + ".*"));
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

    public CourtTimePage continueToCourtTime(String sensitivePassword) {
        Locator courtTimeBtn = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*(Court Time|Court Billing|Billing).*")).setExact(false));
        if (courtTimeBtn.isVisible()) {
            courtTimeBtn.click();
        } else {
            page.navigate(page.url().replaceAll("/court/.*", "/court/billing"));
        }
        if (page.url().contains("/confirm-sensitive-access")) {
            new BillingSessionPage(page).start(sensitivePassword);
        }
        return new CourtTimePage(page).assertLoaded();
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
        Locator modalHeading = page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Delete this court update?").setExact(true));
        assertVisible(modalHeading);
        button("Yes, delete").click();
        assertThat(modalHeading).not().isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        page.waitForTimeout(1000);
        page.reload();
        assertLoaded();
        assertThat(ownedCourtUpdateRow(revisedNotes)).not().isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions().setTimeout(15000));
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

    /**
     * Adds an associated To-do directly from within the Court Updates screen.
     */
    public CourtUpdatesPage addCourtTodo(String todoTitle, String dueDate) {
        Locator addTodoBtn = page.locator("#addTodoBtn");
        if (!addTodoBtn.isVisible()) {
            addTodoBtn = page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(Pattern.compile(".*(Add To-do|Add Task).*")).setExact(false));
        }
        assertVisible(addTodoBtn);
        addTodoBtn.click();

        Locator modal = page.locator("#courtAddTodoModal");
        assertVisible(modal);

        Locator nameInput = modal.locator("#courtTodoName");
        assertVisible(nameInput);
        nameInput.fill(todoTitle);

        Locator dateInput = modal.locator("#courtTodoDueDate");
        if (dateInput.isVisible()) {
            dateInput.fill(dueDate);
        }

        Locator submitBtn = modal.locator("#courtTodoSubmitBtn");
        assertVisible(submitBtn);
        submitBtn.click();
        waitForModalDismissed("#courtAddTodoModal");

        // Verify it was queued into the linked section
        Locator linkedItem = page.locator("#linkedTodoSection .court-todo-item")
                .filter(new Locator.FilterOptions().setHasText(todoTitle));
        assertVisible(linkedItem);
        return this;
    }

    /**
     * Edits an associated To-do directly on the Court Updates screen.
     */
    public CourtUpdatesPage editCourtTodo(String originalTitle, String revisedTitle) {
        Locator todoItem = page.locator(".court-todo-item")
                .filter(new Locator.FilterOptions().setHasText(originalTitle));
        assertVisible(todoItem);

        Locator editBtn = todoItem.locator("button[title*='Edit'], button:has(.fa-pencil-alt)");
        assertVisible(editBtn);
        editBtn.click();

        Locator modal = page.locator("#courtAddTodoModal");
        assertVisible(modal);

        Locator nameInput = modal.locator("#courtTodoName");
        assertVisible(nameInput);
        nameInput.fill(revisedTitle);

        Locator submitBtn = modal.locator("#courtTodoSubmitBtn");
        assertVisible(submitBtn);
        submitBtn.click();
        waitForModalDismissed("#courtAddTodoModal");

        Locator updatedItem = page.locator(".court-todo-item")
                .filter(new Locator.FilterOptions().setHasText(revisedTitle));
        assertVisible(updatedItem);
        return this;
    }


    /**
     * Deletes/removes an associated To-do directly from the Court Updates screen.
     */
    public CourtUpdatesPage deleteCourtTodo(String todoTitle) {
        Locator todoItem = page.locator(".court-todo-item")
                .filter(new Locator.FilterOptions().setHasText(todoTitle));
        assertVisible(todoItem);

        Locator removeBtn = todoItem.locator("button[title*='Remove'], button[title*='Delete'], button:has(.fa-trash-alt), button:has(.fa-times)");
        assertVisible(removeBtn);
        removeBtn.click();

        assertThat(todoItem).not().isVisible();
        return this;
    }

    /**
     * Asserts that a To-do linked to a court update row in the table exhibits the .has-todo icon
     * and shows the correct details in the floating popover.
     */
    public CourtUpdatesPage assertCourtTodoPopover(String notes, String expectedTodoTitle) {
        Locator row = ownedCourtUpdateRow(notes);
        Locator todoIcon = row.locator(".cu-todo-icon.has-todo");
        assertVisible(todoIcon);
        assertThat(todoIcon).hasAttribute("data-todo-name", expectedTodoTitle);
        return this;
    }

    /**
     * Triggers the Extra Event scheduling form from within the Court Updates screen.
     */
    public EventFormPage openExtraEventForm() {
        Locator addEventBtn = page.locator("#addCourtDateRowBtn");
        if (!addEventBtn.isVisible()) {
            addEventBtn = page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(Pattern.compile(".*(Add Event|Extra Event|Edit Event).*")).setExact(false));
        }
        assertVisible(addEventBtn);
        addEventBtn.click();
        page.waitForURL(Pattern.compile(".*/calendar/add-event.*"));
        return new EventFormPage(page);
    }

    /**
     * Edits an extra event directly from within the Court Updates screen.
     */
    public CourtUpdatesPage editExtraEvent(String originalTitle, String revisedTitle) {
        Locator eventCard = page.locator(".court-extra-event, [data-event-id]").filter(new Locator.FilterOptions().setHasText(originalTitle));
        if (eventCard.isVisible()) {
            Locator editBtn = eventCard.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(Pattern.compile(".*Edit.*")).setExact(false));
            if (editBtn.isVisible()) {
                editBtn.click();
                Locator modal = page.getByRole(AriaRole.DIALOG);
                Locator titleInput = modal.locator("input[name='title'], input[name='event_title']");
                if (titleInput.isVisible()) {
                    titleInput.fill(revisedTitle);
                    modal.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(Pattern.compile(".*(Save|Update).*")).setExact(false)).click();
                }
            }
        }
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
