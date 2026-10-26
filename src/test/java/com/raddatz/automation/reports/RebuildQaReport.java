package com.raddatz.automation.reports;

import com.raddatz.automation.config.AutomationConfig;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/** Rebuilds a report from saved TestNG evidence without opening the application. */
public final class RebuildQaReport {
    private RebuildQaReport() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Expected saved TestNG XML and new output directory");
        Path source = Path.of(args[0]).toAbsolutePath();
        Path output = Path.of(args[1]).toAbsolutePath();
        if (Files.exists(output.resolve("raddatz-qa-report.pdf"))) {
            throw new IllegalArgumentException("Use a new output directory to preserve previously generated reports");
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        var document = factory.newDocumentBuilder().parse(source.toFile());
        var suites = document.getElementsByTagName("suite");
        if (suites.getLength() != 1) throw new IllegalArgumentException("Expected evidence for exactly one suite");
        Element suite = (Element) suites.item(0);
        var timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss z", Locale.US)
                .withZone(java.time.ZoneId.of("Asia/Kolkata"));
        // TestNG emits IST; resolve it explicitly to the client's India timezone.
        var start = ZonedDateTime.parse(suite.getAttribute("started-at").replace(" IST", " Asia/Kolkata"), timestamp).toInstant();
        var finish = ZonedDateTime.parse(suite.getAttribute("finished-at").replace(" IST", " Asia/Kolkata"), timestamp).toInstant();
        var results = new ArrayList<RaddatzQaPdfReport.Scenario>();
        var methods = document.getElementsByTagName("test-method");
        for (int i = 0; i < methods.getLength(); i++) {
            Element method = (Element) methods.item(i);
            if ("true".equals(method.getAttribute("is-config"))) continue;
            var exceptions = method.getElementsByTagName("exception");
            String failure = exceptions.getLength() == 0 ? "None recorded"
                    : ((Element) exceptions.item(0)).getAttribute("class") + " (cause requires triage; exception content omitted)";
            results.add(new RaddatzQaPdfReport.Scenario(method.getAttribute("name"), method.getAttribute("description"),
                    method.getAttribute("status"), Duration.ofMillis(Long.parseLong(method.getAttribute("duration-ms"))), failure));
        }
        if (results.isEmpty()) throw new IllegalArgumentException("No executed scenarios in supplied evidence");
        Files.createDirectories(output);
        Files.copy(source, output.resolve("source-testng-results.xml"));
        String executionId = "run_" + DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                .withZone(java.time.ZoneId.of("Asia/Kolkata")).format(start);
        RaddatzQaPdfReport.write(AutomationConfig.load(), output, executionId, start, finish, results);
    }
}
