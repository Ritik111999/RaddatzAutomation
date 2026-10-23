package com.raddatz.automation.tests;

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
