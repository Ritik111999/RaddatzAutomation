package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

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
        selectOwnedCompany(data.companyName());
        page.locator("input[name='property_name']").fill(data.propertyName());
        page.locator("input[name='property_type']").fill(data.propertyType());
        page.locator("textarea[name='rental_office_address']").fill(data.rentalOfficeAddress());
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
        page.locator("#portalAccess").check();

        button("Save Property").click();
        page.waitForURL(Pattern.compile(".*/property/\\d+$"));
        return new PropertyDetailsPage(page).assertLoaded(data.propertyName());
    }

    private void selectOwnedCompany(String companyName) {
        Locator clientSearch = page.getByPlaceholder("Search client name...",
                new Page.GetByPlaceholderOptions().setExact(true));
        clientSearch.fill(companyName);
        Locator exactCandidate = page.locator("#clientDropdown").filter(
                new Locator.FilterOptions().setHasText(companyName));
        assertVisible(exactCandidate);
        exactCandidate.click();
        assertThat(page.locator("#clientIdHidden")).hasAttribute("value", Pattern.compile(".+"));
    }
}
