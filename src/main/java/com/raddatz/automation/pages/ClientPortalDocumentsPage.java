package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page object for the Client Portal Document Management hub (/documents).
 */
public final class ClientPortalDocumentsPage extends BasePage {

    private final Locator heading;
    private final Locator folderViewBtn;
    private final Locator listViewBtn;

    public ClientPortalDocumentsPage(Page page) {
        super(page);
        this.heading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(Pattern.compile(".*Document Management.*")).setExact(false));
        this.folderViewBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Folder View").setExact(false));
        this.listViewBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("List View").setExact(false));
    }

    public ClientPortalDocumentsPage assertLoaded() {
        assertVisible(heading);
        assertVisible(folderViewBtn);
        assertVisible(listViewBtn);
        return this;
    }

    public ClientPortalDocumentsPage switchToListMode() {
        listViewBtn.click();
        page.waitForTimeout(400);
        return this;
    }

    public ClientPortalDocumentsPage switchToFolderMode() {
        folderViewBtn.click();
        page.waitForTimeout(400);
        return this;
    }
}
