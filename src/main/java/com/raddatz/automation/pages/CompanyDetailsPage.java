package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Company details page (/client-and-matter/client/{id}/view)
 * Exposes overview, properties, notes, and documents tabs.
 */
public final class CompanyDetailsPage extends BasePage {

    public CompanyDetailsPage(Page page) {
        super(page);
    }

    public CompanyDetailsPage assertLoaded(String companyName) {
        assertVisible(heading(Pattern.compile(".*" + Pattern.quote(companyName) + ".*")));
        return this;
    }

    public CompanyDetailsPage openNotesTab() {
        Locator notesTab = page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Notes.*")).setExact(false));
        if (notesTab.isVisible()) {
            notesTab.click();
        } else {
            page.locator("a[href*='#notes'], button[data-bs-target*='notes']").first().click();
        }
        return this;
    }

    public CompanyDetailsPage addOwnedNote(String noteText) {
        openNotesTab();
        Locator noteInput = page.getByPlaceholder("Add note...",
                new Page.GetByPlaceholderOptions().setExact(false));
        if (!noteInput.isVisible()) {
            noteInput = page.locator("textarea[name='note'], textarea#noteContent, #companyNoteText");
        }
        if (noteInput.isVisible()) {
            noteInput.fill(noteText);
            Locator saveBtn = page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(Pattern.compile(".*(Save|Add Note).*")).setExact(false));
            if (saveBtn.isVisible()) {
                saveBtn.click();
            }
        }
        return this;
    }

    public CompanyDetailsPage openDocumentsTab() {
        Locator docsTab = page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Documents.*")).setExact(false));
        if (docsTab.isVisible()) {
            docsTab.click();
        } else {
            page.locator("a[href*='#document'], button[data-bs-target*='document']").first().click();
        }
        return this;
    }

    public CompanyDetailsPage uploadOwnedDocument(Path documentPath) {
        openDocumentsTab();
        Locator fileInput = page.locator("input[type='file']");
        if (fileInput.isVisible() || fileInput.count() > 0) {
            fileInput.setInputFiles(documentPath);
            Locator uploadBtn = page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(Pattern.compile(".*Upload.*")).setExact(false));
            if (uploadBtn.isVisible()) {
                uploadBtn.click();
            }
        }
        return this;
    }

    public PropertyFormPage openAddProperty() {
        Locator addPropertyBtn = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("+ Add Property").setExact(false));
        if (!addPropertyBtn.isVisible()) {
            addPropertyBtn = page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("+ Add Property").setExact(false));
        }
        addPropertyBtn.click();
        page.waitForURL(Pattern.compile(".*/property/create.*"));
        return new PropertyFormPage(page).assertLoaded();
    }
}
