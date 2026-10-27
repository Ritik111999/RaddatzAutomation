package com.raddatz.automation.reports;

import com.raddatz.automation.config.AutomationConfig;
import com.raddatz.automation.core.reporting.TestMetadata;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Compact executive QA report modeled on the approved ToolTrader layout. It
 * intentionally reports only run metadata and test outcomes: no credentials,
 * client records, legal content, or captured application data are rendered.
 */
final class RaddatzQaPdfReport {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.of("Asia/Kolkata"));

    private RaddatzQaPdfReport() {
    }

    static void write(AutomationConfig config, Path directory, Instant started, Instant finished, List<Scenario> scenarios) {
        write(config, directory, directory.getFileName().toString(), started, finished, scenarios);
    }

    static void write(AutomationConfig config, Path directory, String executionId, Instant started, Instant finished, List<Scenario> scenarios) {
        Path html = directory.resolve("raddatz-qa-report.html");
        try {
            Files.writeString(html, document(config, executionId, started, finished, scenarios), StandardCharsets.UTF_8);
            PdfReportExporter.export(html, directory.resolve("raddatz-qa-report.pdf"));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write the Raddatz QA report", exception);
        }
    }

    private static String document(AutomationConfig config, String runId, Instant started, Instant finished, List<Scenario> scenarios) {
        long passed = scenarios.stream().filter(s -> "PASS".equals(s.status())).count();
        long failed = scenarios.stream().filter(s -> "FAIL".equals(s.status())).count();
        long skipped = scenarios.stream().filter(s -> "SKIP".equals(s.status())).count();
        long total = scenarios.size();
        String rate = total == 0 ? "N/A" : String.format(java.util.Locale.ROOT, "%.2f%%", passed * 100.0 / total);
        Map<String, List<Scenario>> modules = scenarios.stream().collect(Collectors.groupingBy(
                RaddatzQaPdfReport::moduleFor, java.util.LinkedHashMap::new, Collectors.toList()));

        StringBuilder body = new StringBuilder();
        body.append("<section class='cover'><h1>Raddatz PWA</h1><h2>Automation Execution Report</h2>")
                .append("<p><b>Run ID:</b> ").append(escape(runId)).append("</p>")
                .append("<p><b>Environment:</b> Approved local non-production &nbsp; <b>Browser:</b> ")
                .append(escape(config.browser())).append(" (Playwright)</p>")
                .append("<p><b>Date:</b> ").append(escape(DISPLAY_TIME.format(finished))).append("</p></section>");
        body.append("<section class='content'><h2>Execution Summary</h2><div class='cards'>")
                .append(card("Total Scenarios", total, "neutral"))
                .append(card("Passed", passed, "pass"))
                .append(card("Failed", failed, failed == 0 ? "pass" : "fail"))
                .append(card("Skipped", skipped, "neutral"))
                .append(card("Success Rate", rate, passed == total ? "pass" : "fail"))
                .append("</div><h2>Execution Metadata</h2><table class='metadata'><tr><th>Start</th><td>")
                .append(escape(DISPLAY_TIME.format(started))).append("</td><th>End</th><td>")
                .append(escape(DISPLAY_TIME.format(finished))).append("</td></tr><tr><th>Duration</th><td>")
                .append(escape(format(Duration.between(started, finished)))).append("</td><th>Target</th><td>Approved local environment</td></tr>")
                .append("<tr><th>Scope</th><td colspan='3'>Owned synthetic data and permitted read-only observation. Restricted modules and payment/send/allocation/settlement controls are excluded.</td></tr></table>")
                .append("<h2>Module Summary</h2><table><thead><tr><th>Module</th><th>Total</th><th>Passed</th><th>Failed</th><th>Skipped</th><th>Result</th></tr></thead><tbody>");
        modules.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            long modulePassed = entry.getValue().stream().filter(s -> "PASS".equals(s.status())).count();
            long moduleFailed = entry.getValue().stream().filter(s -> "FAIL".equals(s.status())).count();
            long moduleSkipped = entry.getValue().stream().filter(s -> "SKIP".equals(s.status())).count();
            body.append("<tr><td>").append(escape(entry.getKey())).append("</td><td>").append(entry.getValue().size())
                    .append("</td><td>").append(modulePassed).append("</td><td>").append(moduleFailed).append("</td><td>")
                    .append(moduleSkipped).append("</td><td>").append(badge(moduleFailed > 0 ? "FAIL" : moduleSkipped > 0 ? "SKIP" : "PASS")).append("</td></tr>");
        });
        body.append("</tbody></table><h2>Scenario Register</h2><table><thead><tr><th>Scenario</th><th>Outcome</th><th>Duration</th><th>Scope</th></tr></thead><tbody>");
        scenarios.stream().sorted(Comparator.comparing(Scenario::name)).forEach(s -> body.append("<tr><td><b>")
                .append(escape(s.name())).append("</b><br><span class='muted'>").append(escape(s.description()))
                .append("</span></td><td>").append(badge(s.status())).append("</td><td>").append(escape(format(s.duration())))
                .append("</td><td>").append(escape(moduleFor(s))).append("</td></tr>"));
        body.append("</tbody></table><h2>Scenario Details</h2>");
        body.append("<p>These results cover the executed scenarios in this run. The success rate is an execution metric; full Raddatz functional coverage has not been established. Steps below describe the implemented workflow; individual step timestamps are not recorded.</p>");
        scenarios.stream().sorted(Comparator.comparing(Scenario::name)).forEach(s -> appendScenarioDetail(body, s));
        body.append("<h2>Coverage Gaps and Exclusions</h2><p>The suite currently combines lifecycle checks into 19 methods. A passing method confirms only its implemented assertions. No per-field, per-action, or full-application coverage percentage has been established.</p><ul>")
                .append("<li>Pending: event date/time rescheduling and verification across all consumers; Expense-to-Invoice propagation; additional status and edit paths.</li>")
                .append("<li>Pending: negative and boundary submissions, unsupported document validation, broader search combinations, and PWA offline/responsive behavior.</li>")
                .append("<li>Known limitations: existing-Matter additional-tenant persistence and unavailable inline Word editor.</li>")
                .append("<li>Observation only: workflow forms, Settings menu, and Reports tabs/filters. These do not constitute full functional tests of those modules.</li>")
                .append("<li>Excluded: integrations, backend controls, portal publishing/access, permissions, bulk/import/export, external messaging, and payment/settlement.</li></ul></section>");
        return "<!doctype html><html><head><meta charset='utf-8'><title>Raddatz PWA QA Report</title><style>"
                + "@page{size:A4;margin:16mm 14mm 16mm 14mm;}body{font-family:Arial,sans-serif;color:#14213d;font-size:10pt;margin:0;}"
                + ".cover{min-height:250mm;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;page-break-after:always;}"
                + ".cover h1{font-size:36pt;margin:0 0 18pt}.cover h2{font-size:22pt;color:#384a67;margin:0 0 32pt}.cover p{font-size:13pt;margin:8pt}.content{padding-top:2mm}"
                + "h2{font-size:17pt;border-bottom:2px solid #25408f;padding-bottom:5pt;margin:20pt 0 10pt;color:#172d68}.cards{display:grid;grid-template-columns:repeat(5,1fr);gap:7pt;margin:8pt 0 18pt}.card{border-radius:6pt;padding:9pt;text-align:center;border:1px solid #d6deef}.label{font-size:8pt;color:#4e5d78}.value{font-size:18pt;font-weight:bold;margin-top:4pt}.pass{background:#eefaf2;color:#167144}.fail{background:#fff0f0;color:#b42318}.neutral{background:#f3f6fb;color:#25408f}"
                + "table{border-collapse:collapse;width:100%;margin:8pt 0 18pt;page-break-inside:auto}th{background:#25408f;color:#fff;text-align:left}th,td{border:1px solid #d7deea;padding:6pt;vertical-align:top}tr{page-break-inside:avoid}.metadata th{background:#eaf0fb;color:#172d68;width:16%}.metadata td{width:34%}.badge{display:inline-block;border-radius:8pt;padding:2pt 6pt;font-size:8pt;font-weight:bold}.badge.pass{background:#dff7e8;color:#126b38}.badge.fail{background:#ffe3e3;color:#a21313}.badge.skip{background:#edf1f7;color:#4e5d78}.muted{color:#5b6880;font-size:8pt}.scenario{border:1px solid #d7deea;border-left:4pt solid #25408f;padding:9pt 11pt;margin:10pt 0;page-break-inside:avoid}.scenario h3{font-size:12pt;margin:0 0 7pt;color:#172d68}.scenario p{margin:5pt 0}.scenario ol{margin:4pt 0 6pt 18pt;padding:0}.scenario li{margin:2pt 0}</style></head><body>"
                + "<style>.scenario{border:0;padding:0;margin:0;page-break-before:always;break-before:page;page-break-inside:avoid}.scenario h3{border-bottom:2px solid #25408f;padding:0 0 9pt;margin:0 0 14pt;font-size:16pt}.scenario p{margin:9pt 0;line-height:1.5}.scenario li{margin:7pt 0;line-height:1.4}</style>"
                + body + "</body></html>";
    }

    private static void appendScenarioDetail(StringBuilder body, Scenario scenario) {
        Narrative narrative = narrativeFor(scenario);
        String scenarioId = idFor(scenario);
        String module = moduleFor(scenario);
        String testType = scenario.metadata() != null ? scenario.metadata().testType().label() : (scenario.name().startsWith("administrator") ? "Positive lifecycle" : "Read-only observation");

        body.append("<article class='scenario'><h3>").append(escape(scenarioId)).append(" - ").append(escape(narrative.title())).append(" ")
                .append(badge(scenario.status())).append("</h3><p><b>Module:</b> ").append(escape(module))
                .append(" &nbsp; <b>Type:</b> ").append(escape(testType))
                .append(" &nbsp; <b>Duration:</b> ").append(escape(format(scenario.duration())))
                .append("</p><p><b>Preconditions:</b> ").append(escape(narrative.preconditions())).append("</p><p><b>Test Steps:</b></p><ol>");
        for (String step : narrative.steps()) body.append("<li>").append(escape(step)).append("</li>");
        body.append("</ol><p><b>Expected Result:</b> ").append(escape(narrative.expected())).append("</p><p><b>Actual Result:</b> ")
                .append(escape("PASS".equals(scenario.status()) ? "All implemented assertions completed: " + narrative.expected() : "Scenario stopped before all expected checks completed. Later steps have not been verified."))
                .append("</p><p><b>Failure classification:</b> ").append(escape(scenario.failure()))
                .append("</p><p><b>Evidence:</b> TestNG method ").append(escape(scenario.name())).append("; underlying TestNG and Extent results retained with the source execution.</p></article>");
    }

    private static Narrative narrativeFor(Scenario scenario) {
        if (scenario.metadata() != null) {
            List<String> steps = scenario.metadata().testSteps() != null && scenario.metadata().testSteps().length > 0
                    ? List.of(scenario.metadata().testSteps())
                    : List.of("Execute scenario Playwright assertions against approved target");
            return new Narrative(scenario.metadata().title(), scenario.metadata().preconditions(), steps, scenario.metadata().expectedResult());
        }
        return narrativeFor(scenario.name(), scenario.description());
    }

    private static Narrative narrativeFor(String name, String fallback) {
        return switch (name) {
            case "administratorBuildsOwnedPortfolioHierarchy" -> n("Owned multi-property hierarchy", "Approved local target; fresh automation-owned data.", "Create one Company", "Add two Properties under that Company", "Create three Matters under the intended Properties", "Verify grid lineage and create linked Task/Event", "One Company has the expected Properties and Matters with correct row lineage.");
            case "administratorEditsOwnedHierarchy" -> n("Company, Property, and Matter update persistence", "Fresh owned hierarchy exists.", "Open each owned edit route", "Update the permitted Company, Property, and Matter fields", "Reload and reopen each record", "Verify the revised values and unchanged lineage", "Each permitted update survives server reload and remains linked to the owned hierarchy.");
            case "administratorAddsOwnedSecondTenantToOwnedMatter" -> n("Multiple tenants on one Matter", "Fresh owned Company and Property exist.", "Open Create Matter", "Populate two tenants on one Matter", "Save the Matter", "Verify both tenants in the case-scoped grid row", "The Matter retains both populated tenants.");
            case "administratorMaintainsOwnedTask" -> n("Owned Task lifecycle", "Fresh owned Matter exists.", "Create an owned Task", "Find it in To-dos", "Edit title and description", "Reload, search by revised title, and delete", "Only the owned Task is updated, persisted, and removed.");
            case "administratorMaintainsOwnedEvent" -> n("Event update and downstream propagation", "Fresh owned Matter exists.", "Create an owned Event", "Verify Calendar and Court Updates visibility", "Edit title and notes", "Verify revised Calendar card, then delete", "The revised Event propagates to permitted consumers and is absent after deletion.");
            case "administratorManagesOwnedMatterNoteLifecycle" -> n("Matter Note lifecycle", "Fresh owned Matter exists.", "Create an owned Note", "Edit its content", "Reload and verify revised content", "Delete the same note", "The owned Note persists after update and is removed only by its scoped delete.");
            case "administratorManagesOwnedMatterDocumentLifecycle" -> n("Matter Document lifecycle", "Fresh owned Matter and controlled fixture exist.", "Upload controlled document", "Verify listing and view affordance", "Download local artifact", "Delete the exact owned document", "The owned document is managed without portal, email, or publishing actions.");
            case "administratorManagesOwnedTimeEntryLifecycle" -> n("Time Entry lifecycle", "Fresh owned Matter and billing session exist.", "Create owned billable time", "Edit its description", "Reload Time Entry grid", "Delete the revised owned row", "The owned entry persists after reload and no payment controls are used.");
            case "administratorSeesUpdatedOwnedTimeEntryInDraftInvoice" -> n("Time update to invoice propagation", "Fresh owned Matter and billing session exist.", "Create and update owned time", "Reload the billing grid", "Create an owned draft invoice", "Verify revised bill note in the invoice source row", "The draft consumes the server-persisted revised Time Entry.");
            case "administratorMaintainsOwnedExpenseLifecycle" -> n("Expense lifecycle", "Fresh owned Matter and billing session exist.", "Create owned Expense", "Edit description", "Reopen after reload and verify revised value", "Delete the owned Expense", "The owned Expense persists after update and is removed safely.");
            case "administratorMaintainsOwnedCourtUpdate" -> n("Court Update lifecycle", "Fresh owned Event and Matter exist.", "Verify event on Court Updates date", "Create Court Update", "Edit notes and reload", "Delete exact owned update", "The owned Court Update persists and is removed without billing handoff.");
            case "administratorCreatesOwnedInvoiceDraftFromOwnedTime" -> n("Draft invoice from owned time", "Fresh owned Matter and billable time exist.", "Open Draft Invoices", "Create draft for owned Matter/date", "Verify source row, totals, preview, and local PDF", "Do not send or allocate payment", "Draft contains only the expected owned time source and local PDF is valid.");
            case "administratorMaintainsOwnedDocumentTemplateMetadata" -> n("Document Template metadata lifecycle", "Controlled DOCX fixture exists.", "Create owned template and custom variable", "Upload source document", "View, download, edit, and reopen metadata", "Delete exact owned template", "Owned template metadata and variable persist without generation or billing side effects.");
            case "administratorGeneratesOwnedVariableDocument" -> n("Document variable generation", "Owned template, Matter, and controlled DOCX token exist.", "Create template with custom variable", "Generate document for owned Matter", "Download output", "Inspect Word XML for substituted value", "The custom variable is substituted and raw token is absent from output.");
            case "calendarViewsAndFilterContractsAreReadOnly" -> n("Calendar read-only contracts", "Approved read-only session.", "Open Calendar", "Verify view controls and filters", "Switch permitted views", "Do not create or edit events", "Calendar navigation/filter contracts are available without data mutation.");
            case "reportsTabsAndFiltersRemainReadOnly" -> n("Reports read-only contracts", "Approved sensitive-access setup.", "Open permitted Reports shell", "Verify tabs and filters", "Do not generate, export, send, or open record actions", "Report controls are observed without changing data.");
            case "searchAndFilterContractsAreReadOnly" -> n("Search and filter contracts", "Approved read-only session.", "Run unique no-match searches", "Verify empty state", "Reset search and filters", "Do not change records", "Search and reset contracts behave without data mutation.");
            case "settingsRemainObservationOnly" -> n("Settings boundary observation", "Approved read-only session.", "Open Settings menu", "Verify destination visibility", "Do not enter restricted configuration controls", "Settings navigation is observed while restricted controls remain untouched.");
            case "workflowContractsAreObservationOnly" -> n("Workflow boundary observation", "Approved read-only session.", "Open Workflow list and blank creation form", "Verify visible trigger/action contracts", "Do not choose, toggle, or save", "Workflow contracts are observed without changing workflow behavior.");
            default -> n(name, "Approved local environment and applicable scenario prerequisites.", "Start the approved scenario", "Execute declared Playwright assertions", "Capture the terminal result", fallback);
        };
    }

    private static Narrative n(String title, String preconditions, String... details) {
        List<String> all = List.of(details);
        return new Narrative(title, preconditions, all.subList(0, all.size() - 1), all.getLast());
    }

    private static String idFor(Scenario scenario) {
        if (scenario.metadata() != null && !scenario.metadata().scenarioId().isBlank()) {
            return scenario.metadata().scenarioId();
        }
        return idFor(scenario.name());
    }

    private static String idFor(String name) {
        return switch (name) {
            case "administratorBuildsOwnedPortfolioHierarchy" -> "RAD-TC-00001";
            case "administratorEditsOwnedHierarchy" -> "RAD-TC-00002";
            case "administratorAddsOwnedSecondTenantToOwnedMatter" -> "RAD-TC-00003";
            case "administratorMaintainsOwnedTask" -> "RAD-TC-00004";
            case "searchAndFilterContractsAreReadOnly" -> "RAD-TC-00005";
            case "administratorMaintainsOwnedEvent" -> "RAD-TC-00006";
            case "administratorManagesOwnedMatterNoteLifecycle" -> "RAD-TC-00007";
            case "administratorManagesOwnedMatterDocumentLifecycle" -> "RAD-TC-00008";
            case "calendarViewsAndFilterContractsAreReadOnly" -> "RAD-TC-00009";
            case "administratorMaintainsOwnedCourtUpdate" -> "RAD-TC-00010";
            case "administratorManagesOwnedTimeEntryLifecycle" -> "RAD-TC-00011";
            case "administratorMaintainsOwnedExpenseLifecycle" -> "RAD-TC-00012";
            case "administratorCreatesOwnedInvoiceDraftFromOwnedTime" -> "RAD-TC-00013";
            case "administratorMaintainsOwnedDocumentTemplateMetadata" -> "RAD-TC-00014";
            case "administratorGeneratesOwnedVariableDocument" -> "RAD-TC-00015";
            case "reportsTabsAndFiltersRemainReadOnly" -> "RAD-TC-00016";
            case "workflowContractsAreObservationOnly" -> "RAD-TC-00017";
            case "settingsRemainObservationOnly" -> "RAD-TC-00018";
            case "administratorSeesUpdatedOwnedTimeEntryInDraftInvoice" -> "RAD-TC-00028";
            default -> "Unregistered scenario";
        };
    }

    private static String card(String label, Object value, String tone) {
        return "<div class='card " + tone + "'><div class='label'>" + escape(label) + "</div><div class='value'>" + escape(String.valueOf(value)) + "</div></div>";
    }

    private static String badge(String status) {
        return "<span class='badge " + status.toLowerCase(java.util.Locale.ROOT) + "'>" + escape(status) + "</span>";
    }

    private static String moduleFor(Scenario scenario) {
        if (scenario.metadata() != null && !scenario.metadata().module().isBlank()) {
            return scenario.metadata().module();
        }
        String name = scenario.name().toLowerCase(java.util.Locale.ROOT);
        if (name.contains("invoice") || name.contains("timeentry") || name.contains("expense")) return "Billing";
        if (name.contains("document") || name.contains("template")) return "Documents & Templates";
        if (name.contains("event") || name.contains("calendar") || name.contains("court")) return "Calendar & Court";
        if (name.contains("task") || name.contains("todo")) return "Tasks";
        if (name.contains("search") || name.contains("settings") || name.contains("workflow") || name.contains("report")) return "Read-only Administration";
        return "Companies & Matters";
    }

    private static String format(Duration duration) {
        long seconds = Math.max(0, duration.toSeconds());
        return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    record Scenario(String name, String description, String status, Duration duration, String failure, TestMetadata metadata) {
        Scenario(String name, String description, String status, Duration duration, String failure) {
            this(name, description, status, duration, failure, null);
        }
    }

    private record Narrative(String title, String preconditions, List<String> steps, String expected) {
    }
}

