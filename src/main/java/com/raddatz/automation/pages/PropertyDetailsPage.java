package com.raddatz.automation.pages;

import com.microsoft.playwright.Page;

/** Read-only confirmation view shown after a newly owned Property is saved. */
public final class PropertyDetailsPage extends BasePage {
    public PropertyDetailsPage(Page page) {
        super(page);
    }

    public PropertyDetailsPage assertLoaded(String propertyName) {
        assertVisible(heading(propertyName));
        return this;
    }
}
