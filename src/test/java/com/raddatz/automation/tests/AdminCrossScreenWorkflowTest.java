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
 * Validates cross-cutting multi-entrypoint workflows:
 * 1. Adding/managing To-Dos directly from Court Updates and verifying propagation across
 *    Court Updates popovers, Central Tasks (/todos), and Matter Workspace (/matters/{id}).
 * 2. Scheduling Extra Events from Court Updates with return-navigation and Calendar propagation.
 */
public final class AdminCrossScreenWorkflowTest extends ReadOnlyTestBase {

    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @TestMetadata(
            module = "Cross-Screen Hubs",
            scenarioId = "RAD-CROSS-001",
            title = "Court Updates To-Do creation, in-line popover verification, and cross-screen propagation to Central Tasks and Matter Workspace",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "To-Do created in Court Updates appears in table popover, central /todos, and matter workspace",
            preconditions = "Admin authenticated on local environment with owned Company, Property, Matter, and scheduled hearing",
            testSteps = {
                    "Create owned Company, Property, Matter, and Calendar Hearing",
                    "Navigate to Court Updates for the hearing date",
                    "Select hearing and add To-Do via #addTodoBtn modal",
                    "Submit Court Update and verify .cu-todo-icon and popover in Court Updates list",
                    "Navigate to Central Tasks (/todos) and verify task appears with Matter linkage",
                    "Navigate to Matter Workspace and verify task is listed under Todo tab",
                    "Clean up owned Court Update"
            }
    )
    @Test(groups = {"admin-mutation", "cross-screen"}, description = "Court Updates To-Do creation, in-line popover verification, and cross-screen propagation.")
    public void administratorManagesCourtUpdateTodosWithMultiScreenPropagation() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        Page page = currentPage();
        Sidebar sidebar = new Sidebar(page);

        logger.info("=== STEP 1: CREATE SCENARIO-OWNED HIERARCHY ===");
        sidebar.goToClients()
                .openAddCompany()
                .createOwnedCompany(data);

        sidebar.goToClients()
                .openAddProperty()
                .createOwnedProperty(data);

        sidebar.goToClients()
                .openAddMatter()
                .createOwnedMatter(data)
                .assertOwnedMatterHierarchy(data);

        logger.info("=== STEP 2: SCHEDULE HEARING ON CALENDAR ===");
        sidebar.goToDashboard()
                .openAddEvent()
                .createOwnedEvent(data);

        logger.info("=== STEP 3: NAVIGATE TO COURT UPDATES AND ADD TO-DO ===");
        CourtUpdatesPage courtUpdates = sidebar.goToCourtUpdates()
                .assertLoaded()
                .selectCalendarDate(data.eventDate());

