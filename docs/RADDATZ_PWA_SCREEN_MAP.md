# Raddatz PWA — Admin Screen Map

Observed on 2026-10-22 using the supplied administrator account.  This was a read-only survey: no records were created, changed, sent, generated, or deleted.

## Persistent shell

Every authenticated screen includes the top bar, notification drawer, account menu, left navigation, and export modal.  The export modal advertises PDF, Excel, and CSV options.  The account menu routes to Profile, Theme Settings, and Help & Guides.

## Navigation map

| Area | Route(s) and screens | Automation notes |
| --- | --- | --- |
| Dashboard | `/dashboard` — My Tasks, Office Tasks, weekly calendar, audit-log preview, task add/edit modals | Read-only smoke: assert greeting, task/table cards and calendar. Never tick task checkboxes. |
| Clients | `/client-and-matter` — Companies & Matters search/list/export | Drill-down family: client, property, and matter detail pages. |
| To-dos | `/todos/all` — filters, summaries, task grid, report and bulk-action dialogs | Bulk complete/delete and individual delete are destructive: exclude from normal suites. |
| Calendar | `/calendar/view-full-calendar?view=week` — Month/Week/Day, event detail/reschedule, historical archive | Assert views and navigation only; do not save/reschedule events. |
| Billing (gate) | `/confirm-sensitive-access/billing` — password gate; then `/time`, `/expenses`, `/invoice/draft`, `/billing`, `/court/billing` | A Billing Session is required. Creation, send, void, allocation, update and delete controls must be negative-tested only. |
| Workflows | `/workflow` — workflow list | List/view smoke. |
| Court Updates | `/court/updates` — court-update editor, hearing notes, related to-do dialog | Submit, edit and delete controls are mutation boundaries. |
| Client Portal | `/documents` — folder/list/recent/document view; `/spreadsheets` — property-spreadsheet list/search | Assert filters/view switches, not file modification. |
| Bulk Uploader | `/mass-document` — matter selector, document-template selection, facts, generation options | Do not click Generate Documents in shared environments. |
| Staff | `/staff` — staff summary/list/export | No delete or edit in standard read-only tests. |
| Reports (gate) | `/confirm-sensitive-access/reports`; `/reports` | Reauthentication required before report routes. |
| Custom Fields | `/settings/custom-fields` — list/filter plus create/edit/delete modals | Admin mutation screen; cover only presence and validation without saving. |
| Email Templates | `/settings/email-templates` — preview, responsive modes, send-test and activation controls | Preview is safe; sending/toggling is excluded. |
| User Permissions | `/settings/permissions` — Roles & Permissions | Assert controls only; never Save Changes. |
| Data & Import | `/settings/data-import` — import, export, validation and danger zone | `Delete Test Data` and `Reset System` are hard-blocked. |
| Masters | `/masters/case-types`, `/masters/case-statuses`, `/masters/industries`, `/masters/court-rooms`, `/masters/hearing-types`, `/masters/event-types`, `/masters/event-categories`, `/masters/reminder-templates`, `/masters/business-types`, `/masters/document-template-categories`, `/masters/roles` | Same list + add/edit/deactivate/delete pattern; create/update/delete not part of smoke/regression runs. |
| Document templates | `/document-templates/create` — information, billing entries, variables, document/Word upload, preview | Safe to assert form sections. Do not upload or save. |
| Profile | `/profile` — identity/contact fields and profile image | Assert load only; no save/upload. |
| Theme | `/settings/theme` — preset colors, live preview, company/property preview | Never Save Theme or Reset. |
| Help | `/help` — guide search/categories; `/help/manage` — articles/categories/media; `/help/create` — article editor | Create/publish and media upload are excluded. |
| Audit log | `/audit-log` — search/date filters and change-detail view | Read-only and useful for post-run assertions. |
| Notifications | `/settings/notifications` — alert recipients/delivery; `/settings/notifications/email-log` — sent-mail search/filter | Do not change recipients. |
| Integrations | `/settings/integrations` — connected services, configuration and API-access controls | Connect, disconnect, configure and regenerate API keys are blocked. |
| Billing settings | `/settings/billing-configuration` — invoice settings and expense rules | Requires Billing Session; assert-only. |

