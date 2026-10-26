package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

/** Shared task modal used by Dashboard and To-dos; creates work only for the owned Matter. */
public final class TaskFormModal extends BasePage {
    private final Locator modal;

    public TaskFormModal(Page page) {
        super(page);
        modal = page.getByRole(AriaRole.DIALOG);
    }

    public TaskFormModal assertLoaded() {
        assertVisible(modal.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Add New Task").setExact(false)));
        assertVisible(modal.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("Matter").setExact(true)));
        return this;
    }

    /** Captures rendered task controls only; opening the modal does not save a task. */
    public Map<String, ObservedFieldConstraint> observeValidationConstraints() {
        Map<String, ObservedFieldConstraint> constraints = new LinkedHashMap<>();
        constraints.put("matter", observeConstraint("matter", modal.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("Matter").setExact(true))));
        constraints.put("taskName", observeConstraint("taskName", modal.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("Enter task name").setExact(true))));
        constraints.put("description", observeConstraint("description", modal.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("Optional description...").setExact(true))));
        constraints.put("dueDate", observeConstraint("dueDate", modal.locator("input[type='date']")));
        return Map.copyOf(constraints);
    }

    public void createOwnedTask(AdminScenarioData data) {
        Locator matterSearch = modal.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("Matter").setExact(true));
        // The unique case number prevents a recurring real-world tenant name from selecting another Matter.
        matterSearch.fill(data.caseNumber());
        Locator ownedMatter = modal.getByText(data.tenantName(), new Locator.GetByTextOptions().setExact(true))
                .locator("..");
        assertVisible(ownedMatter);
        ownedMatter.click();
        assertThat(modal.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("Select a matter to populate the client").setExact(true)))
                .hasValue(data.companyName());

        modal.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("Enter task name").setExact(true)).fill(data.taskName());
        modal.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("Optional description...").setExact(true)).fill(data.taskDescription());
        modal.locator("input[type='date']").fill(data.taskDueDate());
        chooseSearchableDropdown(0, "High");
        chooseSearchableDropdown(1, "Staff Task");
        modal.getByRole(AriaRole.COMBOBOX,
                        new Locator.GetByRoleOptions().setName("— Select Staff —").setExact(true))
                .click();
        page.getByRole(AriaRole.LISTBOX).last().getByRole(AriaRole.OPTION,
                        new Locator.GetByRoleOptions().setName("Mark Lee (admin)").setExact(true))
                .click();

        modal.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("+ Add Task").setExact(true)).click();
    }

    private void chooseSearchableDropdown(int index, String optionName) {
        modal.getByRole(AriaRole.COMBOBOX,
                        new Locator.GetByRoleOptions().setName("Search...").setExact(true))
                .nth(index)
                .click();
        page.getByRole(AriaRole.LISTBOX).last().getByRole(AriaRole.OPTION,
                        new Locator.GetByRoleOptions().setName(optionName).setExact(true))
                .click();
    }
}
