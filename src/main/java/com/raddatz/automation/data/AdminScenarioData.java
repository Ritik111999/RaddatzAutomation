package com.raddatz.automation.data;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Realistic, non-personal values for one admin-owned scenario. The reference is
 * stored in notes/email rather than a visible "test" name. Every factory call
 * creates a distinct, internally consistent Company → Property → Matter dataset.
 */
public record AdminScenarioData(
        String reference,
        String companyName,
        String contactName,
        String contactTitle,
        String contactEmail,
        String officePhone,
        String mobilePhone,
        String companyStreetAddress,
        String companySuite,
        String companyCity,
        String companyState,
        String companyZipCode,
        String propertyName,
        String propertyType,
        String rentalOfficeAddress,
        String businessLicenseDescriptor,
        String businessLicenseNumber,
        String radDescriptor,
        String radNumber,
        String radExpirationDate,
        String totalUnits,
        String propertyHourlyRate,
        String tenantName,
        String tenantStreetAddress,
        String tenantQuadrant,
        String tenantUnitNumber,
        String tenantCity,
        String tenantZipCode,
        String tenantPhone,
        String tenantEmail,
        String caseNumber,
        String caseType,
        String caseStatus,
        String matterPlan,
        String casePriority,
        String openingDate,
        String taskName,
        String taskDescription,
        String taskDueDate,
        String eventTitle,
        String eventDate,
        String eventStartTime,
        String eventDuration,
        String eventCourtRoom,
        String eventType,
        String eventCategory,
        String eventJudgeName,
        String eventNotes,
        String ownershipNote) {

    public static AdminScenarioData create(Clock clock) {
        long nonce = Math.abs(System.nanoTime());
        String sequence = String.format(Locale.ROOT, "%06d", nonce % 1_000_000);
        String[] companyDescriptors = {
                "Alder", "Brightwater", "Crescent", "Dunham",
                "Evergreen", "Fairmont", "Granite", "Hawthorne",
                "Ironwood", "Juniper", "Kingsley", "Larkspur",
                "Morningside", "Northfield", "Oakmont", "Parkside"
        };
        String[] companyOperators = {
                "Residential Management", "Community Partners", "Property Services", "Housing Group",
                "Neighborhood Management", "Living Partners", "Residential Services", "Property Alliance",
                "Apartment Management", "Community Services", "Residential Partners", "Housing Management",
                "Property Group", "Neighborhood Partners", "Residential Alliance", "Community Management"
        };
        String[] propertyNeighborhoods = {
                "Ashford", "Briarwood", "Clarendon", "Dogwood",
                "Elmhurst", "Foxhall", "Glenwood", "Highland",
                "Ivydale", "Kensington", "Lakeview", "Meadowbrook",
                "Norwood", "Oakridge", "Pinehurst", "Rosemont"
        };
        String[] propertyStyles = {
                "Court Apartments", "Garden Residences", "Terrace Homes", "Park Apartments",
                "Square Residences", "Crossing Apartments", "Place Homes", "Row Residences",
                "Heights Apartments", "Village Residences", "Commons Apartments", "Landing Homes",
                "Grove Residences", "Manor Apartments", "Station Homes", "Walk Residences"
        };
        String[] contactGivenNames = {"Nina", "Marcus", "Danielle", "Avery", "Rachel", "Darius", "Monica", "Elliot"};
        String[] contactFamilyNames = {"Caldwell", "Ellison", "Porter", "Monroe", "Graves", "Holland", "Sullivan", "Whitaker"};
        String[] tenantGivenNames = {"Olivia", "Ethan", "Maya", "Jordan", "Naomi", "Caleb", "Sofia", "Adrian"};
        String[] tenantMiddleNames = {"Claire", "James", "Renee", "Michael", "Elise", "Grant", "Louise", "Nathan"};
        String[] tenantFamilyNames = {"Bennett", "Wallace", "Franklin", "Avery", "Hughes", "Mercer", "Dalton", "Sinclair"};
        int buildingNumber = 100 + (int) (nonce % 800);
        int suiteNumber = 100 + (int) ((nonce / 10) % 800);
        int unitNumber = 100 + (int) ((nonce / 100) % 800);
        String streetAddress = buildingNumber + " K Street NW";
        String reference = LocalDate.now(clock).format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-"
                + sequence;
        String companyName = companyDescriptors[(int) (nonce % companyDescriptors.length)]
                + " "
                + companyOperators[(int) ((nonce / companyDescriptors.length) % companyOperators.length)]
                + ", LLC";
        String propertyName = propertyNeighborhoods[(int) ((nonce / 100) % propertyNeighborhoods.length)]
                + " "
                + propertyStyles[(int) ((nonce / (100 * propertyNeighborhoods.length)) % propertyStyles.length)];
        String contactName = contactGivenNames[(int) ((nonce / 10) % contactGivenNames.length)]
                + " "
                + contactFamilyNames[(int) ((nonce / (10 * contactGivenNames.length)) % contactFamilyNames.length)];
        String tenantName = tenantGivenNames[(int) (nonce % tenantGivenNames.length)]
                + " "
                + tenantMiddleNames[(int) ((nonce / tenantGivenNames.length) % tenantMiddleNames.length)]
                + " "
                + tenantFamilyNames[(int) ((nonce / (tenantGivenNames.length * tenantMiddleNames.length))
                % tenantFamilyNames.length)];

        return new AdminScenarioData(
                reference,
                companyName,
                contactName,
                "Regional Property Manager",
                contactName.toLowerCase(Locale.ROOT).replace(" ", ".")
                        + "." + reference + "@knoxweb.us",
                "202-555-" + String.format(Locale.ROOT, "%04d", 1000 + (nonce % 8999)),
                "202-555-" + String.format(Locale.ROOT, "%04d", 1000 + ((nonce / 10) % 8999)),
                streetAddress,
                "Suite " + suiteNumber,
                "Washington",
                "DC",
                "20001",
                propertyName,
                "Multifamily Residential",
                streetAddress + ", Washington, DC 20001",
                "District of Columbia Basic Business License",
                "DC-BBL-2026-" + sequence,
                "Rental Accommodations Division Registration",
                "RAD-DC-2026-" + sequence,
                "2027-12-31",
                "48",
                "225.00",
                tenantName,
                streetAddress,
                "NW",
                Integer.toString(unitNumber),
                "Washington, DC",
                "20001",
                "202-555-" + String.format(Locale.ROOT, "%04d", 1000 + ((nonce / 100) % 8999)),
                tenantName.toLowerCase(Locale.ROOT).replace(" ", ".")
                        + "." + reference + "@knoxweb.us",
                "DC-LT-2026-" + sequence,
                "Eviction",
                "active",
                "Standard Plan",
                "Medium",
                LocalDate.now(clock).toString(),
                "Lease Compliance Review - " + sequence,
                "Review the lease-compliance file and prepare the matter status update for portfolio " + sequence + ".",
                LocalDate.now(clock).plusDays(7).toString(),
                "Initial Return Hearing - " + sequence,
                LocalDate.now(clock).plusDays(14).toString(),
                "09:30",
                "01:00",
                "Superior Court - Downtown",
                "Initial Return",
                "Court Dates",
                "Hon. Elena Whitmore",
                "Bring the lease ledger, notice documentation, and service affidavit for matter " + sequence + ".",
                "Scenario reference " + reference + "; created for the local admin automation suite.");
    }

    /**
     * A realistic case-management note whose reference makes the created row
     * positively identifiable as belonging to this exact scenario run.
     */
    public String initialMatterNote() {
        return "Reviewed the rent ledger and service affidavit for the scheduled case conference. "
                + "Scenario reference " + reference + ".";
    }

    /** A substantive update to the scenario-owned note, used to prove edit persistence. */
    public String revisedMatterNote() {
        return "Reviewed the rent ledger, service affidavit, and hearing notice for the scheduled case conference. "
                + "Scenario reference " + reference + ".";
    }

    /** A unique, field-appropriate filename for this scenario's uploaded case memorandum. */
    public String ownedDocumentFileName() {
        return "case-review-memorandum-" + reference + ".docx";
    }

    public String initialTimeEntryDescription() {
        return "Reviewed the lease ledger and prepared the hearing-status summary. Scenario reference " + reference + ".";
    }

    public String revisedTimeEntryDescription() {
        return "Reviewed the lease ledger, hearing notice, and service affidavit for the case conference. Scenario reference "
                + reference + ".";
    }
}
