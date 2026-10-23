package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.nio.file.Path;

/** Custom upload intentionally keeps portal, email, timeline, and auto-note effects disabled. */
public final class MatterDocumentUploadPage extends BasePage {
    public MatterDocumentUploadPage(Page page) {
        super(page);
    }

    public MatterDocumentUploadPage chooseCustomUpload() {
        page.getByText("Upload Custom Document", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForURL("**template_id=custom");
        assertVisible(heading("Upload Document File"));
        return this;
    }

    public MatterDocumentsPage uploadOwnedDocument(Path documentPath) {
        page.locator("input[type='file']").setInputFiles(documentPath);
        page.getByRole(AriaRole.CHECKBOX,
                new Page.GetByRoleOptions().setName("Add to case timeline").setExact(true)).uncheck();
        page.getByRole(AriaRole.CHECKBOX,
                new Page.GetByRoleOptions().setName("Create automatic note when generated").setExact(true)).uncheck();
        button("Upload Document").click();
        page.waitForURL("**/documents");
        return new MatterDocumentsPage(page).assertLoaded();
    }
}
