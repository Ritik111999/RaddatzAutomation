package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.data.OwnedRecordIds;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Admin-only Property form. A property is always attached to an explicitly selected,
 * automation-owned Company; no existing company or property is edited.
 */
public final class PropertyFormPage extends BasePage {
    public PropertyFormPage(Page page) {
        super(page);
    }

    public PropertyFormPage assertLoaded() {
        assertVisible(heading("Add New Property"));
        assertVisible(page.getByPlaceholder("Search client name...",
                new Page.GetByPlaceholderOptions().setExact(true)));
        assertVisible(page.locator("input[name='property_name']"));
        return this;
    }

    public PropertyDetailsPage createOwnedProperty(AdminScenarioData data) {
        selectOwnedCompany(data);
        page.locator("input[name='property_name']").fill(data.propertyName());
        page.locator("input[name='property_type']").fill(data.propertyType());
        page.locator("textarea[name='rental_office_address']").fill(data.rentalOfficeAddress());
        // Backend calls these fields descriptor; the actual visible label is Address.
        page.locator("input[name='business_licenses[0][descriptor]']").fill(data.businessLicenseDescriptor());
        page.locator("input[name='business_licenses[0][number]']").fill(data.businessLicenseNumber());
        page.locator("input[name='rad_numbers[0][descriptor]']").fill(data.radDescriptor());
        page.locator("input[name='rad_numbers[0][number]']").fill(data.radNumber());
        page.locator("input[name='rad_numbers[0][expiration_date]']").fill(data.radExpirationDate());
        page.locator("input[name='total_units']").fill(data.totalUnits());
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
        page.getByPlaceholder("0.00", new Page.GetByPlaceholderOptions().setExact(true)).fill(data.propertyHourlyRate());
        page.getByPlaceholder("Add any important notes about this property...",
                new Page.GetByPlaceholderOptions().setExact(true)).fill(data.ownershipNote());
        page.locator("#propertyStatus").check();

        button("Save Property").click();
        page.waitForURL(Pattern.compile(".*/property/\\d+$"));
        OwnedRecordIds.property(data.businessLicenseNumber(),
                java.net.URI.create(page.url()).getPath().replaceFirst(".*/", ""));
        return new PropertyDetailsPage(page).assertLoaded(data.propertyName());
    }

    private void selectOwnedCompany(AdminScenarioData data) {
        Locator clientSearch = page.getByPlaceholder("Search client name...",
                new Page.GetByPlaceholderOptions().setExact(true));
        clientSearch.fill(data.companyName());
        Locator candidates = page.locator("#clientDropdown .client-option");
        assertThat(candidates).not().hasCount(0);
        java.util.List<String> ids = new java.util.ArrayList<>();
        for (Locator candidate : candidates.all()) {
            if (data.companyName().equals(candidate.getAttribute("data-name"))) ids.add(candidate.getAttribute("data-id"));
        }
        String origin = java.net.URI.create(page.url()).resolve("/").toString();
        String ownerId = null;
        for (String id : ids) {
            if (id == null || !id.matches("[1-9][0-9]*")) throw new IllegalStateException("Invalid Company suggestion identity");
            // Normal UI inspection of a candidate; no form is submitted until ownership is proven.
            page.navigate(origin + "client-and-matter/client/" + id + "/edit");
            assertVisible(heading("Edit Company"));
            boolean emailMatches = false;
            for (Locator email : page.getByRole(AriaRole.TEXTBOX,
                    new Page.GetByRoleOptions().setName("Email").setExact(true)).all()) {
                if (data.contactEmail().equals(email.inputValue())) emailMatches = true;
            }
            if (!emailMatches) continue;
            assertThat(page.getByRole(AriaRole.TEXTBOX,
                    new Page.GetByRoleOptions().setName("Enter company name").setExact(true))).hasValue(data.companyName());
            assertThat(page.getByPlaceholder("Add any important notes about this client...",
                    new Page.GetByPlaceholderOptions().setExact(true)))
                    .hasValue(Pattern.compile("Scenario reference [0-9]{8}-[0-9]{6}; created for the local admin automation suite\\."));
            ownerId = id;
            break;
        }
        if (ownerId == null) throw new IllegalStateException("No Company suggestion matched the current owned contact email");
        OwnedRecordIds.company(data.contactEmail(), ownerId);
        // This is the observed Company-specific Add Property UI route; its parent is prebound and non-editable.
        page.navigate(origin + "property/create?client_id=" + ownerId);
        assertVisible(heading("Add New Property"));
        assertThat(page.locator("input[name='client_id']")).hasValue(ownerId);
    }
}
