package com.raddatz.automation.data;

import java.time.Clock;

/**
 * A realistic admin-owned portfolio hierarchy for one run:
 * one Company, two Properties, and three Matters (two below the first Property).
 */
public record AdminScenarioFamily(
        AdminScenarioData companyAndFirstProperty,
        AdminScenarioData secondPropertyAndMatter,
        AdminScenarioData secondMatterAtFirstProperty) {

    public static AdminScenarioFamily create(Clock clock) {
        AdminScenarioData first = AdminScenarioData.create(clock);
        AdminScenarioData secondProperty = distinctProperty(clock, first).withCompanyFrom(first);
        AdminScenarioData secondMatter = distinctMatter(clock, first)
                .withCompanyAndPropertyFrom(first, first);
        return new AdminScenarioFamily(first, secondProperty, secondMatter);
    }

    private static AdminScenarioData distinctProperty(Clock clock, AdminScenarioData first) {
        AdminScenarioData candidate = AdminScenarioData.create(clock);
        for (int attempt = 0; candidate.propertyName().equals(first.propertyName()) && attempt < 8; attempt++) {
            candidate = AdminScenarioData.create(clock);
        }
        if (candidate.propertyName().equals(first.propertyName())) {
            throw new IllegalStateException("Unable to generate a distinct owned property name for the portfolio scenario.");
        }
        return candidate;
    }

    private static AdminScenarioData distinctMatter(Clock clock, AdminScenarioData first) {
        AdminScenarioData candidate = AdminScenarioData.create(clock);
        for (int attempt = 0; (candidate.caseNumber().equals(first.caseNumber())
                || candidate.tenantName().equals(first.tenantName())) && attempt < 8; attempt++) {
            candidate = AdminScenarioData.create(clock);
        }
        if (candidate.caseNumber().equals(first.caseNumber()) || candidate.tenantName().equals(first.tenantName())) {
            throw new IllegalStateException("Unable to generate a distinct owned matter for the portfolio scenario.");
        }
        return candidate;
    }
}
