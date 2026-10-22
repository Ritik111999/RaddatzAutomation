package com.raddatz.automation.data;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Realistic, non-personal values for one admin-owned scenario. The reference is
 * deliberately stored in notes/email rather than a visible "test" name.
 */
public record AdminScenarioData(
        String reference,
        String companyName,
        String contactName,
        String contactTitle,
        String contactEmail,
        String propertyName,
        String propertyType,
        String rentalOfficeAddress,
        String businessLicenseNumber,
        String radNumber,
        String tenantName,
        String taskName,
        String eventTitle,
        String ownershipNote) {

    public static AdminScenarioData create(Clock clock) {
        String reference = LocalDate.now(clock).format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-"
                + (System.nanoTime() & 0xFFFFF);

        return new AdminScenarioData(
                reference,
                "Mariner Point Residential Management, LLC",
                "Nina Caldwell",
                "Regional Property Manager",
                "nina.caldwell." + reference + "@knoxweb.us",
                "Cedar Grove Residences",
                "Multifamily Residential",
                "425 Market Street, Washington, DC 20001",
                "BL-2026-425",
                "RAD-2026-425",
                "Olivia Bennett",
                "Lease Compliance Review",
                "Initial Return Hearing",
                "Scenario reference " + reference + "; created for the local admin automation suite.");
    }
}
