package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Form actions deliberately omit generated billing entries and document-generation flows. */
public final class DocumentTemplateFormPage extends BasePage {
    public DocumentTemplateFormPage(Page page) {
        super(page);
    }

    public DocumentTemplateFormPage assertCreateLoaded() {
        assertVisible(heading("Create Document Template"));
        assertVisible(page.locator("#name"));
        assertVisible(page.locator("#docxImportFile"));
        return this;
    }

    public DocumentTemplateFormPage assertEditLoaded() {
        assertVisible(heading(Pattern.compile("Edit: .+")));
        assertVisible(page.locator("#name"));
        return this;
    }

    public DocumentTemplatesPage createOwnedTemplate(
            String templateName,
            String description,
            String category,
            String variableLabel,
            String expectedMergeField,
            String variableHint,
            Path sourceDocument) {
        page.locator("#name").fill(templateName);
        page.locator("#description").fill(description);
        page.locator("#category").selectOption(new SelectOption().setLabel(category));
        addOwnedTextVariable(variableLabel, expectedMergeField, variableHint);
        page.locator("#docxImportFile").setInputFiles(sourceDocument);
        assertThat(page.locator("#docxImportBtn")).isEnabled();
        page.locator("#docxImportBtn").click();
        page.locator("#templateContentEmpty").waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN));
        saveTemplate();
        return new DocumentTemplatesPage(page).assertLoaded();
    }

    public DocumentTemplatesPage updateOwnedTemplate(String templateName, String description) {
        page.locator("#name").fill(templateName);
        page.locator("#description").fill(description);
        saveTemplate();
        return new DocumentTemplatesPage(page).assertLoaded();
    }

    public DocumentTemplateFormPage assertOwnedVariablePersisted(
            String variableLabel, String expectedMergeField, String variableHint) {
        Locator variable = page.locator("#variablesContainer");
        assertVisible(variable);
        assertThat(variable.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName(Pattern.compile("Field label.*")))).hasValue(variableLabel);
        assertVisible(variable.getByText(expectedMergeField, new Locator.GetByTextOptions().setExact(true)));
        assertThat(variable.locator("#variable_type_1 option:checked")).hasText("Single-line text");
        assertThat(variable.locator("#variable_hint_1")).hasValue(variableHint);
        return this;
    }

    public DocumentTemplateFormPage assertMetadataPersisted(String templateName, String description, String category) {
        assertThat(page.locator("#name")).hasValue(templateName);
        assertThat(page.locator("#description")).hasValue(description);
        assertThat(page.locator("#category option:checked")).hasText(category);
        return this;
    }

    private void addOwnedTextVariable(String variableLabel, String expectedMergeField, String variableHint) {
        page.locator("#addVariableBtn").click();
        Locator variable = page.locator("#variablesContainer");
        assertVisible(variable);
        variable.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName(Pattern.compile("Field label.*"))).fill(variableLabel);
        assertVisible(variable.getByText(expectedMergeField, new Locator.GetByTextOptions().setExact(true)));
        variable.locator("#variable_hint_1").fill(variableHint);
    }

    private void saveTemplate() {
        Locator saveTemplate = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Save Template.*")));
        assertThat(saveTemplate).isEnabled();
        saveTemplate.click();
        page.waitForURL(Pattern.compile(".*/document-templates$"));
    }
}
