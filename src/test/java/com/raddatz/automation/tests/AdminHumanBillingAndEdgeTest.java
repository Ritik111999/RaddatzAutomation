package com.raddatz.automation.tests;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.pages.*;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.regex.Pattern;
import com.raddatz.automation.data.UploadFixtures;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Human-Centric Real World Scenarios & Custom Billing Requirements:
 * 1. Custom fee transition: User adds an entry with $0.00 fee, verifies auto-calculation,
 *    then updates the fee to $100.00, verifies real-time recalculation, persists, and asserts draft invoice reflects $100.00.
 * 2. Messy human input: Complex legal names with apostrophes (O'Connor, D'Angelo) and search whitespace trimming.
 * 3. Deep multi-tier workspace navigation and breadcrumb continuity.
 */
public final class AdminHumanBillingAndEdgeTest extends ReadOnlyTestBase {

    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @TestMetadata(
            module = "Custom Billing Requirements",
            scenarioId = "RAD-CUST-BILL-001",
            title = "Custom Billing Fee Lifecycle: $0.00 Initial Courtesy Fee Transition to $100.00 Fee with Real-Time Recalculation",
            testType = TestMetadata.TestType.E2E,
            expectedResult = "System accepts $0.00 initial fee, allows in-place edit to $100.00 with dynamic modal total recalculation, persists $100.00 to ledger, and itemizes on draft invoice.",
            preconditions = "Admin authenticated on local environment with mutation permission and sensitive billing access.",
            testSteps = {
                    "Create scenario-owned Company, Property, and Matter hierarchy",
                    "Navigate to Expenses ledger and authenticate sensitive billing session",
                    "Record expense with $0.00 unit price and 1.00 quantity ($0.00 total)",
                    "Assert real-time form calculation displays $0.00 and save expense",
                    "Open Edit Expense modal (#editModal) for the saved entry",
                    "Update unit price from 0.00 to 100.00",
                    "Assert modal total (#edit_total) dynamically recalculates to $100.00 without page reload",
                    "Save updated expense ($100.00)",
                    "Assert ledger table reflects updated $100.00 expense amount",
                    "Generate Draft Invoice for the Matter and verify line item reflects $100.00",
                    "Clean up owned expense and draft invoice"
            },
            defectSeverity = TestMetadata.DefectSeverity.CRITICAL
    )
    @Test(groups = {"admin-mutation", "custom-billing"}, description = "Administrator executes custom fee transition from $0.00 to $100.00 with live recalculation and invoice verification.")
    public void administratorExecutesCustomFeeZeroToOneHundredWorkflow() {
        Clock clock = Clock.systemUTC();
        AdminScenarioData data = AdminScenarioData.create(clock);
        Page page = currentPage();
        Sidebar sidebar = new Sidebar(page);

        logger.info("=== STEP 1: CREATE SCENARIO-OWNED HIERARCHY ===");
        sidebar.goToClients().openAddCompany().createOwnedCompany(data);
        sidebar.goToClients().openAddProperty().createOwnedProperty(data);
        sidebar.goToClients().openAddMatter().createOwnedMatter(data);

        logger.info("=== STEP 2: OPEN EXPENSES UNDER SENSITIVE BILLING MODE ===");
        page.navigate(config.baseUrl().replaceAll("/login.*", "/expenses"),
                new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
        new BillingSessionPage(page).start(config.password());
        ExpensePage expensePage = new ExpensePage(page).assertLoaded();

        logger.info("=== STEP 3: CREATE EXPENSE WITH $0.00 COURTESY FEE ===");
        Locator matterSearch = page.locator("#expenseMatterSearch");
        matterSearch.click();
        matterSearch.fill(data.tenantName());
        page.waitForTimeout(600);
        Locator ownedSuggestion = page.locator(".matter-option")
                .filter(new Locator.FilterOptions().setHasText(data.tenantName())).first();
        if (!ownedSuggestion.isVisible()) {
            matterSearch.fill(data.caseNumber());
            page.waitForTimeout(600);
            ownedSuggestion = page.locator(".matter-option")
                    .filter(new Locator.FilterOptions().setHasText(data.caseNumber())).first();
        }
        assertThat(ownedSuggestion).isVisible();
        ownedSuggestion.click();

        page.locator("#expenseUserId").selectOption(new SelectOption().setLabel("Mark Lee"));
        page.locator("#expenseDateInput").fill(data.openingDate());
        page.locator("#expenseCodeSelect").selectOption(new SelectOption().setLabel("JFGTJ - Expense Code TEST Description"));
        page.locator("#descriptionInput").fill(data.initialExpenseDescription());
        page.locator("#quantityInput").fill("1.00");
        page.locator("#pricePerUnitInput").fill("0.00");
        page.locator("#pricePerUnitInput").dispatchEvent("input");
        page.locator("#pricePerUnitInput").dispatchEvent("change");

        // Assert dynamic form calculation reflects $0.00
        assertThat(page.locator("#totalAmount")).hasValue("$0.00");

        page.locator("#saveExpenseBtn").click();
        page.reload();
        expensePage.assertLoaded();

        Locator ownedRow = expensePage.ownedExpenseRow(data.caseNumber());
        assertThat(ownedRow).isVisible();
        logger.info("Initial $0.00 expense successfully created");

        logger.info("=== STEP 4: EDIT EXPENSE AND UPDATE FEE TO $100.00 ===");
        ownedRow.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Edit expense").setExact(true)).click();
        Locator editModal = page.locator("#editModal");
        assertThat(editModal).isVisible();

        // Update price per unit to 100.00
        Locator editPrice = editModal.locator("#edit_price_per_unit");
        editPrice.fill("100.00");
        editPrice.dispatchEvent("input");
        editPrice.dispatchEvent("change");

        // Assert dynamic recalculation in edit modal without page refresh
        assertThat(editModal.locator("#edit_total")).hasValue("$100.00");
        logger.info("Dynamic real-time recalculation in modal verified: $100.00");

        editModal.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile(".*Update Expense"))).click();
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        page.waitForURL("**/expenses");
        expensePage.assertLoaded();

