package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.data.OwnedRecordIds;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Admin-only Matter form. It selects the owned Company and Property before entering
 * tenant and case details; existing matters are never opened or changed.
 */
public final class MatterFormPage extends BasePage {
    public MatterFormPage(Page page) {
        super(page);
    }

    public MatterFormPage assertLoaded() {
        assertVisible(heading("Create New Matter"));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Company (Property Management Company)*").setExact(true)));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Property*").setExact(true)));
        return this;
    }

    public ClientMatterListPage createOwnedMatter(AdminScenarioData data) {
        selectOwnedCompany(data);
        selectOwnedProperty(data);
        fillFirstTenant(data);
        fillMatterCaseDetails(data);

        button("Create Matter").click();
        page.waitForURL("**/client-and-matter");
        return new ClientMatterListPage(page).assertLoaded();
    }

    /** Creates one Matter with two fully populated, distinct tenants. */
    public ClientMatterListPage createOwnedMatterWithAdditionalTenant(
            AdminScenarioData primaryTenant, AdminScenarioData additionalTenant) {
        selectOwnedCompany(primaryTenant);
        selectOwnedProperty(primaryTenant);
        fillFirstTenant(primaryTenant);
        button("+ Add Another Tenant").click();
        fillAdditionalTenant(additionalTenant);
        fillMatterCaseDetails(primaryTenant);

        button("Create Matter").click();
        page.waitForURL("**/client-and-matter");
        return new ClientMatterListPage(page).assertLoaded();
    }

    private void fillFirstTenant(AdminScenarioData data) {
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Full tenant name").setExact(true)).fill(data.tenantName());
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Street address").setExact(true)).fill(data.tenantStreetAddress());
        page.locator("select[name='tenants[0][quadrant]']").selectOption(data.tenantQuadrant());
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("e.g. 4B").setExact(true)).fill(data.tenantUnitNumber());
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("City").setExact(true))
                .fill(data.tenantCity());
        page.getByPlaceholder("e.g. 32803 or 32803-1234",
                new Page.GetByPlaceholderOptions().setExact(true)).fill(data.tenantZipCode());
        button("+ Add Phone").click();
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("(202) 555-0100").setExact(true)).fill(data.tenantPhone());
        button("+ Add Email").click();
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Email address").setExact(true)).fill(data.tenantEmail());
    }

    private void fillAdditionalTenant(AdminScenarioData data) {
        // Tenant #2 is the stable visible heading that scopes duplicated labels.
        Locator secondTenant = page.getByText("Tenant #2", new Page.GetByTextOptions().setExact(true))
                .locator("..").locator("..");
        secondTenant.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("Full tenant name").setExact(true)).fill(data.tenantName());
        secondTenant.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("Street address").setExact(true)).fill(data.tenantStreetAddress());
        secondTenant.locator("select").selectOption(data.tenantQuadrant());
        secondTenant.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("e.g. 4B").setExact(true)).fill(data.tenantUnitNumber());
        secondTenant.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("City").setExact(true)).fill(data.tenantCity());
        secondTenant.getByPlaceholder("e.g. 32803 or 32803-1234",
                new Locator.GetByPlaceholderOptions().setExact(true)).fill(data.tenantZipCode());
        secondTenant.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("+ Add Phone").setExact(true)).click();
        secondTenant.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("(202) 555-0100").setExact(true)).fill(data.tenantPhone());
        secondTenant.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("+ Add Email").setExact(true)).click();
        secondTenant.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("Email address").setExact(true)).fill(data.tenantEmail());
    }

    private void fillMatterCaseDetails(AdminScenarioData data) {
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Case Number (leave blank to auto-generate)").setExact(true))
                .fill(data.caseNumber());
        page.locator("select[name='case_type']").selectOption(data.caseType());
        page.locator("select[name='case_status']").selectOption(data.caseStatus());
        page.locator("select[name='matter_plan']").selectOption(data.matterPlan());
        page.locator("#openingDate").fill(data.openingDate());
        page.getByRole(AriaRole.RADIO,
                new Page.GetByRoleOptions().setName(Pattern.compile(data.casePriority())).setExact(false)).check();
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Case Description / Initial Notes").setExact(true))
                .fill(data.ownershipNote());
    }

    private void selectOwnedCompany(AdminScenarioData data) {
        Locator companySearch = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Company (Property Management Company)*").setExact(true));
        companySearch.fill(data.companyName());
        String ownedId = OwnedRecordIds.company(data.contactEmail());
        Locator exactCandidate = page.locator("#companyDropdown .company-option[data-id='" + ownedId + "']");
        assertVisible(exactCandidate);
        assertThat(exactCandidate).hasText(data.companyName());
        exactCandidate.click();
        assertThat(page.locator("#companyIdInput")).hasValue(OwnedRecordIds.company(data.contactEmail()));
    }

    private void selectOwnedProperty(AdminScenarioData data) {
        Locator propertySearch = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Property*").setExact(true));
        propertySearch.fill(data.propertyName());
        String ownedId = OwnedRecordIds.property(data.businessLicenseNumber());
        Locator exactCandidate = page.locator("#propertyDropdown .property-option[data-id='" + ownedId + "']");
        assertVisible(exactCandidate);
        assertThat(exactCandidate).hasText(data.propertyName());
        exactCandidate.click();
        assertThat(page.locator("#propertyIdInput")).hasValue(OwnedRecordIds.property(data.businessLicenseNumber()));
    }
}
