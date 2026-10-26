package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

import java.util.LinkedHashMap;
import java.util.Map;

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

    /** Observation-only contract for the visible Company form; no values are entered. */
    public CompanyFormPage assertObservedFieldContract() {
        for (String name : new String[]{"Enter company name", "Name", "Title", "Email", "Phone number", "Cell phone number"}) {
            assertVisible(page.getByRole(AriaRole.TEXTBOX,
                    new Page.GetByRoleOptions().setName(name).setExact(true)));
        }
        for (String placeholder : new String[]{"Enter street address", "Enter suite/unit", "Enter city", "Enter state", "Enter zip code", "Add any important notes about this client..."}) {
            assertVisible(page.getByPlaceholder(placeholder,
                    new Page.GetByPlaceholderOptions().setExact(true)));
        }
        assertVisible(button("Create Company"));
        return this;
    }

    /**
     * Captures the browser-exposed validation metadata without attempting a
     * validation submission.  Native/custom validation message text remains
     * deliberately unknown until the client authorizes a non-persisting probe.
     */
    public Map<String, ObservedFieldConstraint> observeValidationConstraints() {
        Map<String, ObservedFieldConstraint> constraints = new LinkedHashMap<>();
        constraints.put("companyName", observeConstraint("companyName", page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter company name").setExact(true))));
        constraints.put("contactName", observeConstraint("contactName", page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Name").setExact(true))));
        constraints.put("contactEmail", observeConstraint("contactEmail", page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Email").setExact(true))));
        constraints.put("companyStreetAddress", observeConstraint("companyStreetAddress",
                page.getByPlaceholder("Enter street address", new Page.GetByPlaceholderOptions().setExact(true))));
        constraints.put("companyZipCode", observeConstraint("companyZipCode",
                page.getByPlaceholder("Enter zip code", new Page.GetByPlaceholderOptions().setExact(true))));
        return Map.copyOf(constraints);
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
