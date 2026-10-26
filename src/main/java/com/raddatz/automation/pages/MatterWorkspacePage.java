package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

/** Entry point for the tabbed workspace of a scenario-owned Matter. */
public final class MatterWorkspacePage extends BasePage {
    public MatterWorkspacePage(Page page) {
        super(page);
    }

    public MatterWorkspacePage assertLoaded(String tenantName) {
        assertVisible(heading(tenantName));
        assertVisible(notesLink());
        return this;
    }

    public MatterNotesPage openNotes() {
        notesLink().click();
        page.waitForURL("**/notes");
        return new MatterNotesPage(page).assertLoaded();
    }

    public MatterDocumentsPage openDocuments() {
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("Documents$")).setExact(false)).click();
        page.waitForURL("**/documents");
        return new MatterDocumentsPage(page).assertLoaded();
    }

    public BillingSessionPage openTimeEntry() {
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("Add Time Entry$")).setExact(false)).click();
        page.waitForURL(Pattern.compile(".*/(?:confirm-sensitive-access/billing|time(?:\\?.*)?)$"));
        return new BillingSessionPage(page).assertLoaded();
    }

    public MatterWorkspacePage openTodoTab() {
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("Todo$")).setExact(false)).click();
        page.waitForURL("**/todos");
        return this;
    }

    public MatterWorkspacePage openTimelineTab() {
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("Timeline$")).setExact(false)).click();
        page.waitForURL("**/timeline");
        return this;
    }

    public TaskFormModal openAddTaskModal() {
        com.microsoft.playwright.Locator addTaskBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Add Task$")).setExact(false));
        assertVisible(addTaskBtn);
        addTaskBtn.click();
        return new TaskFormModal(page);
    }

    public EventFormPage openScheduleEvent() {
        com.microsoft.playwright.Locator scheduleBtn = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("Schedule Event$")).setExact(false));
        if (!scheduleBtn.isVisible()) {
            scheduleBtn = page.locator("a[href*='/calendar/add-event']");
        }
        assertVisible(scheduleBtn);
        scheduleBtn.click();
        page.waitForURL(Pattern.compile(".*/calendar/add-event.*"));
        return new EventFormPage(page).assertLoaded();
    }

    private com.microsoft.playwright.Locator notesLink() {
        // The UI prepends a presentational icon to the accessible link name.
        return page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("Notes$")).setExact(false));
    }
}
