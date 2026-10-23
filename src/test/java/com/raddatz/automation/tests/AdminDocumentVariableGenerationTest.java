package com.raddatz.automation.tests;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.data.TemplateSourceFixture;
import com.raddatz.automation.pages.*;
import org.testng.annotations.Test;

import java.time.Clock;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Verifies actual DOCX substitution, using a fresh owned template and empty owned Matter inventory. */
public final class AdminDocumentVariableGenerationTest extends ReadOnlyTestBase {
    @Override protected boolean requiresMutationPermission() { return true; }

    @Test(groups="admin-mutation", description="Generate an owned Word document and verify a custom variable is replaced in its downloaded XML.")
    public void administratorGeneratesOwnedVariableDocument() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        String name = "Hearing Preparation Memorandum for " + data.propertyName();
        String instructions = "Review the lease ledger and service affidavit for " + data.caseNumber() + ".";
        var page = currentPage();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Settings").setExact(true)).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create Document Template").setExact(true)).click();
        page.waitForURL("**/document-templates/create");
        assertThat(page.locator("#timeEntriesContainer")).hasCount(1);
        assertThat(page.locator("#expenseEntriesContainer")).hasCount(1);
        assertThat(page.locator("#timeEntriesContainer .time-entry-row")).hasCount(0);
        assertThat(page.locator("#expenseEntriesContainer .expense-entry-row")).hasCount(0);
        new DocumentTemplateFormPage(page).assertCreateLoaded().createOwnedTemplate(name, data.ownershipNote(), "Notice",
                "Hearing Preparation Instructions", TemplateSourceFixture.TOKEN, "Enter preparation instructions for counsel.",
                TemplateSourceFixture.prepare(data, config.artifactDirectory()));

        new Sidebar(page).goToClients().openAddCompany().createOwnedCompany(data).openAddProperty().createOwnedProperty(data);
        new Sidebar(page).goToClients().openAddMatter().createOwnedMatter(data)
                .openOwnedMatter(data.tenantName(), data.caseNumber()).openDocuments();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
                .setName(java.util.regex.Pattern.compile(".*Documents \\(0 total\\).*")))).isVisible();
        var generation = new MatterTemplateGenerationPage(page).selectOwnedTemplate(name).generateOwnedWord(data, instructions);
        var files = generation.verifyGeneratedWordAndGetNames(config.artifactDirectory(), data.reference(), instructions);
        for (String file : files) new MatterDocumentsPage(page).deleteOwnedDocument(file);
        // Remove the source template only after all of its generated owned documents have been verified and cleaned up.
        new Sidebar(page).goToDashboard();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Settings").setExact(true)).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create Document Template").setExact(true)).click();
        page.waitForURL("**/document-templates/create");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Document Templates").setExact(true)).click();
        page.waitForURL("**/document-templates");
        new DocumentTemplatesPage(page).deleteOwnedTemplate(name);
    }
}
