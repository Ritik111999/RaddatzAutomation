# Raddatz PWA — Module Procedures (POM Step-by-Step)

This document provides step-by-step procedures for every automated functional flow across Raddatz PWA modules, using **only** Page Object Model (POM) methods.

---

## Standard Session Lifecycle Wrapper

Every test scenario must adhere to the standardized lifecycle:

```text
1. Authenticate Admin (via LoginPage or restored Playwright session)
2. Handle any sensitive gateway access (BillingSessionPage if accessing financial routes)
3. Execute Module Procedure (calling POM methods only)
4. Verify Assertions & Cross-Screen State Persistence
5. Clean up owned records (if scenario creates temporary records)
```

---

## Module 01 — Portfolio Hierarchy (Company $\rightarrow$ Property $\rightarrow$ Matter)

### Procedure: Create 3-Tier Hierarchy
1. Navigate via `Sidebar.goToClients()`.
2. Open Company creation via `ClientMatterListPage.openCompanyCreation()`.
3. Fill and submit company: `CompanyFormPage.fillCompanyForm(data).submit()`.
4. Open newly created company details: `ClientMatterListPage.openCompany(data.companyName())`.
5. Click "+ Property" to open Property form: `CompanyDetailsPage.createProperty()`.
6. Fill and submit property: `PropertyFormPage.fillPropertyForm(data).submit()`.
7. Open property details: `CompanyDetailsPage.openProperty(data.propertyName())`.
8. Click "+ Matter" to open Matter form: `PropertyDetailsPage.createMatter()`.
9. Fill and submit matter: `MatterFormPage.fillMatterForm(data).submit()`.
10. Assert matter workspace loaded: `MatterWorkspacePage.assertLoaded(data.tenantName())`.

---

## Module 02 — Multi-Tenant Matter Management

### Procedure: Add Secondary Tenants to Existing Matter
1. Navigate to Matter: `ClientMatterListPage.openMatter(data.tenantName())`.
2. On General Tab, click "Add Another Tenant": `MatterWorkspacePage.addAdditionalTenant()`.
3. Fill secondary tenant details (name, phone, email, SSN last 4).
4. Click `Save Changes`.
5. Reload and assert both primary and secondary tenants are rendered: `MatterWorkspacePage.assertTenantPresent(secondaryName)`.

---

## Module 03 — Court Updates & Calendar Event Cross-Scheduling

### Procedure: Schedule Event from Court Updates
1. Navigate via `Sidebar.goToCourtUpdates()`.
2. Click `#addCourtDateRowBtn` ("Add Event"): `CourtUpdatesPage.openEventForm()`.
3. Application navigates to `/calendar/add-event?from=court-updates&return_to=court-updates`.
4. Fill event details: `EventFormPage.fillEvent(data.eventTitle(), date, time, courtLocation).submit()`.
5. Application redirects back to `/court-updates`.
6. Assert event is rendered in the Calendar Entries sidebar for that date: `CourtUpdatesPage.assertEventPresent(data.eventTitle())`.
7. Navigate to `/calendar`: `Sidebar.goToCalendar()`.
8. Assert event appears in Calendar grid under Court Dates: `CalendarPage.assertEventPresent(data.eventTitle())`.

---

## Module 04 — Court Updates To-Do Management

