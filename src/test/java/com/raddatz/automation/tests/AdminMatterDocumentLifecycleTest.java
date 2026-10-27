package com.raddatz.automation.tests;

import com.raddatz.automation.core.reporting.TestMetadata;
import com.raddatz.automation.data.AdminScenarioData;
import com.raddatz.automation.data.UploadFixtures;
import com.raddatz.automation.pages.Sidebar;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.time.Clock;

/** Uploads, verifies, downloads, and deletes a uniquely named owned case document. */
public final class AdminMatterDocumentLifecycleTest extends ReadOnlyTestBase {
    @Override
    protected boolean requiresMutationPermission() {
        return true;
    }

    @TestMetadata(
            module = "Matter360",
            scenarioId = "RAD-DOCS-001",
            title = "Matter Case Evidence Document Upload, Listing, Download, and Deletion",
            testType = TestMetadata.TestType.POSITIVE,
            expectedResult = "Case document file is uploaded, listed with size and timestamp, verified via local binary download, and deleted safely without side effects.",
            preconditions = "An owned Matter case and controlled document fixture exist.",
            testSteps = {
                    "Create parent Company, Property, and Matter chain",
                    "Open Matter Workspace and navigate to Documents tab",
                    "Open Custom Upload modal and upload controlled inspection memorandum",
                    "Assert document is listed with exact file name",
                    "Download document artifact and verify local file existence and size",
                    "Delete uploaded document and verify absence from Matter document library"
            },
            defectSeverity = TestMetadata.DefectSeverity.HIGH
    )
    @Test(groups = "admin-mutation", description = "Administrator manages an owned Matter document without portal, email, timeline, or note side effects.")
    public void administratorManagesOwnedMatterDocumentLifecycle() {
        AdminScenarioData data = AdminScenarioData.create(Clock.systemUTC());
        Path uploadFixture = UploadFixtures.copyInspectionMemorandum(data.ownedDocumentFileName(), config.artifactDirectory());
        Path downloadedCopy = config.artifactDirectory().resolve("downloads").resolve(data.ownedDocumentFileName());
        var clients = new Sidebar(currentPage()).goToClients();

        clients.openAddCompany()
                .createOwnedCompany(data)
                .openAddProperty()
                .createOwnedProperty(data);

        new Sidebar(currentPage()).goToClients()
                .openAddMatter()
                .createOwnedMatter(data)
                .openOwnedMatter(data.tenantName(), data.caseNumber())
                .openDocuments()
                .openCustomUpload()
                .uploadOwnedDocument(uploadFixture)
                .assertOwnedDocumentListed(data.ownedDocumentFileName())
                .downloadOwnedDocument(data.ownedDocumentFileName(), downloadedCopy)
                .deleteOwnedDocument(data.ownedDocumentFileName());
    }
}
