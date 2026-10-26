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

    /**
     * Adds an associated To-do directly from within the Court Updates screen.
     */
    public CourtUpdatesPage addCourtTodo(String todoTitle, String dueDate) {
        Locator addTodoBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*(Add To-do|Add Task|New Task).*")).setExact(false));
        if (!addTodoBtn.isVisible()) {
            addTodoBtn = page.locator("#addCourtTodoBtn, button[data-action='add-court-todo'], a[href*='add-todo']");
        }
        if (addTodoBtn.isVisible()) {
            addTodoBtn.click();
            Locator modal = page.getByRole(AriaRole.DIALOG);
            Locator nameInput = modal.locator("input[name='name'], input[name='title'], #todoName");
            if (!nameInput.isVisible()) {
                nameInput = page.getByPlaceholder("Task name", new Page.GetByPlaceholderOptions().setExact(false));
            }
            if (nameInput.isVisible()) {
                nameInput.fill(todoTitle);
            }
            Locator dateInput = modal.locator("input[type='date']");
            if (dateInput.isVisible()) {
                dateInput.fill(dueDate);
            }
            Locator saveBtn = modal.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName(Pattern.compile(".*(Save|Add).*")).setExact(false));
            if (saveBtn.isVisible()) {
                saveBtn.click();
            }
        }
        return this;
    }

    /**
     * Edits an associated To-do directly on the Court Updates screen.
     */
    public CourtUpdatesPage editCourtTodo(String originalTitle, String revisedTitle) {
        Locator todoCard = page.locator(".court-todo-item, [data-todo-id]").filter(new Locator.FilterOptions().setHasText(originalTitle));
        if (todoCard.isVisible()) {
            Locator editBtn = todoCard.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(Pattern.compile(".*Edit.*")).setExact(false));
            if (editBtn.isVisible()) {
                editBtn.click();
                Locator modal = page.getByRole(AriaRole.DIALOG);
                Locator nameInput = modal.locator("input[name='name'], input[name='title']");
                if (nameInput.isVisible()) {
                    nameInput.fill(revisedTitle);
                    modal.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(Pattern.compile(".*(Save|Update).*")).setExact(false)).click();
                }
            }
        }
        return this;
    }

    /**
     * Deletes an associated To-do directly from the Court Updates screen.
     */
    public CourtUpdatesPage deleteCourtTodo(String todoTitle) {
        Locator todoCard = page.locator(".court-todo-item, [data-todo-id]").filter(new Locator.FilterOptions().setHasText(todoTitle));
        if (todoCard.isVisible()) {
            Locator deleteBtn = todoCard.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(Pattern.compile(".*Delete.*")).setExact(false));
            if (deleteBtn.isVisible()) {
                deleteBtn.click();
                Locator confirmBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile(".*(Yes|Confirm|Delete).*")).setExact(false));
                if (confirmBtn.isVisible()) {
                    confirmBtn.click();
                }
            }
        }
        return this;
    }

    /**
     * Adds an extra/follow-up event directly from within the Court Updates screen.
     */
    public CourtUpdatesPage addExtraEvent(String eventTitle, String eventDate, String eventTime) {
        Locator addEventBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*(Add Event|Add Extra Event|Schedule Next).*")).setExact(false));
        if (!addEventBtn.isVisible()) {
            addEventBtn = page.locator("#addCourtEventBtn, button[data-action='add-court-event'], a[href*='add-event']");
        }
        if (addEventBtn.isVisible()) {
            addEventBtn.click();
            Locator modal = page.getByRole(AriaRole.DIALOG);
            Locator titleInput = modal.locator("input[name='title'], input[name='event_title']");
            if (titleInput.isVisible()) {
                titleInput.fill(eventTitle);
            }
            Locator dateInput = modal.locator("input[type='date']");
            if (dateInput.isVisible()) {
                dateInput.fill(eventDate);
            }
            Locator timeInput = modal.locator("input[type='time']");
            if (timeInput.isVisible()) {
                timeInput.fill(eventTime);
            }
            Locator saveBtn = modal.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName(Pattern.compile(".*(Save|Schedule).*")).setExact(false));
            if (saveBtn.isVisible()) {
                saveBtn.click();
            }
        }
        return this;
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
