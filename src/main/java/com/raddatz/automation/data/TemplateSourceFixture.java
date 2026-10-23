package com.raddatz.automation.data;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** A controlled Word source containing the UI-observed merge token, not an empty-token upload sample. */
public final class TemplateSourceFixture {
    public static final String TOKEN = "{{HEARING_PREPARATION_INSTRUCTIONS}}";

    private TemplateSourceFixture() { }

    public static Path prepare(AdminScenarioData data, Path artifacts) {
        Path source = artifacts.resolve("upload-fixtures/hearing-preparation-" + data.reference() + ".docx");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>"
                + paragraph("Hearing Preparation Memorandum")
                + paragraph(data.propertyName() + " — " + data.caseNumber())
                + paragraph("Counsel instructions: " + TOKEN)
                + paragraph(data.ownershipNote())
                + "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/></w:sectPr></w:body></w:document>";
        try (ZipFile original = new ZipFile(UploadFixtures.inspectionMemorandum().toFile())) {
            Files.createDirectories(source.getParent());
            try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(source))) {
                var entries = original.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    output.putNextEntry(new ZipEntry(entry.getName()));
                    if (entry.getName().equals("word/document.xml")) output.write(xml.getBytes(StandardCharsets.UTF_8));
                    else try (var input = original.getInputStream(entry)) { input.transferTo(output); }
                    output.closeEntry();
                }
            }
            return source;
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot prepare controlled merge-field Word source", exception);
        }
    }

    public static void assertSubstituted(Path document, String value) {
        try (ZipFile zip = new ZipFile(document.toFile())) {
            var entry = zip.getEntry("word/document.xml");
            if (entry == null) throw new IllegalStateException("Generated Word file has no document body");
            String xml;
            try (var input = zip.getInputStream(entry)) { xml = new String(input.readAllBytes(), StandardCharsets.UTF_8); }
            var factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            String body = factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                    xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement().getTextContent();
            if (body.contains(TOKEN) || !body.contains(value)) {
                throw new AssertionError("Generated Word source did not replace the controlled custom merge field");
            }
        } catch (java.io.IOException | javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException exception) {
            throw new IllegalStateException("Cannot inspect generated Word download", exception);
        }
    }

    private static String paragraph(String text) {
        return "<w:p><w:r><w:t>" + escape(text) + "</w:t></w:r></w:p>";
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