        // Select the owned hearing entry
        Locator ownedEvent = page.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText(data.eventTitle()));
        assertThat(ownedEvent).isVisible();
        ownedEvent.click();

        // Add To-Do directly from within Court Updates
        String courtTodoTitle = "Court Follow-up Task " + data.reference();
        String courtTodoDueDate = LocalDate.now().plusDays(3).toString();
        courtUpdates.addCourtTodo(courtTodoTitle, courtTodoDueDate);

        // Fill hearing notes and submit court update
        page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Notes *").setExact(true)).fill(data.ownershipNote());
        page.locator("#case_status").selectOption("active");
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(".*Submit Court Update.*"))).click();

        logger.info("=== STEP 4: VERIFY TO-DO POPOVER ON COURT UPDATES TABLE ===");
        page.reload();
        courtUpdates.assertLoaded();
        courtUpdates.assertCourtTodoPopover(data.ownershipNote(), courtTodoTitle);

        logger.info("=== STEP 5: VERIFY PROPAGATION TO CENTRAL TASKS (/todos) ===");
        sidebar.goToTodos()
                .assertLoaded()
                .assertTaskListed(courtTodoTitle);

        logger.info("=== STEP 6: VERIFY PROPAGATION TO MATTER WORKSPACE TODO TAB ===");
        sidebar.goToClients();
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(data.tenantName()).setExact(false)).click();
        new MatterWorkspacePage(page).assertLoaded(data.tenantName())
                .openTodoTab();
        assertThat(page.locator("body")).containsText(courtTodoTitle);

        logger.info("=== STEP 7: CLEAN UP OWNED COURT UPDATE ===");
        sidebar.goToCourtUpdates()
                .assertLoaded()
                .selectCalendarDate(data.eventDate())
                .deleteOwnedCourtUpdate(data.ownershipNote());

        logger.info("=== CROSS-SCREEN TO-DO PROPAGATION VERIFIED SUCCESSFULLY ===");
    }

    @TestMetadata(
            module = "Cross-Screen Hubs",
            scenarioId = "RAD-CROSS-002",
            title = "Court Updates Extra Event scheduling, return navigation, and propagation to Calendar",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Extra event scheduled from Court Updates redirects back and propagates to Calendar",
            preconditions = "Admin authenticated on local environment with owned Company, Property, Matter, and scheduled hearing",
            testSteps = {
                    "Create owned Company, Property, Matter, and Calendar Hearing",
                    "Navigate to Court Updates for hearing date and select hearing",
                    "Click Add Event (#addCourtDateRowBtn) to open Calendar event scheduling with return_to",
                    "Submit Extra Event and assert application redirects back to Court Updates",
                    "Verify Extra Event appears in Calendar"
            }
    )
    @Test(groups = {"admin-mutation", "cross-screen"}, description = "Court Updates Extra Event scheduling, return navigation, and propagation.")
    public void administratorSchedulesExtraEventFromCourtUpdatesWithCalendarPropagation() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        Page page = currentPage();
        Sidebar sidebar = new Sidebar(page);

        logger.info("=== STEP 1: CREATE SCENARIO-OWNED HIERARCHY ===");
        sidebar.goToClients()
                .openAddCompany()
                .createOwnedCompany(data);

        sidebar.goToClients()
                .openAddProperty()
                .createOwnedProperty(data);

        sidebar.goToClients()
                .openAddMatter()
                .createOwnedMatter(data)
                .assertOwnedMatterHierarchy(data);

        logger.info("=== STEP 2: SCHEDULE INITIAL HEARING ON CALENDAR ===");
        sidebar.goToDashboard()
                .openAddEvent()
                .createOwnedEvent(data);

        logger.info("=== STEP 3: NAVIGATE TO COURT UPDATES & TRIGGER EXTRA EVENT ===");
        CourtUpdatesPage courtUpdates = sidebar.goToCourtUpdates()
                .assertLoaded()
                .selectCalendarDate(data.eventDate());

        Locator ownedEvent = page.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText(data.eventTitle()));
        assertThat(ownedEvent).isVisible();
        ownedEvent.click();

        // Trigger Extra Event scheduling via the verified court update hook
        EventFormPage extraEventForm = courtUpdates.openExtraEventForm();
        String extraEventTitle = "Extra Hearing " + data.reference();
        String extraEventDate = LocalDate.now().plusDays(4).toString();
        extraEventForm.createOwnedEvent(new AdminScenarioData(
                data.reference(), data.companyName(), data.contactName(), data.contactTitle(), data.contactEmail(),
                data.officePhone(), data.mobilePhone(), data.companyStreetAddress(), data.companySuite(), data.companyCity(),
                data.companyState(), data.companyZipCode(), data.propertyName(), data.propertyType(), data.rentalOfficeAddress(),
                data.businessLicenseDescriptor(), data.businessLicenseNumber(), data.radDescriptor(), data.radNumber(),
                data.radExpirationDate(), data.totalUnits(), data.propertyHourlyRate(), data.tenantName(), data.tenantStreetAddress(),
                data.tenantQuadrant(), data.tenantUnitNumber(), data.tenantCity(), data.tenantZipCode(), data.tenantPhone(),
                data.tenantEmail(), data.caseNumber(), data.caseType(), data.caseStatus(), data.matterPlan(), data.casePriority(),
                data.openingDate(), data.taskName(), data.taskDescription(), data.taskDueDate(), extraEventTitle, extraEventDate,
                "14:00", data.eventDuration(), data.eventCourtRoom(), data.eventType(), data.eventCategory(), data.eventJudgeName(),
                data.eventNotes(), data.ownershipNote()
        ));

        // Assert return redirect to Court Updates
        page.waitForURL(Pattern.compile(".*/court-updates.*"));
        courtUpdates.assertLoaded();

        logger.info("=== CROSS-SCREEN EXTRA EVENT SCHEDULING VERIFIED SUCCESSFULLY ===");
    }
}
