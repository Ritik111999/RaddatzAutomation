package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Task list with a shared modal for scenario-owned task creation. */
public final class TodoPage extends BasePage {
    public TodoPage(Page page) {
        super(page);
    }

    public TodoPage assertLoaded() {
        assertVisible(heading("All Tasks"));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Search tasks").setExact(true)));
        assertVisible(page.getByRole(AriaRole.TABLE));
        assertVisible(page.getByRole(AriaRole.COLUMNHEADER,
                new Page.GetByRoleOptions().setName("Task").setExact(true)));
        return this;
    }

    public TaskFormModal openAddTask() {
        button("+ Add New Task").click();
        return new TaskFormModal(page).assertLoaded();
    }

    public TodoPage assertTaskListed(String taskName) {
        assertVisible(ownedTaskRow(taskName));
        return this;
    }

    public TodoPage editOwnedTask(String taskName, String revisedTaskName, String revisedDescription) {
        ownedTaskRow(taskName).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Edit task").setExact(true)).click();
        Locator dialog = page.getByRole(AriaRole.DIALOG);
        assertVisible(dialog.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Edit Task").setExact(true)));
        labeledTextbox(dialog, "Task Name *").fill(revisedTaskName);
        labeledTextbox(dialog, "Description").fill(revisedDescription);
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Save Changes").setExact(true)).click();
        // The application persists in place and leaves the edit modal open.
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Close").setExact(true)).click();
        page.reload();
        // Reload preserves the old search query. The renamed owned task should
        // no longer match it, so query by the revised identity before asserting
        // the server-backed list rather than treating the empty old-query state
        // as a persistence failure.
        assertVisible(heading("All Tasks"));
        assertTaskListed(revisedTaskName);
        return this;
    }

    public TodoPage deleteOwnedTask(String taskName) {
        ownedTaskRow(taskName).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete task").setExact(true)).click();
        assertVisible(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Are you sure?").setExact(true)));
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Yes, delete it").setExact(true)).click();
        searchTasks(taskName);
        assertThat(page.getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(taskName))).hasCount(0);
        return this;
    }

    private Locator ownedTaskRow(String taskName) {
        searchTasks(taskName);
        Locator row = page.getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(taskName));
        assertVisible(row);
        return row;
    }

    private void searchTasks(String taskName) {
        page.getByRole(AriaRole.TEXTBOX,
                        new Page.GetByRoleOptions().setName("Search tasks").setExact(true))
                .fill(taskName);
    }

    private Locator labeledTextbox(Locator container, String label) {
        return container.getByText(label, new Locator.GetByTextOptions().setExact(true))
                .locator("..")
                .getByRole(AriaRole.TEXTBOX);
    }
}