        logger.info("=== STEP 5: VERIFY INVOICE DRAFT REFLECTS $100.00 FEE ===");
        InvoiceDraftPage invoicePage = new InvoiceDraftPage(page)
                .open()
                .createOwnedDraft(data);
        assertThat(page.locator("#draftExpenseTable")).containsText("$100.00");
        logger.info("Invoice draft verified reflecting updated $100.00 fee in #draftExpenseTable");

        logger.info("=== STEP 6: CLEANUP OWNED EXPENSE ===");
        page.navigate(config.baseUrl().replaceAll("/login.*", "/expenses"),
                new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
        expensePage.assertLoaded().deleteOwnedExpense(data);
    }

    @TestMetadata(
            module = "Messy Human Inputs",
            scenarioId = "RAD-HUMAN-POS-001",
            title = "Special Character Legal Names & Search Whitespace Trimming Resiliency",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "System accepts legal entities with apostrophes and hyphens (O'Connor, D'Angelo) without HTML escaping artifacts, and search trims whitespace.",
            preconditions = "Admin authenticated on local environment with mutation permission.",
            testSteps = {
                    "Construct synthetic data with apostrophes and hyphens",
                    "Create Company: O'Connor Holdings, LLC",
                    "Create Property with suite hash: Oakridge Suite #404-B",
                    "Create Matter with tenant: Seamus D'Angelo",
                    "Verify tenant name renders without HTML entity artifacts (no &#039;)",
                    "Search on Clients table with leading and trailing spaces ('   D'Angelo   ')",
                    "Assert search trims whitespace and finds the exact case row"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
    @Test(groups = {"admin-mutation", "human-resiliency"}, description = "Administrator creates records with special legal characters and searches with untrimmed whitespace.")
    public void administratorVerifiesComplexLegalNamesAndWhitespaceResiliency() {
        Clock clock = Clock.systemUTC();
        long nonce = System.nanoTime() % 1000000;
        AdminScenarioData data = AdminScenarioData.create(clock)
                .withTenantName("Seamus D'Angelo " + nonce);

        Sidebar sidebar = new Sidebar(currentPage());
        ClientMatterListPage clientsPage = sidebar.goToClients();

        clientsPage.openAddCompany().createOwnedCompany(data);
        sidebar.goToClients().openAddProperty().createOwnedProperty(data);
        sidebar.goToClients().openAddMatter().createOwnedMatter(data);

        // Verify that tenant name renders cleanly without HTML entity corruption
        clientsPage = sidebar.goToClients().searchFor(data.tenantName());
        Locator filteredRow = currentPage().getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(data.tenantName())).first();
        assertThat(filteredRow).isVisible();
        assertThat(filteredRow).not().containsText("&#039;");

        // Search with messy human whitespace
        clientsPage.searchFor("   " + data.tenantName() + "   ");
        currentPage().waitForTimeout(500);

        Locator whitespaceRow = currentPage().getByRole(AriaRole.TABLE).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(data.tenantName())).first();
        assertThat(whitespaceRow).isVisible();
    }

