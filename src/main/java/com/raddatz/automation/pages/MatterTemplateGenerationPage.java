package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.data.TemplateSourceFixture;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Single-Matter generation from an owned template; portal and notification controls stay unchecked. */
public final class MatterTemplateGenerationPage extends BasePage {
    public MatterTemplateGenerationPage(Page page) { super(page); }

    public MatterTemplateGenerationPage selectOwnedTemplate(String templateName) {
        link("+ Add Document").click();
        page.waitForURL("**/add-documents");
        page.locator("#templateSearchInput").fill(templateName);
        page.locator(".template-item").filter(new Locator.FilterOptions().setHasText(templateName)).click();
        page.waitForURL(Pattern.compile(".*/add-documents\\?template_id=\\d+"));
        return this;
    }

    public MatterTemplateGenerationPage generateOwnedWord(AdminScenarioData data, String instructions) {
        assertThat(page.locator("#field_tenant_name")).hasValue(data.tenantName());
        assertThat(page.locator("input[name='property_name']")).hasValue(data.propertyName());
        page.locator("#tpl_var_hearing_preparation_instructions").fill(instructions);
        assertVisible(page.getByText("1 of 1 template fields completed", new Page.GetByTextOptions().setExact(true)));
        page.locator("#word").check();
        page.locator("#pdf").uncheck();
        page.locator("#both_formats").uncheck();
        page.locator("#saveToDocs").check();
        page.locator("#addToTimeline").uncheck();
        page.locator("#create_note").uncheck();
        for (String id : List.of("sendToPortal", "emailClient", "emailAttorney", "emailManager")) {
            assertThat(page.locator("#" + id)).not().isChecked();
        }
        page.locator("#submitBtn").click();
        page.waitForURL(Pattern.compile(".*/matters/\\d+/documents$"));
        new MatterDocumentsPage(page).assertLoaded();
        return this;
    }

    /** The caller establishes an empty owned Matter inventory before generation. */
    public List<String> verifyGeneratedWordAndGetNames(Path artifacts, String reference, String instructions) {
        Locator rows = page.getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.LINK,
                        new Page.GetByRoleOptions().setName("Download Word").setExact(true))));
        assertThat(rows).not().hasCount(0);
        List<String> names = new ArrayList<>();
        for (int i = 0; i < rows.count(); i++) {
            // Enumerates every generated document on a newly empty owned Matter; no existing data is selected.
            Locator row = rows.nth(i);
            String viewName = row.getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName(Pattern.compile("^View .+"))).innerText();
            var view = row.getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName(Pattern.compile("^View .+")));
            String accessibleName = view.getAttribute("aria-label");
            names.add(accessibleName != null ? accessibleName.substring("View ".length()) : viewName);
            var download = page.waitForDownload(() -> row.getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName("Download Word").setExact(true)).click());
            Path target = artifacts.resolve("downloads/generated-" + reference + "-" + i + ".docx");
            try { Files.createDirectories(target.getParent()); download.saveAs(target); }
            catch (Exception exception) { throw new IllegalStateException("Cannot save generated Word document", exception); }
            TemplateSourceFixture.assertSubstituted(target, instructions);
        }
        return names;
    }
}
