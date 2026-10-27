package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page object for the Client Portal Property Spreadsheets module (/spreadsheets)
 * and individual Property Spreadsheet ledger (/property/{id}/spreadsheet).
 */
public final class ClientPortalSpreadsheetsPage extends BasePage {

    private final Locator heading;
    private final Locator searchInput;
    private final Locator recordCount;
    private final Locator spreadsheetsTable;

    public ClientPortalSpreadsheetsPage(Page page) {
        super(page);
        this.heading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Spreadsheets").setExact(true));
        this.searchInput = page.locator("#searchInput");
        this.recordCount = page.locator("#ssRecordCount");
        this.spreadsheetsTable = page.locator("#spreadsheetsTable");
    }

    public ClientPortalSpreadsheetsPage assertLoaded() {
        assertVisible(heading);
        assertVisible(spreadsheetsTable);
        assertVisible(recordCount);
        return this;
    }

    public ClientPortalSpreadsheetsPage assertTableColumns() {
        for (String col : List.of("Property Management Company", "Property", "Active Cases")) {
            assertThat(spreadsheetsTable.getByRole(AriaRole.COLUMNHEADER,
                    new Locator.GetByRoleOptions().setName(col).setExact(true))).isVisible();
        }
        return this;
    }

    public ClientPortalSpreadsheetsPage searchSpreadsheet(String query) {
        assertVisible(searchInput);
        searchInput.fill(query);
        page.waitForTimeout(300);
        assertThat(searchInput).hasValue(query);
        return this;
    }

    public ClientPortalSpreadsheetsPage clearSearch() {
        searchInput.fill("");
        page.waitForTimeout(300);
        assertThat(searchInput).hasValue("");
        return this;
    }

    public int getRecordCount() {
        String text = recordCount.textContent().trim();
        return Integer.parseInt(text.replaceAll("[^0-9]", ""));
    }

    public ClientPortalSpreadsheetsPage openFirstPropertySpreadsheet() {
        Locator firstPropLink = page.locator("a.ss-property-name").first();
        assertVisible(firstPropLink);
        firstPropLink.click();
        page.waitForURL(Pattern.compile(".*/property/\\d+/spreadsheet.*"));
        return this;
    }

    public ClientPortalSpreadsheetsPage assertPropertySpreadsheetViewLoaded() {
        assertThat(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*— Spreadsheet.*")))).isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Export Spreadsheet").setExact(false))).isVisible();
        return this;
    }
}
