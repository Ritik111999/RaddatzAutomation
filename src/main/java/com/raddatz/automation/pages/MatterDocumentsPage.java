package com.raddatz.automation.pages;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Document list operations are always scoped to the unique filename created by this run. */
public final class MatterDocumentsPage extends BasePage {
    public MatterDocumentsPage(Page page) {
        super(page);
    }

    public MatterDocumentsPage assertLoaded() {
        assertVisible(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("Documents \\(.* total\\)")).setExact(false)));
        assertVisible(link("+ Add Document"));
        return this;
    }

    public MatterDocumentUploadPage openCustomUpload() {
        link("+ Add Document").click();
        page.waitForURL("**/add-documents");
        return new MatterDocumentUploadPage(page).chooseCustomUpload();
    }

    public MatterDocumentsPage assertOwnedDocumentListed(String fileName) {
        assertVisible(ownedDocumentRow(fileName));
        assertVisible(ownedDocumentRow(fileName).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("View " + fileName).setExact(true)));
        return this;
    }

    public MatterDocumentsPage downloadOwnedDocument(String fileName, Path downloadTarget) {
        Download download = page.waitForDownload(() -> ownedDocumentRow(fileName).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Download Word").setExact(true)).click());
        try {
            Files.createDirectories(downloadTarget.getParent());
            download.saveAs(downloadTarget);
            if (!Files.isRegularFile(downloadTarget) || Files.size(downloadTarget) == 0) {
                throw new IllegalStateException("Document download was not written to the expected artifact path");
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Could not save owned document download", exception);
        }
        return this;
    }

    public MatterDocumentsPage deleteOwnedDocument(String fileName) {
        ownedDocumentRow(fileName).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete document").setExact(true)).click();
        assertVisible(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("Delete .*Both files will be permanently removed\\.")).setExact(false)));
        button("Yes, Delete").click();
        assertThat(ownedDocumentRow(fileName)).not().isVisible();
        return this;
    }

    private Locator ownedDocumentRow(String fileName) {
        return page.getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.LINK,
                        new Page.GetByRoleOptions().setName("View " + fileName).setExact(true))));
    }
}
