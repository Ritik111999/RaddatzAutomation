package com.raddatz.automation.reports;

import com.raddatz.automation.config.AutomationConfig;

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
        Path html = directory.resolve("raddatz-qa-report.html");
        try {
            Files.writeString(html, document(config, directory.getFileName().toString(), started, finished, scenarios), StandardCharsets.UTF_8);
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
                    .append(moduleSkipped).append("</td><td>").append(badge(moduleFailed == 0 ? "PASS" : "FAIL")).append("</td></tr>");
        });
        body.append("</tbody></table><h2>Scenario Register</h2><table><thead><tr><th>Scenario</th><th>Outcome</th><th>Duration</th><th>Scope</th></tr></thead><tbody>");
        scenarios.stream().sorted(Comparator.comparing(Scenario::name)).forEach(s -> body.append("<tr><td><b>")
                .append(escape(s.name())).append("</b><br><span class='muted'>").append(escape(s.description()))
                .append("</span></td><td>").append(badge(s.status())).append("</td><td>").append(escape(format(s.duration())))
                .append("</td><td>").append(escape(moduleFor(s))).append("</td></tr>"));
        body.append("</tbody></table></section>");
        return "<!doctype html><html><head><meta charset='utf-8'><title>Raddatz PWA QA Report</title><style>"
                + "@page{size:A4;margin:16mm 14mm 16mm 14mm;}body{font-family:Arial,sans-serif;color:#14213d;font-size:10pt;margin:0;}"
                + ".cover{min-height:250mm;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;page-break-after:always;}"
                + ".cover h1{font-size:36pt;margin:0 0 18pt}.cover h2{font-size:22pt;color:#384a67;margin:0 0 32pt}.cover p{font-size:13pt;margin:8pt}.content{padding-top:2mm}"
                + "h2{font-size:17pt;border-bottom:2px solid #25408f;padding-bottom:5pt;margin:20pt 0 10pt;color:#172d68}.cards{display:grid;grid-template-columns:repeat(5,1fr);gap:7pt;margin:8pt 0 18pt}.card{border-radius:6pt;padding:9pt;text-align:center;border:1px solid #d6deef}.label{font-size:8pt;color:#4e5d78}.value{font-size:18pt;font-weight:bold;margin-top:4pt}.pass{background:#eefaf2;color:#167144}.fail{background:#fff0f0;color:#b42318}.neutral{background:#f3f6fb;color:#25408f}"
                + "table{border-collapse:collapse;width:100%;margin:8pt 0 18pt;page-break-inside:auto}th{background:#25408f;color:#fff;text-align:left}th,td{border:1px solid #d7deea;padding:6pt;vertical-align:top}tr{page-break-inside:avoid}.metadata th{background:#eaf0fb;color:#172d68;width:16%}.metadata td{width:34%}.badge{display:inline-block;border-radius:8pt;padding:2pt 6pt;font-size:8pt;font-weight:bold}.badge.pass{background:#dff7e8;color:#126b38}.badge.fail{background:#ffe3e3;color:#a21313}.badge.skip{background:#edf1f7;color:#4e5d78}.muted{color:#5b6880;font-size:8pt}</style></head><body>"
                + body + "</body></html>";
    }

    private static String card(String label, Object value, String tone) {
        return "<div class='card " + tone + "'><div class='label'>" + escape(label) + "</div><div class='value'>" + escape(String.valueOf(value)) + "</div></div>";
    }

    private static String badge(String status) {
        return "<span class='badge " + status.toLowerCase(java.util.Locale.ROOT) + "'>" + escape(status) + "</span>";
    }

    private static String moduleFor(Scenario scenario) {
        String name = scenario.name();
        if (name.contains("Invoice") || name.contains("TimeEntry") || name.contains("Expense")) return "Billing";
        if (name.contains("Document") || name.contains("Template")) return "Documents & Templates";
        if (name.contains("Event") || name.contains("Calendar") || name.contains("Court")) return "Calendar & Court";
        if (name.contains("Task") || name.contains("Todo")) return "Tasks";
        if (name.contains("Search") || name.contains("settings") || name.contains("workflow") || name.contains("report")) return "Read-only Administration";
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

    record Scenario(String name, String description, String status, Duration duration) {
    }
}
