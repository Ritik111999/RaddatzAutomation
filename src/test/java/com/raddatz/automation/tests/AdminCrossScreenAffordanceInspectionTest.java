package com.raddatz.automation.tests;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.pages.BillingSessionPage;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Systematic physical inspection of cross-cutting multi-entrypoint workflows:
 * Court Updates (To-dos & Events), Matter Workspace (Tasks, Notes, Docs, Billing),
 * Calendar (Events & Dates), Todos (Global vs Matter-scoped), and Invoicing.
 */
public final class AdminCrossScreenAffordanceInspectionTest extends ReadOnlyTestBase {

    private final Path domDir = Paths.get("target", "automation-artifacts", "dom-maps");

    @Test(groups = "admin-readonly", description = "Physically inspect all cross-cutting multi-entrypoint screens and capture DOM maps.")
    @TestMetadata(
            module = "CrossScreenAffordance",
            scenarioId = "RAD-CROSS-003",
            title = "Cross-Screen Affordance & DOM Map Capture",
            testType = TestMetadata.TestType.READ_ONLY,
            expectedResult = "DOM snapshots saved in dom-maps/ directory and controls logged for multi-entrypoint propagation.",
            preconditions = "Valid administrative session in read-only exploration mode.",
            testSteps = {
                    "Inspect Court Updates To-dos & Events modal affordances",
                    "Inspect Matter Workspace Task/Note/Doc/Billing tabs",
                    "Inspect Calendar navigation and views",
                    "Inspect Todos screen and filters",
                    "Inspect Time Entry and Expense modal affordances",
                    "Inspect Invoice Drafts and sensitive billing access"
            },
            defectSeverity = TestMetadata.DefectSeverity.LOW
    )
    public void inspectCrossScreenAffordances() throws IOException {
        Files.createDirectories(domDir);
        Page page = currentPage();
        Sidebar sidebar = new Sidebar(page);

        logger.info("=== AFFORDANCE 1: COURT UPDATES TO-DOS & EVENTS ===");
        sidebar.goToCourtUpdates();
        page.waitForTimeout(1000);
        saveDom(page, "01_court_updates_main.html");

        // Inspect Add To-Do modal in Court Updates
        Locator addTodoBtn = page.locator("button:has-text('Add To-Do'), button:has-text('Add Task'), #addCourtTodoBtn, [data-action='add-todo']");
        if (addTodoBtn.count() > 0 && addTodoBtn.first().isVisible()) {
            logger.info("Clicking Court Updates Add To-Do button");
            addTodoBtn.first().click();
            page.waitForTimeout(600);
            saveDom(page, "02_court_updates_todo_modal.html");
            logModalFields(page, "Court Updates Add To-Do Modal");
            closeModal(page);
        }

        // Inspect Add Event modal in Court Updates
        Locator addEventBtn = page.locator("button:has-text('Add Event'), button:has-text('Extra Event'), #addCourtEventBtn, [data-action='add-event']");
        if (addEventBtn.count() > 0 && addEventBtn.first().isVisible()) {
            logger.info("Clicking Court Updates Add Event button");
            addEventBtn.first().click();
            page.waitForTimeout(600);
            saveDom(page, "03_court_updates_event_modal.html");
            logModalFields(page, "Court Updates Add Event Modal");
            closeModal(page);
        }

        logger.info("=== AFFORDANCE 2: CALENDAR EVENT AND DATE INTERACTIONS ===");
        sidebar.goToCalendar();
        page.waitForTimeout(1000);
        saveDom(page, "04_calendar_main.html");

        // Click Add Event on Calendar
        Locator calAddEvent = page.locator("button:has-text('Add Event'), button:has-text('New Event'), button:has-text('+ Event'), #addEventBtn");
        if (calAddEvent.count() > 0 && calAddEvent.first().isVisible()) {
            logger.info("Clicking Calendar Add Event button");
            calAddEvent.first().click();
            page.waitForTimeout(600);
            saveDom(page, "05_calendar_add_event_modal.html");
            logModalFields(page, "Calendar Add Event Modal");
            closeModal(page);
        }

        logger.info("=== AFFORDANCE 3: TO-DOS / TASKS CENTRAL WORKSPACE ===");
        sidebar.goToTodos();
        page.waitForTimeout(1000);
        saveDom(page, "06_todos_main.html");

        Locator createTodoBtn = page.locator("button:has-text('Create Task'), button:has-text('Add Task'), button:has-text('Add To-Do'), #createTaskBtn");
        if (createTodoBtn.count() > 0 && createTodoBtn.first().isVisible()) {
            logger.info("Clicking Todos Create Task button");
            createTodoBtn.first().click();
            page.waitForTimeout(600);
            saveDom(page, "07_todos_create_modal.html");
            logModalFields(page, "Global Create Task Modal");
            closeModal(page);
        }

        logger.info("=== AFFORDANCE 4: MATTER WORKSPACE TABS & MULTI-ACTIONS ===");
        sidebar.goToClients();
        page.waitForTimeout(1000);
        Locator matterLink = page.locator("a[href*='/matters/']").first();
        if (matterLink.isVisible()) {
            String matterUrl = matterLink.getAttribute("href");
            logger.info("Navigating to Matter Workspace: {}", matterUrl);
            page.navigate(matterUrl);
            page.waitForTimeout(1000);
            saveDom(page, "08_matter_workspace_general.html");
            logVisibleActions(page, "Matter General Tab");

            // Inspect Matter Tasks / Todo tab
            Locator todoTab = page.locator("a:has-text('Todo'), a:has-text('Tasks'), a:has-text('To-dos')");
            if (todoTab.count() > 0 && todoTab.first().isVisible()) {
                todoTab.first().click();
                page.waitForTimeout(1000);
                saveDom(page, "09_matter_workspace_todos.html");
                logVisibleActions(page, "Matter Todos Tab");
            }

            // Inspect Matter Notes tab
            Locator notesTab = page.locator("a:has-text('Notes')");
            if (notesTab.count() > 0 && notesTab.first().isVisible()) {
                notesTab.first().click();
                page.waitForTimeout(1000);
                saveDom(page, "10_matter_workspace_notes.html");
                logVisibleActions(page, "Matter Notes Tab");
            }

            // Inspect Matter Documents tab
            Locator docsTab = page.locator("a:has-text('Documents')");
            if (docsTab.count() > 0 && docsTab.first().isVisible()) {
                docsTab.first().click();
                page.waitForTimeout(1000);
                saveDom(page, "11_matter_workspace_documents.html");
                logVisibleActions(page, "Matter Documents Tab");
            }
        }

        logger.info("=== AFFORDANCE 5: COURT TIME BILLING ===");
        page.navigate(config.baseUrl().replaceFirst("/login$", "/court/billing"));
        if (page.url().contains("/confirm-sensitive-access")) {
            new BillingSessionPage(page).start(config.password());
        }
        page.waitForTimeout(1000);
        saveDom(page, "12_court_time_billing.html");
        logVisibleActions(page, "Court Time Billing");

        logger.info("=== AFFORDANCE 6: TIME ENTRIES & EXPENSES ===");
        page.navigate(config.baseUrl().replaceFirst("/login$", "/time"));
        if (page.url().contains("/confirm-sensitive-access")) {
            new BillingSessionPage(page).start(config.password());
        }
        page.waitForTimeout(1000);
        saveDom(page, "13_time_entries.html");
        logVisibleActions(page, "Time Entries");

        page.navigate(config.baseUrl().replaceFirst("/login$", "/expenses"));
        page.waitForTimeout(1000);
        saveDom(page, "14_expenses.html");
        logVisibleActions(page, "Expenses");

        logger.info("=== AFFORDANCE 7: INVOICE DRAFTS & RECONCILIATION ===");
        page.navigate(config.baseUrl().replaceFirst("/login$", "/invoice/draft"));
        if (page.url().contains("/confirm-sensitive-access")) {
            new BillingSessionPage(page).start(config.password());
        }
        page.waitForTimeout(1000);
        saveDom(page, "15_invoice_drafts.html");
        logVisibleActions(page, "Invoice Drafts");

        logger.info("=== AFFORDANCE INSPECTION COMPLETE ===");
    }