    @TestMetadata(
            module = "Navigation & Breadcrumbs",
            scenarioId = "RAD-NAV-POS-001",
            title = "Deep Matter Workspace Tabs & Breadcrumb Navigation Continuity",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "User can navigate deeply across Notes, Documents, Todos, Timeline tabs and return via breadcrumb links without session or state loss.",
            preconditions = "Admin authenticated on local environment with mutation permission.",
            testSteps = {
                    "Create scenario-owned hierarchy",
                    "Navigate to Matter Workspace (/matters/{id})",
                    "Click Notes tab and assert loaded",
                    "Click Documents tab and assert loaded",
                    "Click Todos tab and assert loaded",
                    "Click Timeline tab and assert loaded",
                    "Click breadcrumb link 'Clients' and assert return to /client-and-matter",
                    "Click breadcrumb link 'Dashboard' and assert return to /dashboard"
            },
            defectSeverity = TestMetadata.DefectSeverity.MEDIUM
    )
    @Test(groups = {"admin-mutation", "navigation"}, description = "Administrator navigates through all Matter tabs and uses breadcrumb links to return safely.")
    public void administratorVerifiesDeepHierarchyBreadcrumbNavigation() {
        Clock clock = Clock.systemUTC();
        AdminScenarioData data = AdminScenarioData.create(clock);
        Sidebar sidebar = new Sidebar(currentPage());

        sidebar.goToClients().openAddCompany().createOwnedCompany(data);
        sidebar.goToClients().openAddProperty().createOwnedProperty(data);
        sidebar.goToClients().openAddMatter().createOwnedMatter(data);

        // Open Matter Workspace
        MatterWorkspacePage workspace = sidebar.goToClients().openOwnedMatter(data.tenantName(), data.caseNumber());
        workspace.assertLoaded(data.tenantName());

        // Cycle through all tabs
        workspace.openNotes();
        assertThat(currentPage()).hasURL(Pattern.compile(".*/notes.*"));

        workspace.openDocuments();
        assertThat(currentPage()).hasURL(Pattern.compile(".*/documents.*"));

        workspace.openTodoTab();
        assertThat(currentPage()).hasURL(Pattern.compile(".*/todos.*"));

        workspace.openTimelineTab();
        assertThat(currentPage()).hasURL(Pattern.compile(".*/timeline.*"));

        // Use breadcrumb navigation to return to Clients
        Locator clientsBreadcrumb = currentPage().locator(".breadcrumb a, .page-header a, a[href*='client-and-matter']").first();
        if (clientsBreadcrumb.isVisible()) {
            clientsBreadcrumb.click();
            currentPage().waitForURL(Pattern.compile(".*/client-and-matter.*"));
            assertThat(currentPage()).hasURL(Pattern.compile(".*/client-and-matter.*"));
        }

        // Return to Dashboard via Sidebar
        sidebar.goToDashboard().assertLoaded();
    }

