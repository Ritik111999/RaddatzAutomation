package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

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

    /** Opens only the Matter row bound to this run's unique case number. */
    public MatterWorkspacePage openOwnedMatter(String tenantName, String caseNumber) {
        searchFor(caseNumber);
        var ownedMatterRow = page.getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText(caseNumber));
        assertVisible(ownedMatterRow);
        Locator ownedMatterLink = ownedMatterRow.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(tenantName).setExact(true));
        assertVisible(ownedMatterLink);
        ownedMatterLink.click();
        page.waitForURL(Pattern.compile(".*/matters/\\d+/general$"));
        return new MatterWorkspacePage(page).assertLoaded(tenantName);
    }
}
