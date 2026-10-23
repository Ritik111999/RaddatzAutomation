package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

/** Admin-only Company form. Welcome email is intentionally never activated. */
public final class CompanyFormPage extends BasePage {
    public CompanyFormPage(Page page) {
        super(page);
    }

    public CompanyFormPage assertLoaded() {
        assertVisible(heading("Add New Company"));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter company name").setExact(true)));
        return this;
    }

    public ClientMatterListPage createOwnedCompany(AdminScenarioData data) {
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter company name").setExact(true)).fill(data.companyName());
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Name").setExact(true)).fill(data.contactName());
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Title").setExact(true)).fill(data.contactTitle());
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Email").setExact(true)).fill(data.contactEmail());
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Phone number").setExact(true)).fill(data.officePhone());
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Cell phone number").setExact(true)).fill(data.mobilePhone());
        page.getByPlaceholder("Enter street address",
                new Page.GetByPlaceholderOptions().setExact(true)).fill(data.companyStreetAddress());
        page.getByPlaceholder("Enter suite/unit",
                new Page.GetByPlaceholderOptions().setExact(true)).fill(data.companySuite());
        page.getByPlaceholder("Enter city",
                new Page.GetByPlaceholderOptions().setExact(true)).fill(data.companyCity());
        page.getByPlaceholder("Enter state",
                new Page.GetByPlaceholderOptions().setExact(true)).fill(data.companyState());
        page.getByPlaceholder("Enter zip code",
                new Page.GetByPlaceholderOptions().setExact(true)).fill(data.companyZipCode());
        page.getByPlaceholder("Add any important notes about this client...",
                new Page.GetByPlaceholderOptions().setExact(true)).fill(data.ownershipNote());

        button("Create Company").click();
        page.waitForURL("**/client-and-matter");
        return new ClientMatterListPage(page).assertLoaded();
    }
}
