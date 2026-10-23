package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Read-only list view for companies, properties, and matters. */
public final class ClientMatterListPage extends BasePage {
    public ClientMatterListPage(Page page) {
        super(page);
    }

    public ClientMatterListPage assertLoaded() {
        assertVisible(heading("Companies & Matters"));
        assertVisible(page.getByPlaceholder("Search clients, cases, or tenants...",
                new Page.GetByPlaceholderOptions().setExact(true)));
        assertVisible(page.getByRole(AriaRole.TABLE));
        assertThat(page.getByRole(AriaRole.TABLE).getByRole(AriaRole.COLUMNHEADER)).hasCount(8);
        return this;
    }

    public CompanyFormPage openAddCompany() {
        link("+ Add New Company").click();
        page.waitForURL("**/client-and-matter/add-client");
        return new CompanyFormPage(page).assertLoaded();
    }

    public PropertyFormPage openAddProperty() {
        link("+ Add Property").click();
        page.waitForURL("**/property/create");
        return new PropertyFormPage(page).assertLoaded();
    }

    public MatterFormPage openAddMatter() {
        link("+ Add New Matter").click();
        page.waitForURL("**/create-matter");
        return new MatterFormPage(page).assertLoaded();
    }

    public ClientMatterListPage searchFor(String scenarioOwnedName) {
        page.getByPlaceholder("Search clients, cases, or tenants...",
                new Page.GetByPlaceholderOptions().setExact(true)).fill(scenarioOwnedName);
        return this;
    }

    public ClientMatterListPage assertCompanyIsListed(String scenarioOwnedName) {
        return assertListedName(scenarioOwnedName);
    }

    public ClientMatterListPage assertListedName(String scenarioOwnedName) {
        assertVisible(page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(scenarioOwnedName).setExact(true)));
        return this;
    }

    /** Confirms the Matter is displayed below the exact owned Company and Property branch. */
    public ClientMatterListPage assertOwnedMatterHierarchy(AdminScenarioData data) {
        searchFor(data.caseNumber());
        Locator ownedMatterRow = page.getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(data.caseNumber()));
        assertVisible(ownedMatterRow);
        assertVisible(ownedMatterRow.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(data.companyName()).setExact(true)));
        assertVisible(ownedMatterRow.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(data.propertyName()).setExact(true)));
        assertVisible(ownedMatterRow.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(data.tenantName()).setExact(true)));
        return this;
    }

    /** Confirms both owned tenants are displayed on the uniquely identified Matter row. */
    public ClientMatterListPage assertOwnedMultiTenantMatterListed(
            AdminScenarioData primaryTenant, AdminScenarioData additionalTenant) {
        searchFor(primaryTenant.caseNumber());
        Locator ownedMatterRow = page.getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(primaryTenant.caseNumber()));
        assertVisible(ownedMatterRow);
        Locator matterLink = ownedMatterRow.locator("a[href*='/matters/']");
        assertVisible(matterLink);
        assertThat(matterLink).containsText(primaryTenant.tenantName());
        assertThat(matterLink).containsText(additionalTenant.tenantName());
        return this;
    }

    /** Opens only the Matter row bound to this run's unique case number. */
    public MatterWorkspacePage openOwnedMatter(String tenantName, String caseNumber) {
        searchFor(caseNumber);
        var ownedMatterRow = page.getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText(caseNumber));
        assertVisible(ownedMatterRow);
        // The Case # is plain text and multi-tenant names are composite. Within
        // the already case-scoped row, this route is the only Matter destination.
        Locator ownedMatterLink = ownedMatterRow.locator("a[href*='/matters/']");
        assertVisible(ownedMatterLink);
        ownedMatterLink.click();
        page.waitForURL(Pattern.compile(".*/matters/\\d+/general$"));
        return new MatterWorkspacePage(page).assertLoaded(tenantName);
    }
}
