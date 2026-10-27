package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page object for the Workflows module (/workflow and /workflow/create).
 */
public final class WorkflowPage extends BasePage {

    private final Locator heading;
    private final Locator workflowsTable;
    private final Locator newWorkflowBtn;

    public WorkflowPage(Page page) {
        super(page);
        this.heading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Workflows").setExact(true));
        this.workflowsTable = page.getByRole(AriaRole.TABLE);
        this.newWorkflowBtn = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(Pattern.compile(".*New Workflow.*")).setExact(false));
    }

    public WorkflowPage assertLoaded() {
        assertVisible(heading);
        assertVisible(workflowsTable);
        assertVisible(newWorkflowBtn);
        return this;
    }

    public WorkflowPage assertTableColumns() {
        assertThat(workflowsTable.getByRole(AriaRole.COLUMNHEADER,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Workflow Name.*")))).isVisible();
        assertThat(workflowsTable.getByRole(AriaRole.COLUMNHEADER,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Trigger.*")))).isVisible();
        assertThat(workflowsTable.getByRole(AriaRole.COLUMNHEADER,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Status.*")))).isVisible();
        assertThat(workflowsTable.getByRole(AriaRole.COLUMNHEADER,
                new Locator.GetByRoleOptions().setName("Actions").setExact(true))).isVisible();
        return this;
    }

    public WorkflowPage openCreateWorkflow() {
        assertVisible(newWorkflowBtn);
        newWorkflowBtn.click();
        page.waitForURL("**/workflow/create");
        return this;
    }

    public WorkflowPage assertCreateWorkflowLoaded() {
        assertThat(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Create New Workflow").setExact(true))).isVisible();
        assertThat(page.getByText("Trigger Type *",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*To-Do.*")).setExact(false))).isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Calendar Event.*")).setExact(false))).isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save Workflow").setExact(true))).isVisible();
        return this;
    }
}
