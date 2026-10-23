package com.raddatz.automation.tests;

import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.data.UploadFixtures;
import com.raddatz.automation.pages.DocumentTemplatesPage;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.time.Clock;

/**
 * Establishes owned-template metadata and Word-source persistence only. It does
 * not generate a Matter document, add automatic billing entries, or enter the editor.
 */
public final class AdminDocumentTemplateMetadataLifecycleTest extends ReadOnlyTestBase {
    private static final String CATEGORY = "Notice";

    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @Test(groups = "admin-mutation", description = "Administrator creates, uploads, edits, and downloads an owned document template without generation or billing side effects.")
    public void administratorMaintainsOwnedDocumentTemplateMetadata() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        String initialName = "Lease Compliance Notice for " + data.propertyName();
        String revisedName = "Lease Compliance and Hearing Notice for " + data.propertyName();
        String initialDescription = "Lease-compliance notice for " + data.caseNumber()
                + ". Scenario reference " + data.reference() + ".";
        String revisedDescription = "Lease-compliance and hearing-preparation notice for " + data.caseNumber()
                + ". Scenario reference " + data.reference() + ".";
        String variableLabel = "Hearing Preparation Instructions";
        String variableToken = "{{HEARING_PREPARATION_INSTRUCTIONS}}";
        String variableHint = "Enter the hearing-preparation instructions for counsel.";
        String sourceFileName = "lease-compliance-notice-" + data.reference() + ".docx";
        Path sourceDocument = UploadFixtures.copyInspectionMemorandum(sourceFileName, config.artifactDirectory());
        Path downloadedDocument = config.artifactDirectory().resolve("downloads").resolve(sourceFileName);

        DocumentTemplatesPage templates = openTemplatesFromSettings();
        templates.openCreateTemplate()
                .createOwnedTemplate(initialName, initialDescription, CATEGORY,
                        variableLabel, variableToken, variableHint, sourceDocument)
                .assertOwnedTemplateListed(initialName, CATEGORY)
                .assertOwnedTemplateActionsAvailable(initialName)
                .viewOwnedTemplate(initialName)
                .downloadOwnedTemplate(initialName, downloadedDocument)
                .editOwnedTemplate(initialName)
                .assertMetadataPersisted(initialName, initialDescription, CATEGORY)
                .assertOwnedVariablePersisted(variableLabel, variableToken, variableHint)
                .updateOwnedTemplate(revisedName, revisedDescription)
                .assertOwnedTemplateListed(revisedName, CATEGORY)
                .assertOwnedTemplateActionsAvailable(revisedName)
                .viewOwnedTemplate(revisedName)
                .deleteOwnedTemplate(revisedName);
    }

    private DocumentTemplatesPage openTemplatesFromSettings() {
        new Sidebar(currentPage()).goToDashboard();
        currentPage().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Settings").setExact(true)).click();
        currentPage().getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Create Document Template").setExact(true)).click();
        currentPage().waitForURL("**/document-templates/create");
        currentPage().getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Document Templates").setExact(true)).click();
        currentPage().waitForURL("**/document-templates");
        return new DocumentTemplatesPage(currentPage()).assertLoaded();
    }
}
