package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

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
}
