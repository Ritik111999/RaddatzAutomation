package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Small, owned-record edits; identity and Notes are checked before every write. */
public final class OwnedHierarchyEditorPage extends BasePage {
    public OwnedHierarchyEditorPage(Page page) { super(page); }

    public OwnedHierarchyEditorPage updateCompanyContactTitle(AdminScenarioData data) {
        openOwnedListLink(data, data.companyName());
        page.waitForURL("**/client-and-matter/client/*/view");
        openEditor("Company");
        assertCompanyOwner(data);
        Locator title = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Title").setExact(true));
        assertThat(title).hasValue(data.contactTitle());
        title.fill("Regional Operations Director");
        action("Update Company").click();
        page.waitForURL("**/client-and-matter/client/*/view");
        page.reload();
        openEditor("Company");
        assertCompanyOwner(data);
        assertThat(title).hasValue("Regional Operations Director");
        link("Cancel").click();
        return this;
    }

    public OwnedHierarchyEditorPage updatePropertyUnits(AdminScenarioData data) {
        openOwnedListLink(data, data.propertyName());
        page.waitForURL(Pattern.compile(".*/property/\\d+$"));
        openEditor("Property");
        Locator notes = page.getByPlaceholder("Add any important notes about this property...",
                new Page.GetByPlaceholderOptions().setExact(true));
        assertThat(notes).hasValue(data.ownershipNote());
        assertThat(page.locator("input[name='business_licenses[0][descriptor]']")).hasValue(data.rentalOfficeAddress());
        assertThat(page.locator("input[name='rad_numbers[0][descriptor]']")).hasValue(data.rentalOfficeAddress());
        Locator units = page.locator("input[name='total_units']");
        assertThat(units).hasValue(data.totalUnits());
        units.fill("52");
        page.locator("select[name='quadrant']").selectOption("NW");
        action("Update Property").click();
        page.waitForURL(Pattern.compile(".*/property/\\d+$"));
        page.reload();
        openEditor("Property");
        assertThat(notes).hasValue(data.ownershipNote());
        assertThat(units).hasValue("52");
        assertThat(page.locator("select[name='quadrant']")).hasValue("NW");
        link("Cancel").click();
        return this;
    }

    public OwnedHierarchyEditorPage updateMatterPriority(AdminScenarioData data) {
        new Sidebar(page).goToClients().openOwnedMatter(data.tenantName(), data.caseNumber());
        openEditor("Matter");
        Locator notes = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Case Description / Notes").setExact(true));
        assertThat(notes).hasValue(data.ownershipNote());
        assertThat(page.getByPlaceholder("Search or type tenant name...",
                new Page.GetByPlaceholderOptions().setExact(true))).hasValue(data.tenantName());
        Locator high = page.getByRole(AriaRole.RADIO,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*High$")));
        high.check();
        action("Save Changes").click();
        assertVisible(page.getByText("Matter updated successfully.", new Page.GetByTextOptions().setExact(true)));
        page.reload();
        page.waitForURL(Pattern.compile(".*/matters/\\d+/edit$"));
        assertThat(notes).hasValue(data.ownershipNote());
        assertThat(high).isChecked();
        assertThat(page.getByPlaceholder("Search or type tenant name...",
                new Page.GetByPlaceholderOptions().setExact(true))).hasValue(data.tenantName());
        link("Cancel").click();
        new Sidebar(page).goToClients().assertOwnedMatterHierarchy(data);
        return this;
    }

    private void assertCompanyOwner(AdminScenarioData data) {
        assertThat(page.getByPlaceholder("Add any important notes about this client...",
                new Page.GetByPlaceholderOptions().setExact(true))).hasValue(data.ownershipNote());
        assertThat(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter company name").setExact(true))).hasValue(data.companyName());
        assertThat(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Email").setExact(true))).hasValue(data.contactEmail());
    }

    private void openOwnedListLink(AdminScenarioData data, String name) {
        new Sidebar(page).goToClients().searchFor(data.caseNumber());
        Locator row = page.getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(data.caseNumber()));
        row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(name).setExact(true)).click();
    }

    private void openEditor(String entity) {
        Locator editor = entity.equals("Property")
                ? page.locator(".header-actions.rz-page-actions").getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName(Pattern.compile(".*Edit Property$")))
                : page.getByRole(AriaRole.LINK,
                    new Page.GetByRoleOptions().setName(Pattern.compile(".*Edit " + entity + "$")));
        editor.click();
        page.waitForURL("**/edit");
    }

    private Locator action(String label) {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*" + label + "$")));
    }
}