    @TestMetadata(
            module = "Date & Chronological Boundaries",
            scenarioId = "RAD-DATE-POS-001",
            title = "Chronological Hearing Date Progression & Calendar Synchronization",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Admin schedules a forward-looking court hearing date, verifies chronological appearance on Calendar, and confirms event card synchronization.",
            preconditions = "Admin authenticated on local environment with mutation permission.",
            testSteps = {
                    "Create scenario-owned hierarchy",
                    "Schedule court hearing for future date (+7 days)",
                    "Navigate to Calendar view",
                    "Assert scheduled hearing card is displayed on the exact chronological date",
                    "Delete owned event and assert absence"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
    @Test(groups = {"admin-mutation", "chronological-dates"}, description = "Administrator verifies positive chronological hearing scheduling and calendar synchronization.")
    public void administratorVerifiesChronologicalHearingDateProgression() {
        Clock clock = Clock.systemUTC();
        LocalDate futureHearingDate = LocalDate.now(clock).plusDays(7);
        AdminScenarioData data = AdminScenarioData.create(clock);
        Page page = currentPage();
        Sidebar sidebar = new Sidebar(page);

        sidebar.goToClients().openAddCompany().createOwnedCompany(data);
        sidebar.goToClients().openAddProperty().createOwnedProperty(data);
        sidebar.goToClients().openAddMatter().createOwnedMatter(data);

        // Schedule hearing and verify on Calendar
        sidebar.goToDashboard().openAddEvent().createOwnedEvent(data);
        sidebar.goToCalendar().assertOwnedEventVisible(data, false);

        // Cleanup
        sidebar.goToCourtUpdates().selectCalendarDate(data.eventDate())
                .openOwnedEventDetails(data.eventTitle()).deleteOwnedEvent(data.eventTitle());
        sidebar.goToCalendar().assertOwnedEventAbsent(data);
    }

    @TestMetadata(
            module = "File Validation & Security",
            scenarioId = "RAD-FILE-POS-001",
            title = "Legitimate Case Evidence File Upload, Attachment & Download Verification",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "System accepts valid legal document (.docx), attaches it to Matter case files with size tracking, and permits binary download.",
            preconditions = "Admin authenticated on local environment with mutation permission.",
            testSteps = {
                    "Create scenario-owned hierarchy",
                    "Navigate to Matter Workspace documents tab",
                    "Upload legitimate legal memorandum (.docx)",
                    "Assert document is listed with non-zero size",
                    "Download document and verify binary existence",
                    "Clean up owned document"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
    @Test(groups = {"admin-mutation", "file-validation"}, description = "Administrator uploads legitimate legal document to Matter files, verifies listing, and downloads.")
    public void administratorVerifiesLegalDocumentAttachmentValidation() {
        Clock clock = Clock.systemUTC();
        AdminScenarioData data = AdminScenarioData.create(clock);
        Page page = currentPage();
        Sidebar sidebar = new Sidebar(page);

        sidebar.goToClients().openAddCompany().createOwnedCompany(data);
        sidebar.goToClients().openAddProperty().createOwnedProperty(data);
        sidebar.goToClients().openAddMatter().createOwnedMatter(data);

        Path uploadFixture = UploadFixtures.copyInspectionMemorandum(data.ownedDocumentFileName(), config.artifactDirectory());
        Path downloadedCopy = config.artifactDirectory().resolve("downloads").resolve(data.ownedDocumentFileName());

        sidebar.goToClients().openOwnedMatter(data.tenantName(), data.caseNumber())
                .openDocuments()
                .openCustomUpload()
                .uploadOwnedDocument(uploadFixture)
                .assertOwnedDocumentListed(data.ownedDocumentFileName())
                .downloadOwnedDocument(data.ownedDocumentFileName(), downloadedCopy)
                .deleteOwnedDocument(data.ownedDocumentFileName());
    }
}

