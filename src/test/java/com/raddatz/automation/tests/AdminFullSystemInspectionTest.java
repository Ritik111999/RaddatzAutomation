package com.raddatz.automation.tests;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.pages.BillingSessionPage;
import com.raddatz.automation.pages.SearchFilterPage;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only physical exploration suite that systematically inspects and screenshots
 * all visible Raddatz screens, extracting interactive controls, cross-screen affordances,
 * and propagation paths without mutating any data.
 */
public final class AdminFullSystemInspectionTest extends ReadOnlyTestBase {

    private final Path screenshotDir = Paths.get("target", "automation-artifacts", "exploration");

    @Test(groups = "admin-readonly", description = "Physically inspect and screenshot all screens across Raddatz to map cross-screen capabilities.")
    public void physicallyInspectAllRaddatzScreens() throws IOException {
        Files.createDirectories(screenshotDir);
        Page page = currentPage();
        Sidebar sidebar = new Sidebar(page);

        logger.info("=== STEP 1: PHYSICAL INSPECTION OF DASHBOARD ===");
        sidebar.goToDashboard();
        takeScreenshot(page, "01_dashboard.png");
        logVisibleButtonsAndLinks(page, "Dashboard");

        logger.info("=== STEP 2: PHYSICAL INSPECTION OF CLIENTS & MATTERS ===");
        sidebar.goToClients();
        takeScreenshot(page, "02_client_and_matter.png");
        logVisibleButtonsAndLinks(page, "Clients & Matters");

        // Inspect Company View if any company exists
        Locator companyLinks = page.locator("a[href*='/client-and-matter/client/']");
        if (companyLinks.count() > 0) {
            String companyHref = companyLinks.first().getAttribute("href");
            logger.info("Inspecting Company Details at {}", companyHref);
            page.navigate(companyHref);
            page.waitForTimeout(1000);
            takeScreenshot(page, "03_company_details.png");
            logVisibleButtonsAndLinks(page, "Company Details");

            // Check Notes tab
            Locator notesTab = page.locator("a:has-text('Notes'), button:has-text('Notes')");
            if (notesTab.count() > 0 && notesTab.first().isVisible()) {
                notesTab.first().click();
                page.waitForTimeout(500);
                takeScreenshot(page, "03b_company_notes.png");
            }

            // Check Documents tab
            Locator docsTab = page.locator("a:has-text('Documents'), button:has-text('Documents')");
            if (docsTab.count() > 0 && docsTab.first().isVisible()) {
                docsTab.first().click();
                page.waitForTimeout(500);
                takeScreenshot(page, "03c_company_documents.png");
            }

            // Inspect Property if listed
            Locator propertyLinks = page.locator("a[href*='/property/']");
            if (propertyLinks.count() > 0) {
                String propHref = propertyLinks.first().getAttribute("href");
                logger.info("Inspecting Property Details at {}", propHref);
                page.navigate(propHref);
                page.waitForTimeout(1000);
                takeScreenshot(page, "04_property_details.png");
                logVisibleButtonsAndLinks(page, "Property Details");

                // Inspect Matter if listed
                Locator matterLinks = page.locator("a[href*='/matters/']");
                if (matterLinks.count() > 0) {
                    String matterHref = matterLinks.first().getAttribute("href");
                    logger.info("Inspecting Matter Workspace at {}", matterHref);
                    page.navigate(matterHref);
                    page.waitForTimeout(1000);
                    takeScreenshot(page, "05_matter_general.png");
                    logVisibleButtonsAndLinks(page, "Matter General");

                    // Inspect Matter Notes
                    Locator matterNotes = page.locator("a:has-text('Notes')");
                    if (matterNotes.count() > 0 && matterNotes.first().isVisible()) {
                        matterNotes.first().click();
                        page.waitForTimeout(500);
                        takeScreenshot(page, "06_matter_notes.png");
                    }

                    // Inspect Matter Documents
                    Locator matterDocs = page.locator("a:has-text('Documents')");
                    if (matterDocs.count() > 0 && matterDocs.first().isVisible()) {
                        matterDocs.first().click();
                        page.waitForTimeout(500);
                        takeScreenshot(page, "07_matter_documents.png");
                    }
                }
            }
        }

        // Directly inspect an existing Matter Workspace from the Clients & Matters grid
        sidebar.goToClients();
        Locator matterGridLinks = page.locator("a[href*='/matters/']");
        if (matterGridLinks.count() > 0) {
            String matterHref = matterGridLinks.first().getAttribute("href");
            logger.info("Directly inspecting Matter Workspace at {}", matterHref);
            page.navigate(matterHref);
            page.waitForTimeout(1000);
            takeScreenshot(page, "05_matter_general.png");
            logVisibleButtonsAndLinks(page, "Matter General");

            Locator notesTab = page.locator("a:has-text('Notes')");
            if (notesTab.count() > 0 && notesTab.first().isVisible()) {
                notesTab.first().click();
                page.waitForTimeout(500);
                takeScreenshot(page, "06_matter_notes.png");
            }

            Locator docsTab = page.locator("a:has-text('Documents')");
            if (docsTab.count() > 0 && docsTab.first().isVisible()) {
                docsTab.first().click();
                page.waitForTimeout(500);
                takeScreenshot(page, "07_matter_documents.png");
            }

            Locator tasksTab = page.locator("a:has-text('Tasks'), a:has-text('To-dos')");
            if (tasksTab.count() > 0 && tasksTab.first().isVisible()) {
                tasksTab.first().click();
                page.waitForTimeout(500);
                takeScreenshot(page, "07b_matter_tasks.png");
            }
        }

        logger.info("=== STEP 3: PHYSICAL INSPECTION OF COURT UPDATES ===");
        sidebar.goToCourtUpdates();
        page.waitForTimeout(1000);
        takeScreenshot(page, "08_court_updates.png");
        logVisibleButtonsAndLinks(page, "Court Updates");

        // Inspect Court Updates Add Todo affordance
        Locator addTodo = page.locator("button:has-text('Add To-do'), button:has-text('Add Task'), button:has-text('To-do')");
        if (addTodo.count() > 0 && addTodo.first().isVisible()) {
            logger.info("Found Add To-do affordance on Court Updates!");
            addTodo.first().click();
            page.waitForTimeout(500);
            takeScreenshot(page, "08b_court_updates_todo_modal.png");
            // Close modal
            Locator closeBtn = page.locator("button:has-text('Close'), button:has-text('Cancel')");
            if (closeBtn.count() > 0 && closeBtn.first().isVisible()) {
                closeBtn.first().click();
            }
        }

        // Inspect Court Updates Add Extra Event affordance
        Locator addEvent = page.locator("button:has-text('Extra Event'), button:has-text('Add Event'), button:has-text('+ Event')");
        if (addEvent.count() > 0 && addEvent.first().isVisible()) {
            logger.info("Found Add Extra Event affordance on Court Updates!");
            addEvent.first().click();
            page.waitForTimeout(500);
            takeScreenshot(page, "08c_court_updates_event_modal.png");
            // Close modal
            Locator closeBtn = page.locator("button:has-text('Close'), button:has-text('Cancel')");
            if (closeBtn.count() > 0 && closeBtn.first().isVisible()) {
                closeBtn.first().click();
            }
        }

        logger.info("=== STEP 4: PHYSICAL INSPECTION OF ALL TASKS / TO-DOS ===");
        sidebar.goToTodos();
        page.waitForTimeout(1000);
        takeScreenshot(page, "09_todos_all.png");
        logVisibleButtonsAndLinks(page, "To-dos");

        logger.info("=== STEP 5: PHYSICAL INSPECTION OF CALENDAR ===");
        sidebar.goToCalendar();
        page.waitForTimeout(1000);
        takeScreenshot(page, "10_calendar.png");
        logVisibleButtonsAndLinks(page, "Calendar");

        logger.info("=== STEP 6: PHYSICAL INSPECTION OF BILLING / COURT TIME / EXPENSES ===");
        page.navigate(config.baseUrl().replaceFirst("/login$", "/time"));
        if (page.url().contains("/confirm-sensitive-access")) {
            new BillingSessionPage(page).start(config.password());
        }
        page.waitForTimeout(1000);
        takeScreenshot(page, "11_billing_time.png");
        logVisibleButtonsAndLinks(page, "Billing & Time");

        // Inspect Expenses
        Locator expensesTab = page.locator("a:has-text('Expenses'), button:has-text('Expenses'), a[href*='expenses']");
        if (expensesTab.count() > 0 && expensesTab.first().isVisible()) {
            expensesTab.first().click();
            page.waitForTimeout(1000);
            takeScreenshot(page, "12_billing_expenses.png");
            logVisibleButtonsAndLinks(page, "Expenses");
        }

        logger.info("=== STEP 7: PHYSICAL INSPECTION OF INVOICE DRAFTS ===");
        page.navigate(config.baseUrl().replaceFirst("/login$", "/invoice/draft"));
        if (page.url().contains("/confirm-sensitive-access")) {
            new BillingSessionPage(page).start(config.password());
        }
        page.waitForTimeout(1000);
        takeScreenshot(page, "13_invoice_draft.png");
        logVisibleButtonsAndLinks(page, "Invoice Drafts");

        logger.info("=== STEP 8: PHYSICAL INSPECTION OF DOCUMENT TEMPLATES ===");
        page.navigate(config.baseUrl().replaceFirst("/login$", "/document-templates"));
        page.waitForTimeout(1000);
        takeScreenshot(page, "14_document_templates.png");
        logVisibleButtonsAndLinks(page, "Document Templates");

        logger.info("=== STEP 9: PHYSICAL INSPECTION OF REPORTS ===");
        new SearchFilterPage(page).openReportsWithApprovedSessionCredential(config.password());
        page.waitForTimeout(1000);
        takeScreenshot(page, "15_reports.png");
        logVisibleButtonsAndLinks(page, "Reports");

        logger.info("=== PHYSICAL INSPECTION COMPLETE ===");
    }

    private void takeScreenshot(Page page, String filename) {
        Path path = screenshotDir.resolve(filename);
        page.screenshot(new Page.ScreenshotOptions().setPath(path).setFullPage(true));
        logger.info("Saved inspection screenshot: {}", path.toAbsolutePath());
    }

    private void logVisibleButtonsAndLinks(Page page, String screenName) {
        Locator buttons = page.getByRole(AriaRole.BUTTON);
        List<String> buttonNames = new ArrayList<>();
        for (int i = 0; i < Math.min(buttons.count(), 20); i++) {
            String text = buttons.nth(i).innerText().replaceAll("\\s+", " ").trim();
            if (!text.isBlank()) buttonNames.add(text);
        }

        Locator links = page.getByRole(AriaRole.LINK);
        List<String> linkNames = new ArrayList<>();
        for (int i = 0; i < Math.min(links.count(), 20); i++) {
            String text = links.nth(i).innerText().replaceAll("\\s+", " ").trim();
            if (!text.isBlank()) linkNames.add(text);
        }

        logger.info("[{}] Buttons found: {}", screenName, buttonNames);
        logger.info("[{}] Links found: {}", screenName, linkNames);
    }
}
