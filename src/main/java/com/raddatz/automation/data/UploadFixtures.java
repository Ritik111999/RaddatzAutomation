package com.raddatz.automation.data;

import java.net.URISyntaxException;
import java.nio.file.Path;

/** Local, fictional files used only by owned-record upload workflows. */
public final class UploadFixtures {
    private UploadFixtures() {
    }

    public static Path inspectionMemorandum() {
        return fixture("upload-fixtures/property-inspection-memorandum.docx");
    }

    public static Path propertyExteriorPhoto() {
        return fixture("upload-fixtures/property-exterior-inspection.png");
    }

    private static Path fixture(String resource) {
        try {
            var url = UploadFixtures.class.getClassLoader().getResource(resource);
            if (url == null) {
                throw new IllegalStateException("Missing upload fixture: " + resource);
            }
            return Path.of(url.toURI());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Invalid upload fixture resource: " + resource, exception);
        }
    }
}
