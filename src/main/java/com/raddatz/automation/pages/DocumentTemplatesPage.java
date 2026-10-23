package com.raddatz.automation.pages;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Registry operations are restricted to a template whose exact name was created by this run. */
public final class DocumentTemplatesPage extends BasePage {
    public DocumentTemplatesPage(Page page) {
        super(page);
    }

    public DocumentTemplatesPage assertLoaded() {
        assertVisible(heading("Document Templates"));
        assertVisible(page.locator("#searchInput"));
        assertVisible(link("+ Add New Template"));
        return this;
    }

    public DocumentTemplateFormPage openCreateTemplate() {
        link("+ Add New Template").click();
        page.waitForURL("**/document-templates/create");
        return new DocumentTemplateFormPage(page).assertCreateLoaded();
    }

    public DocumentTemplateFormPage editOwnedTemplate(String templateName) {
        ownedTemplateRow(templateName).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Edit template").setExact(true)).click();
        page.waitForURL("**/document-templates/*/edit");
        return new DocumentTemplateFormPage(page).assertEditLoaded();
    }

    public DocumentTemplatesPage assertOwnedTemplateListed(String templateName, String category) {
        Locator row = ownedTemplateRow(templateName);
        assertVisible(row);
        assertVisible(row.getByText(templateName, new Locator.GetByTextOptions().setExact(true)));
        assertVisible(row.getByText(category, new Locator.GetByTextOptions().setExact(true)));
        return this;
    }

    /**
     * Proves that the owned row exposes the safe-to-map view and destructive-action
     * affordances without opening an existing template or triggering either action.
     */
    public DocumentTemplatesPage assertOwnedTemplateActionsAvailable(String templateName) {
        Locator row = ownedTemplateRow(templateName);
        assertVisible(row.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("View template").setExact(true)));
        assertVisible(row.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Download template").setExact(true)));
        assertVisible(row.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Edit template").setExact(true)));
        assertVisible(row.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete template").setExact(true)));
        return this;
    }

    public DocumentTemplatesPage viewOwnedTemplate(String templateName) {
        ownedTemplateRow(templateName).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("View template").setExact(true)).click();
        Locator preview = page.locator("#templatePreviewModal");
        assertVisible(preview);
        assertVisible(preview.getByText(templateName, new Locator.GetByTextOptions().setExact(true)));
        assertVisible(preview.locator("#templatePreviewDownload"));
        Locator previewHeader = preview.locator("#templatePreviewModalLabel").locator("..");
        previewHeader.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Close").setExact(true)).click();
        assertThat(preview).not().isVisible();
        return this;
    }

    public DocumentTemplatesPage downloadOwnedTemplate(String templateName, Path downloadTarget) {
        Download download = page.waitForDownload(() -> ownedTemplateRow(templateName).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Download template").setExact(true)).click());
        try {
            Files.createDirectories(downloadTarget.getParent());
            download.saveAs(downloadTarget);
            if (!Files.isRegularFile(downloadTarget) || Files.size(downloadTarget) == 0) {
                throw new IllegalStateException("Owned template download was not written to the expected artifact path");
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Could not save owned template download", exception);
        }
        return this;
    }

    public DocumentTemplatesPage deleteOwnedTemplate(String templateName) {
        ownedTemplateRow(templateName).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete template").setExact(true)).click();
        Locator confirmation = page.locator("#deleteModal");
        assertVisible(confirmation);
        assertVisible(confirmation.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName(Pattern.compile(".*Confirm Delete$"))));
        assertVisible(confirmation.getByText(templateName, new Locator.GetByTextOptions().setExact(true)));
        confirmation.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete Template").setExact(true)).click();
        assertThat(ownedTemplateRow(templateName)).not().isVisible();
        return this;
    }

    private Locator ownedTemplateRow(String templateName) {
        return page.getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(templateName));
    }
}