    private void saveDom(Page page, String filename) throws IOException {
        Path target = domDir.resolve(filename);
        Files.writeString(target, page.content());
        logger.info("Saved DOM snapshot: {}", target.toAbsolutePath());
    }

    private void logModalFields(Page page, String modalName) {
        Locator inputs = page.locator(".modal.show input, .modal.show select, .modal.show textarea, [role='dialog'] input, [role='dialog'] select, [role='dialog'] textarea");
        for (int i = 0; i < inputs.count(); i++) {
            Locator inp = inputs.nth(i);
            String name = inp.getAttribute("name");
            String id = inp.getAttribute("id");
            String type = inp.getAttribute("type");
            String placeholder = inp.getAttribute("placeholder");
            logger.info("[{}] Field #{}: name='{}', id='{}', type='{}', placeholder='{}'", modalName, i + 1, name, id, type, placeholder);
        }
    }

    private void logVisibleActions(Page page, String screenName) {
        Locator btns = page.locator("button, a.btn, input[type='submit']");
        for (int i = 0; i < Math.min(btns.count(), 25); i++) {
            Locator btn = btns.nth(i);
            if (btn.isVisible()) {
                String text = btn.innerText().replaceAll("\\s+", " ").trim();
                String href = btn.getAttribute("href");
                logger.info("[{}] Action #{}: text='{}', href='{}'", screenName, i + 1, text, href);
            }
        }
    }

    private void closeModal(Page page) {
        Locator closeBtn = page.locator(".modal.show button:has-text('Close'), .modal.show button:has-text('Cancel'), .modal.show .btn-close, [role='dialog'] button:has-text('Close')");
        if (closeBtn.count() > 0 && closeBtn.first().isVisible()) {
            closeBtn.first().click();
            page.waitForTimeout(400);
        } else {
            page.keyboard().press("Escape");
            page.waitForTimeout(400);
        }
    }
}
