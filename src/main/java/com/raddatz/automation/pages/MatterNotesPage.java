package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Matter Notes uses stable, application-supplied editor ids rather than an
 * accessible textarea. Each mutation scopes its row to the exact run-owned
 * note text before acting.
 */
public final class MatterNotesPage extends BasePage {
    public MatterNotesPage(Page page) {
        super(page);
    }

    public MatterNotesPage assertLoaded() {
        assertVisible(heading("Add Note"));
        assertVisible(page.getByRole(AriaRole.TABLE));
        assertVisible(button("Submit"));
        return this;
    }

    public MatterNotesPage createOwnedNote(String noteText) {
        page.locator("#matterNoteContent").fill(noteText);
        button("Submit").click();
        assertVisible(page.getByText("Note saved.", new Page.GetByTextOptions().setExact(true)));
        assertVisible(ownedNoteRow(noteText));
        return this;
    }

    public MatterNotesPage updateOwnedNote(String originalText, String revisedText) {
        ownedNoteRow(originalText).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Edit note").setExact(true)).click();
        Locator dialog = page.getByRole(AriaRole.DIALOG);
        assertVisible(dialog.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Edit Note").setExact(true)));
        dialog.locator("#matterNoteEditContent").fill(revisedText);
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Update").setExact(true)).click();
        assertVisible(page.getByText("Note updated successfully.", new Page.GetByTextOptions().setExact(true)));
        page.reload();
        assertLoaded();
        assertVisible(ownedNoteRow(revisedText));
        return this;
    }

    public MatterNotesPage deleteOwnedNote(String ownedNoteText) {
        ownedNoteRow(ownedNoteText).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete note").setExact(true)).click();
        assertVisible(heading("Are you sure?"));
        button("Delete").click();
        assertVisible(page.getByText("Note deleted successfully.", new Page.GetByTextOptions().setExact(true)));
        assertThat(ownedNoteRow(ownedNoteText)).not().isVisible();
        return this;
    }

    private Locator ownedNoteRow(String exactNoteText) {
        return page.getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(exactNoteText));
    }
}
