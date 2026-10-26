package com.raddatz.automation.reports;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/** Renders Extent's HTML report to PDF with Playwright Chromium. */
public final class PdfReportExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfReportExporter.class);

    private PdfReportExporter() {
    }

    public static void export(Path html, Path pdf) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            page.navigate(html.toUri().toString());
            Page.PdfOptions options = new Page.PdfOptions().setPath(pdf).setFormat("A4").setPrintBackground(true);
            if (html.getFileName().toString().equals("raddatz-qa-report.html")) {
                options.setPreferCSSPageSize(true).setDisplayHeaderFooter(true)
                        .setHeaderTemplate("<div style='font-size:8px;width:100%;text-align:center;color:#64748b'>Raddatz PWA - Confidential QA execution report</div>")
                        .setFooterTemplate("<div style='font-size:8px;width:100%;text-align:center;color:#64748b'>Page <span class='pageNumber'></span> of <span class='totalPages'></span></div>");
            }
            page.pdf(options);
            browser.close();
            LOGGER.info("PDF report written to {}", pdf.toAbsolutePath());
        } catch (RuntimeException exception) {
            LOGGER.warn("HTML report is available at {}. PDF export was skipped; install Chromium with the Playwright CLI first.", html, exception);
        }
    }
}
