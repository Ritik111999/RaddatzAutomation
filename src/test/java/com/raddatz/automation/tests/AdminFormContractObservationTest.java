package com.raddatz.automation.tests;

import com.raddatz.automation.pages.Sidebar;
import com.raddatz.automation.pages.ObservedFieldConstraint;
import com.raddatz.automation.pages.SearchFilterPage;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertFalse;

/**
 * Non-mutating inventory of visible hierarchy-form controls. It opens forms but
 * never fills a field, chooses a parent, uploads, or submits.
 */
public final class AdminFormContractObservationTest extends ReadOnlyTestBase {
    @Test(groups = {"admin-readonly", "field-contract"}, description = "Observe Company, Property, and Matter field contracts without entering or saving data.")
    public void hierarchyFormFieldContractsAreObservedWithoutMutation() {
        new Sidebar(currentPage()).goToClients().openAddCompany().assertObservedFieldContract();
        new Sidebar(currentPage()).goToClients().openAddProperty().assertObservedFieldContract();
        new Sidebar(currentPage()).goToClients().openAddMatter().assertObservedFieldContract();
    }

    @Test(groups = {"admin-readonly", "field-contract"}, description = "Capture native hierarchy-field constraints without form submission.")
    public void hierarchyValidationConstraintsAreObservedWithoutSubmission() {
        Map<String, ObservedFieldConstraint> company = new Sidebar(currentPage()).goToClients().openAddCompany()
                .observeValidationConstraints();
        Map<String, ObservedFieldConstraint> property = new Sidebar(currentPage()).goToClients().openAddProperty()
                .observeValidationConstraints();
        Map<String, ObservedFieldConstraint> matter = new Sidebar(currentPage()).goToClients().openAddMatter()
                .observeValidationConstraints();

        assertFalse(company.isEmpty(), "Company contract should expose visible controls");
        assertFalse(property.isEmpty(), "Property contract should expose visible controls");
        assertFalse(matter.isEmpty(), "Matter contract should expose visible controls");
        logConstraints("Company", company);
        logConstraints("Property", property);
        logConstraints("Matter", matter);
    }

    @Test(groups = {"admin-readonly", "field-contract"}, description = "Capture task, event, and template native constraints without submission.")
    public void operationalValidationConstraintsAreObservedWithoutSubmission() {
        Map<String, ObservedFieldConstraint> task = dashboard().openAddTask().observeValidationConstraints();
        currentPage().getByRole(com.microsoft.playwright.options.AriaRole.DIALOG)
                .getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Close").setExact(true))
                .click();
        Map<String, ObservedFieldConstraint> event = dashboard().openAddEvent().observeValidationConstraints();
        Map<String, ObservedFieldConstraint> template = new SearchFilterPage(currentPage())
                .openCreateDocumentTemplate().observeValidationConstraints();

        assertFalse(task.isEmpty(), "Task contract should expose visible controls");
        assertFalse(event.isEmpty(), "Event contract should expose visible controls");
        assertFalse(template.isEmpty(), "Template contract should expose visible controls");
        logConstraints("Task", task);
        logConstraints("Event", event);
        logConstraints("Template", template);
    }

    @Test(groups = {"admin-readonly", "field-contract"}, description = "Capture blank Court Update validation attributes without selecting an Event or submitting.")
    public void courtUpdateValidationConstraintsAreObservedWithoutSubmission() {
        Map<String, ObservedFieldConstraint> courtUpdate = new Sidebar(currentPage()).goToCourtUpdates()
                .observeValidationConstraints();
        assertFalse(courtUpdate.isEmpty(), "Court Update contract should expose visible controls");
        logConstraints("CourtUpdate", courtUpdate);
    }

    private void logConstraints(String form, Map<String, ObservedFieldConstraint> constraints) {
        constraints.forEach((name, observed) -> logger.info(
                "Observed {}.{} type={} required={} min={} max={} maxlength={} pattern={} ariaRequired={}",
                form, name, observed.type(), observed.required(), observed.min(), observed.max(),
                observed.maxLength(), observed.pattern(), observed.ariaRequired()));
    }
}