### Procedure: Add, View & Complete To-Do from Court Updates
1. Navigate via `Sidebar.goToCourtUpdates()`.
2. Select scheduled hearing in Calendar Entries sidebar.
3. Click `#addTodoBtn` ("Add To-Do"): `CourtUpdatesPage.openAddTodoModal()`.
4. Form pre-fills Matter ID and read-only Tenant Display.
5. Enter task title, due date, choose Assignee: `CourtUpdatesPage.fillCourtTodo(title, dueDate, assignee)`.
6. Click "Add To-Do" inside modal: task is queued into `#linkedTodoSection`.
7. Fill hearing notes and select case status: `CourtUpdatesPage.fillNotes(notes).setCaseStatus("active")`.
8. Click "Submit Court Update": `CourtUpdatesPage.submit()`.
9. Verify in Court Updates grid: `.cu-todo-icon` has class `.has-todo`.
10. Click `.cu-todo-icon`: verify `.cu-todo-popover` displays task title and assignee.
11. Navigate to `/todos`: `Sidebar.goToTodos()`.
12. Assert task appears in the Central Task Manager with linked Matter badge: `TodoPage.assertTaskPresent(title)`.
13. Navigate to Matter Workspace: `ClientMatterListPage.openMatter(tenantName)`.
14. Click Todo tab: assert task is listed under Matter Tasks: `MatterWorkspacePage.openTodoTab().assertTaskPresent(title)`.

---

## Module 05 — Court Time Auto-Billing Propagation

### Procedure: Log Court Time and Propagate to Invoices
1. In Court Updates, submit hearing notes for the scheduled event.
2. Click "Court Time" shortcut: `CourtUpdatesPage.continueToCourtTime(password)`.
3. In `/court/billing`, locate the appearance row: `CourtTimePage.findAppearanceRow(eventTitle)`.
4. Enter billable hours (e.g. 1.5) and rate (e.g. $250.00): `CourtTimePage.logHours(hours, rate).save()`.
5. Navigate to `/time`: `Sidebar.goToTime()`.
6. Assert an unbilled Time Entry exists matching the exact Matter, hours, and rate: `TimeEntryPage.assertUnbilledEntry(tenantName, hours, rate)`.
7. Navigate to `/invoice/draft`: `Sidebar.goToInvoiceDrafts()`.
8. Click "New Draft": `InvoiceDraftPage.createNewDraft(companyName, tenantName)`.
9. Assert draft line items include the Court Time entry: `InvoiceDraftPage.assertLineItem(hours, rate, total)`.
10. Click "Download" to verify generated PDF: `InvoiceDraftPage.downloadPdf()`.
11. Click "Send" to finalize invoice: `InvoiceDraftPage.send()`.
12. Reload `/time`: assert time entry status changed to `BILLED`: `TimeEntryPage.assertEntryBilled(tenantName)`.

---

## Module 06 — Document Templates & Dynamic Variable Merging

### Procedure: Generate Matter Legal Document from Template
1. Navigate via `Sidebar.goToDocumentTemplates()`.
2. Verify template metadata & preview: `DocumentTemplatesPage.previewTemplate(templateName)`.
3. Navigate to target Matter Workspace: `ClientMatterListPage.openMatter(tenantName)`.
4. Click Documents tab: `MatterWorkspacePage.openDocuments()`.
5. Click "Generate from Template": `MatterDocumentsPage.openTemplateGenerator()`.
6. Select target template: `MatterTemplateGenerationPage.selectTemplate(templateName)`.
7. Assert preview dynamically replaces tokens (`{{tenant_name}}` $\rightarrow$ real tenant name, `{{property_address}}` $\rightarrow$ real address): `MatterTemplateGenerationPage.assertTokensResolved(tenantName, address)`.
8. Click "Generate & Save Document": document is saved to Matter Documents.
9. Assert document is downloadable and visible in Matter Documents list: `MatterDocumentsPage.assertDocumentPresent(docTitle)`.

---

## Module 07 — Negative & Boundary Guard Validations

### Procedure: Input Rejection & Safety Enforcement
1. **Invalid Court Date:** Attempt to submit Court Date in past without override $\rightarrow$ assert validation error toast.
2. **Missing Required Fields:** Attempt to submit Company, Property, or Matter with blank required fields $\rightarrow$ assert HTML5 / backend constraint blocks submission.
3. **Restricted Zone Access Guard:** Ensure automation aborts and logs critical warning if any attempt is made to reach `/settings/integrations`, payment gateways, or bulk delete.
4. **Unbilled Time Deletion Guard:** Attempt to delete a billed time entry $\rightarrow$ assert action disabled or rejected.
