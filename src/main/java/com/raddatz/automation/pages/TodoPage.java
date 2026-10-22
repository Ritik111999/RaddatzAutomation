package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/** Read-only task-list assertions. Task mutation controls intentionally are not modelled here. */
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
}
