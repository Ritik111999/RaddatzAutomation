package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

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
        assertVisible(page.getByText(taskName,
                new Page.GetByTextOptions().setExact(true)).first());
        return this;
    }
}
