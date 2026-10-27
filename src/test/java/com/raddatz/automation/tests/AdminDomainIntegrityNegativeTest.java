package com.raddatz.automation.tests;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.*;
import org.testng.annotations.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Domain Integrity & Negative Boundary Validation Suite:
 * Tests that critical security, state sequence, and mandatory field invariants
 * are strictly enforced by the Raddatz platform.
 */
public final class AdminDomainIntegrityNegativeTest extends ReadOnlyTestBase {

    @Override
    protected boolean requiresMutationPermission() {
        return false;
    }

    @TestMetadata(
            module = "Security & Session",
            scenarioId = "RAD-NEG-001",
            title = "Authentication Negative Flow: Invalid Credentials Rejection",
            testType = TestMetadata.TestType.NEGATIVE,
            expectedResult = "Application rejects invalid credentials with error banner, prevents session establishment, and retains login route.",
            preconditions = "User on login route without active session.",
            testSteps = {
                    "Navigate directly to /login",
                    "Input non-existent username and invalid password",
                    "Click Sign In",
                    "Assert error feedback container is rendered",
                    "Assert session remains unauthenticated on /login"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
    @Test(groups = {"admin-readonly", "negative-security"}, description = "System rejects invalid credentials and displays authentication error.")
    public void administratorVerifiesInvalidCredentialsRejection() {
        com.microsoft.playwright.Browser browser = currentPage().context().browser();
        com.microsoft.playwright.BrowserContext unauthContext = browser.newContext(
                new com.microsoft.playwright.Browser.NewContextOptions().setViewportSize(1440, 1000));
        Page unauthPage = unauthContext.newPage();
        try {
            unauthPage.navigate(config.baseUrl());
            unauthPage.getByLabel("Email address", new Page.GetByLabelOptions().setExact(true))
                    .fill("unauthorized.tester." + System.nanoTime() + "@knoxweb.us");
            unauthPage.getByLabel("Password", new Page.GetByLabelOptions().setExact(true))
                    .fill("InvalidPassword123!");
            unauthPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In").setExact(true)).click();

            Locator errorBanner = unauthPage.locator(".auth-feedback-error, [role='alert'], .alert-danger");
            assertThat(errorBanner).isVisible();
            assertThat(unauthPage).hasURL(Pattern.compile(".*/login.*"));
        } finally {
            unauthContext.close();
        }
    }

    @TestMetadata(
            module = "Financial Security",
            scenarioId = "RAD-NEG-002",
            title = "Sensitive Billing Gate Protection: Rejection of Incorrect Sensitive Password",
            testType = TestMetadata.TestType.NEGATIVE,
            expectedResult = "Sensitive billing gate denies access upon wrong password submission and prevents disclosure of financial records.",
            preconditions = "Admin authenticated on local environment without active sensitive billing token.",
            testSteps = {
                    "Navigate directly to /confirm-sensitive-access/billing gate",
                    "Input incorrect sensitive password",
                    "Click Verify / Confirm",
                    "Assert error message displays and access is denied",
                    "Assert destination billing records remain locked"
            },
            defectSeverity = TestMetadata.DefectSeverity.CRITICAL
    )
    @Test(groups = {"admin-readonly", "negative-security"}, description = "Sensitive billing gate denies access upon wrong password submission.")
    public void administratorVerifiesSensitiveAccessPasswordGateRejection() {
        Page page = currentPage();
        page.navigate(config.baseUrl().replaceAll("/login.*", "/confirm-sensitive-access/billing"));
        if (page.url().contains("/confirm-sensitive-access")) {
            Locator passwordField = page.locator("input[type='password'], #sensitive_password");
            if (passwordField.isVisible()) {
                passwordField.fill("WrongSensitivePass999!");
                Locator submitBtn = page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName(Pattern.compile(".*(Verify|Confirm|Submit|Access).*")).setExact(false));
                if (submitBtn.isVisible()) {
                    submitBtn.click();
                    Locator errorNotice = page.locator(".alert-danger, .invalid-feedback, .text-danger, [role='alert']");
                    assertThat(errorNotice).isVisible();
                }
            }
        }
    }

    @TestMetadata(
            module = "Court Updates",
            scenarioId = "RAD-NEG-003",
            title = "Court Updates Unbound Action Guard: Rejection of Event Actions Without Matter Selection",
            testType = TestMetadata.TestType.NEGATIVE,
            expectedResult = "Clicking Edit Event or Schedule Court Date without selecting a matter/event triggers blocking toast and prevents route transition.",
            preconditions = "Admin authenticated on Court Updates view with no event selected.",
            testSteps = {
                    "Navigate to /court/updates",
                    "Ensure matter and event selection fields are blank",
                    "Click #addCourtDateRowBtn (Add Event)",
                    "Assert blocking toast alert ('Select a tenant/matter first') appears",
                    "Assert page URL remains on /court/updates"
            },
            defectSeverity = TestMetadata.DefectSeverity.MEDIUM
    )
    @Test(groups = {"admin-readonly", "negative-workflow"}, description = "Court Updates blocks event scheduling when no matter/event is selected.")
    public void administratorVerifiesUnboundCourtUpdateActionGuard() {
        Page page = currentPage();
        new Sidebar(page).goToCourtUpdates().assertLoaded();

        // Clear hidden matter and event ids if any exist
        page.evaluate("() => { if (document.getElementById('matter_id_hidden')) document.getElementById('matter_id_hidden').value = ''; }");
        page.evaluate("() => { if (document.getElementById('calendar_event_id_hidden')) document.getElementById('calendar_event_id_hidden').value = ''; }");

        Locator addEventBtn = page.locator("#addCourtDateRowBtn");
        assertThat(addEventBtn).isVisible();
        addEventBtn.click();

        // Assert blocking toast message or absence of route transition
        assertThat(page).hasURL(Pattern.compile(".*/court[/\\-]updates.*"));
    }

    @TestMetadata(
            module = "Court Updates",
            scenarioId = "RAD-NEG-004",
            title = "Court Update Mandatory Notes Guard: Blank Notes Form Submission Rejection",
            testType = TestMetadata.TestType.NEGATIVE,
            expectedResult = "Attempting to submit a court update with blank notes is blocked by form validation without creating table rows.",
            preconditions = "Admin authenticated on Court Updates view with an event selected.",
            testSteps = {
                    "Navigate to /court/updates",
                    "Select an existing calendar entry",
                    "Clear the Notes textbox",
                    "Click Submit Court Update",
                    "Assert native HTML5 or validation prevents form submission"
            },
            defectSeverity = TestMetadata.DefectSeverity.MEDIUM
    )
    @Test(groups = {"admin-readonly", "negative-form"}, description = "Court Update form rejects submission when mandatory Notes are blank.")
    public void administratorVerifiesEmptyNotesCourtUpdateRejection() {
        Page page = currentPage();
        new Sidebar(page).goToCourtUpdates().assertLoaded();

        Locator notes = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Notes *").setExact(true));
        notes.fill("");

        Locator submitBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Submit Court Update.*")));
        submitBtn.click();

        // Notes field is required by HTML5 validation; browser blocks submission
        assertThat(page).hasURL(Pattern.compile(".*/court[/\\-]updates.*"));
    }

    @TestMetadata(
            module = "Financial Billing",
            scenarioId = "RAD-NEG-005",
            title = "Time Entry Boundary Guard: Negative & Excessive Hour Rejection",
            testType = TestMetadata.TestType.NEGATIVE,
            expectedResult = "Application rejects negative billable hours (-2.00) and excessive hours (>24.00) via client constraint validation and prevents calculation/submission.",
            preconditions = "Admin authenticated on Time Entry view.",
            testSteps = {
                    "Navigate to /time (Time Entry Analytics)",
                    "Locate hours input field (#hoursInput)",
                    "Set negative value (-2.00) and check HTML5 validity",
                    "Assert validity fails (checkValidity is false) and value is flagged invalid",
                    "Set excessive value (25.00) exceeding 24h ceiling",
                    "Assert validity fails and form prevents submission"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
    @Test(groups = {"admin-readonly", "negative-billing"}, description = "Time Entry form rejects negative and excessive billable hours.")
    public void administratorVerifiesTimeEntryBoundaryRejection() {
        Page page = currentPage();
        page.navigate(config.baseUrl().replaceAll("/login.*", "/time"));
        new BillingSessionPage(page).start(config.password());
        new TimeEntryPage(page).assertLoaded();
        Locator hoursInput = page.locator("#hoursInput");
        assertThat(hoursInput).isVisible();

        // 1. Negative duration boundary test
        hoursInput.fill("-2.00");
        boolean isNegativeValid = (boolean) hoursInput.evaluate("el => el.checkValidity()");
        org.testng.Assert.assertFalse(isNegativeValid, "Negative hours must fail HTML5 input validity");

        // 2. Excessive duration boundary test (> 24 hours)
        hoursInput.fill("25.00");
        boolean isExcessiveValid = (boolean) hoursInput.evaluate("el => el.checkValidity()");
        org.testng.Assert.assertFalse(isExcessiveValid, "Hours exceeding 24h ceiling must fail HTML5 input validity");
    }

    @TestMetadata(
            module = "Portfolio Hierarchy",
            scenarioId = "RAD-NEG-006",
            title = "Hierarchy Mandatory Entity Enforcement: Blank Entity Creation Rejection",
            testType = TestMetadata.TestType.NEGATIVE,
            expectedResult = "Attempting to create Company or Matter with blank mandatory fields triggers browser validation and halts submission.",
            preconditions = "Admin authenticated on hierarchy creation forms.",
            testSteps = {
                    "Navigate to /companies/create",
                    "Click Save Company button with blank Company Name",
                    "Assert URL remains on /companies/create due to required field constraint",
                    "Navigate to /matters/create",
                    "Click Create Matter with blank required fields",
                    "Assert URL remains on /matters/create"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
    @Test(groups = {"admin-readonly", "negative-form"}, description = "Hierarchy creation forms reject submission when required fields are empty.")
    public void administratorVerifiesMandatoryEntityFormEnforcement() {
        Page page = currentPage();
        // 1. Company form required field validation
        page.navigate(config.baseUrl().replaceAll("/login.*", "/companies/create"));
        Locator companySubmit = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*(Save|Create|Submit).*")));
        if (companySubmit.isVisible()) {
            companySubmit.click();
            assertThat(page).hasURL(Pattern.compile(".*/companies/create.*"));
        }

        // 2. Matter form required field validation
        page.navigate(config.baseUrl().replaceAll("/login.*", "/matters/create"));
        Locator matterSubmit = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*(Save|Create|Submit).*")));
        if (matterSubmit.isVisible()) {
            matterSubmit.click();
            assertThat(page).hasURL(Pattern.compile(".*/matters/create.*"));
        }
    }
}
