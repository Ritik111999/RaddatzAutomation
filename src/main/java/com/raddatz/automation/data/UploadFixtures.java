package com.raddatz.automation.data;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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

    /**
     * Gives every run a uniquely named copy of the fictional memorandum so a
     * document row can be unambiguously identified and safely cleaned up.
     */
    public static Path copyInspectionMemorandum(String fileName, Path artifactDirectory) {
        try {
            Path fixtureCopyDirectory = artifactDirectory.resolve("upload-fixtures");
            Files.createDirectories(fixtureCopyDirectory);
            Path fixtureCopy = fixtureCopyDirectory.resolve(fileName);
            return Files.copy(inspectionMemorandum(), fixtureCopy, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not prepare unique document fixture", exception);
        }
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
