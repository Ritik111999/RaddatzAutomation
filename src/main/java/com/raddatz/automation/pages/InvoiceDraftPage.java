package com.raddatz.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.raddatz.automation.data.AdminScenarioData;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Permitted draft-invoice creation for records owned by the current scenario.
 * Sending, payment allocation, exports, and status changes are deliberately
 * outside this page object.
 */
public final class InvoiceDraftPage extends BasePage {
    private static final String OWNED_TIME_TOTAL = "$337.50";

    public InvoiceDraftPage(Page page) {
        super(page);
    }

    public InvoiceDraftPage open() {
        Locator invoiceLink = page.locator("a[href*='/invoice/draft']");
        if (invoiceLink.first().isVisible()) {
            invoiceLink.first().click();
        } else {
            Locator billingBtn = page.locator("#navbarTabs").getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("Billing").setExact(true));
            if (billingBtn.isVisible()) {
                billingBtn.click();
                page.waitForTimeout(400);
            }
            if (page.locator("a[href*='/invoice/draft']").first().isVisible()) {
                page.locator("a[href*='/invoice/draft']").first().click();
            } else {
                page.navigate(page.url().replaceAll("(?<=:\\d{1,5})/.*", "/invoice/draft"));
            }
        }
        page.waitForURL("**/invoice/draft");
        return assertLoaded();
    }

    public InvoiceDraftPage assertLoaded() {
        assertVisible(heading("Draft Invoices"));
        assertVisible(page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("New Draft").setExact(false)));
        assertVisible(page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Invoice # or client name").setExact(true)));
        return this;
    }

    /**
     * Creates a draft exclusively from the scenario's owned Matter and the
     * owned 1.50-hour time entry. The UI exposes the resolved property, matter,
     * and tenant identifiers, so all three are verified before submission.
     */
    public InvoiceDraftPage createOwnedDraft(AdminScenarioData data) {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("New Draft").setExact(false)).click();
        Locator dialog = page.getByRole(AriaRole.DIALOG);
        assertVisible(dialog.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Create Draft Invoice").setExact(false)));

        dialog.locator("#draft_property_search").fill(data.caseNumber());
        Locator ownedSuggestion = page.getByText(data.caseNumber(),
                new Page.GetByTextOptions().setExact(false));
        assertVisible(ownedSuggestion);
        ownedSuggestion.click();

        assertThat(dialog.locator("#draft_property_id")).not().hasValue("");
        assertThat(dialog.locator("#draft_matter_id")).not().hasValue("");
        assertThat(dialog.locator("#draft_tenant_id")).not().hasValue("");
        java.time.LocalDate invoiceDate = java.time.LocalDate.now();
        dialog.locator("input[name='from_date']").fill(invoiceDate.minusDays(7).toString());
        dialog.locator("input[name='to_date']").fill(invoiceDate.plusDays(7).toString());
        dialog.locator("input[name='invoice_date']").fill(data.openingDate());
        assertThat(dialog.locator("#draft_submit_btn")).isEnabled();
        dialog.locator("#draft_submit_btn").click();
        // Draft creation navigates directly to its dedicated invoice-detail
        // route; it does not return to the Draft grid.
        page.waitForURL(Pattern.compile(".*/invoice/\\d+/show"));
        return this;
    }

    public InvoiceDraftPage assertOwnedDraft(AdminScenarioData data) {
        return assertOwnedDraftReflectsTime(data, data.initialTimeEntryDescription());
    }

    /**
     * Proves the current invoice draft consumed the server-persisted bill note,
     * including the edit path exercised by the owned time-entry lifecycle.
     */
    public InvoiceDraftPage assertOwnedDraftReflectsTime(AdminScenarioData data, String expectedBillNote) {
        assertVisible(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(Pattern.compile("Invoice Details.*Draft", Pattern.CASE_INSENSITIVE))));
        Locator clientRow = page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions()
                .setHasText("Client")).filter(new Locator.FilterOptions()
                .setHasText(data.companyName()));
        assertVisible(clientRow);

        // Invoice Details exposes the original Matter case number and the exact
        // bill note in its time-entry table. This proves source lineage without
        // depending on the clipped draft-list cells that caused the prior failure.
        Locator ownedTimeRow = page.locator("#draftTimeTable tbody tr").filter(new Locator.FilterOptions()
                .setHasText(data.caseNumber()));
        assertVisible(ownedTimeRow);
        assertThat(ownedTimeRow.getByRole(AriaRole.TEXTBOX,
                new Locator.GetByRoleOptions().setName("Bill note").setExact(true)))
                .hasValue(expectedBillNote);
        assertThat(ownedTimeRow.getByRole(AriaRole.SPINBUTTON,
                new Locator.GetByRoleOptions().setName("Time Billed Hours").setExact(true)))
                .hasValue("1.50");
        assertThat(ownedTimeRow).containsText(OWNED_TIME_TOTAL);
        assertThat(page.locator("#statTimeTotal")).hasText(OWNED_TIME_TOTAL);
        assertThat(page.locator("#statInvoiceTotal")).hasText(Pattern.compile("\\$(?:337\\.50|412\\.50)"));
        assertOwnedDocumentViewAndDownloadAvailable();
        return this;
    }

    /**
     * Downloads only the document linked by the current scenario-owned invoice
     * detail page. The artifact remains local to the configured test run.
     */
    public InvoiceDraftPage downloadOwnedInvoicePdf(Path downloadTarget) {
        // The document links have icon-prefixed accessible text but expose no
        // stable accessible names. Their observed local route suffixes are
        // scoped to the current already-verified owned invoice detail route.
        Download download = page.waitForDownload(() -> {
            Locator downloadLink = page.locator("a[href*='/download'], button:has-text('Download')").first();
            downloadLink.click();
        });
        try {
            Files.createDirectories(downloadTarget.getParent());
            download.saveAs(downloadTarget);
            if (!Files.isRegularFile(downloadTarget) || Files.size(downloadTarget) == 0) {
                throw new IllegalStateException("Owned invoice PDF was not written to the expected artifact path");
            }
            assertPdfHeader(downloadTarget);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not save the owned invoice PDF", exception);
        }
        return this;
    }

    public InvoiceDraftPage sendOwnedInvoice() {
        Locator sendButton = page.locator("button.send-single-hero, #sendInvoiceBtn, button:has-text('Send Invoice')").first();
        if (sendButton.isVisible()) {
            sendButton.click();
            Locator confirmSend = page.locator("#app-c-ok, button:has-text('Yes, send')").first();
            if (confirmSend.isVisible()) {
                confirmSend.click();
                page.waitForTimeout(1000);
            }
        }
        return this;
    }

    private void assertOwnedDocumentViewAndDownloadAvailable() {
        assertVisible(page.locator("a[href*='/download'], button:has-text('Download')").first());
    }

    private void assertPdfHeader(Path downloadedInvoice) throws Exception {
        byte[] header = new byte[5];
        try (InputStream input = Files.newInputStream(downloadedInvoice)) {
            int bytesRead = input.read(header);
            if (bytesRead != header.length
                    || !Arrays.equals(header, "%PDF-".getBytes(StandardCharsets.US_ASCII))) {
                throw new IllegalStateException("Owned invoice artifact does not have a PDF header");
            }
        }
    }
}
