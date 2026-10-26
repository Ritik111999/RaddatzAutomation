package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public abstract class BasePage {
    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    protected Locator button(String name) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
    }

    protected Locator link(String name) {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name).setExact(true));
    }

    protected Locator heading(String name) {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(name).setExact(true));
    }

    protected Locator heading(Pattern pattern) {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(pattern));
    }

    protected void assertVisible(Locator locator) {
        assertThat(locator).isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions().setTimeout(15000));
    }

    /** Reads only rendered control metadata; it never focuses, fills, or submits the control. */
    protected ObservedFieldConstraint observeConstraint(String control, Locator locator) {
        assertVisible(locator);
        return new ObservedFieldConstraint(
                control,
                absentAsEmpty(locator.getAttribute("type")),
                locator.getAttribute("required") != null,
                absentAsEmpty(locator.getAttribute("min")),
                absentAsEmpty(locator.getAttribute("max")),
                absentAsEmpty(locator.getAttribute("maxlength")),
                absentAsEmpty(locator.getAttribute("pattern")),
                absentAsEmpty(locator.getAttribute("aria-required")));
    }

    private String absentAsEmpty(String value) {
        return value == null ? "" : value;
    }
}