## Client, property and matter hierarchy

The Companies & Matters list is the entry point.  A selected company exposes Overview, Properties, Cases, Notes, Documents, Billing/Invoices and contacts.  A property exposes Overview, active/inactive cases, documents, notes and contacts.  A selected matter exposes:

- `/matters/{id}/general` — tenant/case summary, key dates, task summary and quick actions
- `/matters/{id}/edit` — client/property, tenants, case information, team and notes
- `/client-and-matter/matter/{id}/notes` — note composer/history
- `/matters/{id}/documents` — file list, drop zone, PandaDoc handoff
- `/matters/{id}/todos` — case-task filters/list/editor
- `/matters/{id}/timeline` — case event history
- `/matters/{id}/add-documents` — document-template selection

Use an existing fixture matter selected by the test configuration; do not create a new client, property, tenant, case, task, event, document, invoice, note, or billing entry for discovery tests.

## Report tabs

After the Reports password gate, the application exposes:

- `/reports?tab=financial` — Financial Summary / recent transactions
- `/reports?tab=time` — Time Entries, billable split, attorney hours, top matters
- `/reports?tab=staff` — workload, completion and unassigned tasks
- `/reports?tab=cases` — case activity, type trend, inactive cases, court dates
- `/reports?tab=clients` — client statements (contains Send and Generate All Statements; do not activate)
- `/reports?tab=client_notes` — client notes
- `/reports?tab=activity` — system activity and detail dialog

## Automation architecture recommended for this application

```text
src/main/java/.../
  core/        DriverFactory, Config, Waits, TestListener
  pages/       BasePage, LoginPage, Sidebar, DashboardPage, ...
  components/  TopBar, ExportModal, ConfirmSensitiveAccess, MatterTabs
  flows/       LoginFlow, BillingSessionFlow, MatterNavigationFlow
  reports/     ExtentReportManager, PdfReportExporter
  utils/       Logger, Screenshot, TestData
src/test/java/.../
  tests/smoke/ tests/regression/ tests/readonly/ tests/negative/
```

Key Page Objects: `LoginPage`, `DashboardPage`, `ClientMatterListPage`, `CompanyPage`, `PropertyPage`, `MatterGeneralPage`, `MatterNotesPage`, `MatterDocumentsPage`, `MatterTodosPage`, `MatterTimelinePage`, `CalendarPage`, `TodoPage`, `BillingAccessPage`, `TimePage`, `ExpensePage`, `InvoiceDraftPage`, `BillingPage`, `CourtTimePage`, `ReportsPage`, `SettingsPage`, `MastersPage`, and `ExportModal`.

Use TestNG groups to enforce safety: `readonly` may run in any shared environment; `mutation` is disabled by default and needs dedicated test data; `destructive` is never enabled against this administrator environment.  Persist a screenshot on failure, structured Log4j2/SLF4J logs per test, ExtentReports HTML output, and convert the final HTML report to PDF in the build’s reporting step.

## Important guardrails

- Never use controls labelled Delete, Reset, Void, Send, Generate, Import, Upload, Save, Update, Deactivate, Disconnect, Regenerate, Allocate, Complete, or Mark all read in the initial automation suite.
- The two password gates are required to inspect Billing and Reports. Keep the password only in a local secret/environment variable; never commit it.
- The top navigation and repeated Export modal are ideal component objects. Use semantic locators (accessible name, label, role) before CSS/XPath.
- Data-rich screens contain tenant, contact, billing and activity information. Reports, screenshots, logs and PDFs must redact sensitive values before being shared outside the authorized team.
