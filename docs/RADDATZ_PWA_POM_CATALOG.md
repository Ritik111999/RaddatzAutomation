# Raddatz PWA — Page Object Model (POM) Catalog

Complete inventory of all Page Object Model (POM) classes supporting the Raddatz Legal Automation Framework.

Base package: `com.raddatz.automation.pages`

---

## 1. Architectural Model

Following the ToolTrader design standard, each screen or major modal has a dedicated Page Object encapsulating Playwright Locators, Auto-Waiting assertions, and Domain Actions.

| Component | Responsibility |
| :--- | :--- |
| **Locators** | Semantic role-based, label-based, and ID selectors (`getByRole`, `getByLabel`, `getByPlaceholder`, `#id`) |
| **Actions** | User operations (navigate, fill, click, select, dismiss, submit) |
| **Assertions** | Playwright auto-waiting assertions (`assertThat(locator).isVisible()`, `.hasValue()`) |
| **State Guards** | Safe boundary enforcement, preventing mutations of unapproved or user-owned records |

---

## 2. Page Object Registry

| # | POM Class | Route / Scope | Primary Operations |
|---|:---|:---|:---|
| 1 | `LoginPage` | `/login` | `open()`, `signIn()`, `assertLoaded()` |
| 2 | `Sidebar` | Universal Navigation | `goToDashboard()`, `goToClients()`, `goToTodos()`, `goToCalendar()`, `goToCourtUpdates()` |
| 3 | `DashboardPage` | `/dashboard` | Metric review, recent tasks, upcoming hearings, sensitive billing entrypoint |
| 4 | `ClientMatterListPage` | `/client-and-matter` | Company list, search filter, property list, matter navigation, `openCompanyCreation()` |
| 5 | `CompanyFormPage` | `/client-and-matter/client/create` | `fillCompanyForm()`, `submit()`, field validation |
| 6 | `CompanyDetailsPage` | `/client-and-matter/client/{id}` | Notes tab, Documents tab, Properties list, `createProperty()`, `createInvoice()` |
| 7 | `PropertyFormPage` | `/property/create` | Address autofill, management company link, contact info, `submit()` |
| 8 | `PropertyDetailsPage` | `/property/{id}` | Property notes, documents, matters list, `createMatter()` |
| 9 | `MatterFormPage` | `/create-matter` | Tenant details, case number, rental amount, lease dates, multi-tenant creation |
| 10 | `MatterWorkspacePage` | `/matters/{id}` | Tabs: General, Notes, Documents, Todo, Timeline; shortcuts: `openNotes()`, `openDocuments()`, `openTimeEntry()` |
| 11 | `MatterNotesPage` | `/matters/{id}/notes` | `addNote()`, `assertNotePresent()`, `deleteOwnedNote()` |
| 12 | `MatterDocumentsPage` | `/matters/{id}/documents` | Document upload, template generation shortcut, file download |
| 13 | `MatterDocumentUploadPage` | Modal / `/matters/{id}/upload` | File dropzone, document title, document category selection |
| 14 | `MatterTemplateGenerationPage`| `/matters/{id}/generate` | Template selection, variable preview (`{{tenant_name}}`), document creation |
| 15 | `TodoPage` | `/todos` | Central tasks list, `createTask()`, `completeTask()`, filter by Overdue/Today/Staff |
| 16 | `TaskFormModal` | Modal (Global & Matter) | Task title, due date, assigned staff, priority, matter linkage |
| 17 | `CalendarPage` | `/calendar` | Day/Week/Month views, `choosePickerValue()`, event filtering, click date to schedule |
| 18 | `EventFormPage` | `/calendar/add-event` | Hearing title, date, time, court location, docket number, matter linkage |
| 19 | `EventDetailsPage` | `/calendar/{id}` | Event metadata review, reschedule, mark completed, delete |
| 20 | `CourtUpdatesPage` | `/court-updates` | Calendar Entries sidebar, `createOwnedCourtUpdate()`, `addCourtTodo()`, `editCourtTodo()`, `deleteCourtTodo()`, `addExtraEvent()` |
| 21 | `CourtTimePage` | `/court/billing` | Court appearances list, billable hours entry, hourly rate, `saveCourtTime()` |
| 22 | `BillingSessionPage` | `/confirm-sensitive-access/*` | Password verification gateway for sensitive financial and billing sections |
| 23 | `TimeEntryPage` | `/time` | Matter autocomplete, date, hours, rate, billable flag, `addTimeEntry()`, edit/delete |
| 24 | `ExpensePage` | `/expenses` | Matter autocomplete, payee, expense code, amount, `addExpense()`, edit/delete |
| 25 | `InvoiceDraftPage` | `/invoice/draft` | `newDraft()`, client selection, date range, unbilled aggregation, `send()`, PDF download |
| 26 | `DocumentTemplatesPage` | `/document-templates` | Template list, preview `#templatePreviewModal`, template metadata lifecycle |
| 27 | `DocumentTemplateFormPage` | `/document-templates/create` | Template name, category, body editor, variable token insertion |
| 28 | `SearchFilterPage` | Universal | Table search, date range filtering, export triggers, report session |
| 29 | `OwnedHierarchyEditorPage`| Cross-tier | In-place editing of Company, Property, and Matter records owned by test run |

---

## 3. Reusable Cross-Cutting Components

1. **Matter Autocomplete Component:**
   - Standardized search input targeting `input[placeholder*='Search by tenant name']`.
   - Dual-strategy query: searches first by `tenantName`, falls back to `caseNumber`.
   - Waits for suggestion container `.autocomplete-suggestions, .list-group-item` before clicking.

2. **Sensitive Access Modal Gate (`BillingSessionPage`):**
   - Automatically detects `/confirm-sensitive-access` routes.
   - Enters secure admin session password and waits for target financial route navigation.

3. **Court Updates Linked To-Do Popover Component:**
   - Interacts with `.cu-todo-icon` and asserts `.has-todo`.
   - Inspects `.cu-todo-popover` containing `.pop-val` and `.pop-label` without navigating away from Court Updates.
