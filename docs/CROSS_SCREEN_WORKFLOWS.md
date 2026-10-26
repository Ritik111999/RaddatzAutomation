# Raddatz PWA — Cross-Cutting Multi-Entrypoint Workflow Architecture

This document maps all functionalities in the Raddatz Legal Case Management PWA that can be accessed from **multiple screens** and where updates propagate across **multiple modules**.

---

## 1. Executive Summary of Cross-Cutting Hubs

In Raddatz, business entities are not confined to isolated silos. Primary operational records (To-Dos, Events, Court Updates, Notes, Documents, Time Entries, and Invoices) have multiple creation and mutation entrypoints across the platform:

```
                          ┌───────────────────────────┐
                          │   Court Updates Screen    │
                          │     (/court-updates)      │
                          └─────────────┬─────────────┘
                                        │
           ┌────────────────────────────┼────────────────────────────┐
           ▼                            ▼                            ▼
┌─────────────────────┐      ┌─────────────────────┐      ┌─────────────────────┐
│  To-Do Management   │      │  Event Scheduling   │      │  Court Time Billing │
│   - Add To-Do Modal │      │   - Add/Edit Event  │      │   - Auto Appearance │
│   - In-line Popover │      │   - Extra Court Date│      │   - Hourly Rate/Time│
└──────────┬──────────┘      └──────────┬──────────┘      └──────────┬──────────┘
           │                            │                            │
           ▼                            ▼                            ▼
┌─────────────────────┐      ┌─────────────────────┐      ┌─────────────────────┐
│  Central /todos     │      │  Central /calendar  │      │  Central /time      │
│  & Matter Workspace │      │  & Matter Timeline  │      │  & /invoice/draft   │
└─────────────────────┘      └─────────────────────┘      └─────────────────────┘
```

---

## 2. Multi-Entrypoint Capability Matrix

| Business Entity | Primary Screen | Secondary Entrypoints & Modals | Downstream Propagation / Update Locations |
| :--- | :--- | :--- | :--- |
| **To-Dos / Tasks** | `/todos` (`Create Task`) | • **Court Updates:** `#addTodoBtn` $\rightarrow$ `#courtAddTodoModal`<br>• **Matter Workspace:** `Add Task` & `Create Client To Do`<br>• **Court Time:** `Create To-do` button | 1. `/todos` (Central Task Manager with filtering)<br>2. `/matters/{id}` (Todo tab)<br>3. `/court-updates` (`.cu-todo-icon.has-todo` + popover)<br>4. Dashboard task widgets |
| **Events / Hearings** | `/calendar` (`Add Event` / click date) | • **Court Updates:** `#addCourtDateRowBtn` (`openCourtUpdatesEventForm`) $\rightarrow$ Schedule Court Date<br>• **Matter Workspace:** `Schedule Event` & `Add/Manage Key Dates` | 1. `/calendar` (Month/Week/Day/Agenda views)<br>2. `/court-updates` (Calendar Entries strip for that date)<br>3. `/matters/{id}` (Timeline & key dates)<br>4. `/court/billing` (generates an appearance row) |
| **Court Updates** | `/court-updates` (Calendar Entries strip) | • Click any scheduled hearing button<br>• Link from `/matters/{id}` Timeline | 1. Court Updates history grid<br>2. Updates Matter status (Active, Hearing, Closed)<br>3. Auto-logs appearance in `/court/billing`<br>4. Records notes in Matter Timeline |
| **Court Time & Billing** | `/court/billing` (`Save` / `Update` row) | • Completing Court Update on `/court-updates`<br>• Add Entry inside `/court/billing` | 1. Auto-creates billable time entry in `/time`<br>2. Automatically pulled into `/invoice/draft` |
| **Time Entries** | `/time` (`Save & New`) | • **Matter Workspace:** `Add Time Entry` button<br>• **Court Time:** Auto-creation from court appearances | 1. Appears in `/time` grid<br>2. Included in Client/Matter financial summary<br>3. Pulled into `/invoice/draft` as billable line item |
| **Expenses** | `/expenses` (`Save & New`) | • **Matter Workspace:** `Log Expense` button | 1. Appears in `/expenses` grid<br>2. Pulled into `/invoice/draft` as reimbursable expense |
| **Invoices** | `/invoice/draft` (`New Draft`) | • **Company Details:** `Create Invoice` button<br>• **Matter Workspace:** `Create Invoice` button | 1. Draft created with auto-calculated total of unbilled time + expenses<br>2. Finalized via `Send`<br>3. Marks referenced Time Entries and Expenses as "Billed"<br>4. Downloadable as PDF |
| **Notes** | Respective entity screens | • **Company Details:** Notes tab<br>• **Property Details:** Notes tab<br>• **Matter Workspace:** Notes tab<br>• **Court Updates:** Hearing Notes | 1. Respective Notes tabs<br>2. Matter Timeline aggregates all Notes and Court Updates chronologically |
| **Documents** | `/document-templates` & `/matters/{id}/documents` | • **Company Details:** `Upload Document`<br>• **Property Details:** `Upload Document`<br>• **Matter Workspace:** `Upload Document` or `Generate from Template` | 1. Respective Documents tabs<br>2. Template variables (`{{tenant_name}}`, `{{property_address}}`) dynamically merged from Matter records |

---

## 3. Deep-Dive: Court Updates as a Central Hub

### A. To-Do Management from Court Updates
1. **Trigger:** User clicks `#addTodoBtn` ("Add To-Do") in the Court Updates form.
2. **Modal (`#courtAddTodoModal`):**
   - Pre-fills `courtTodoMatterId` and read-only `courtTodoMatterDisplay` from the selected tenant/matter.
   - Pre-fills `courtTodoDueDate` ensuring it is never in the past.
   - Allows choosing Assignee Type (`office`, `staff`, `client`) and Assignee.
3. **Client-Side State Storage:**
   - Pending To-Dos are queued in memory: `var pendingTodos = [];`.
   - Rendered in `#linkedTodoSection` under `#pendingTodosList`.
   - Serialized into hidden inputs:
     - `pendingTodosJson`
     - `existingTodosJson`
     - `removedTodoIdsJson`
4. **Submission & Persistence:**
   - On clicking "Submit Court Update", both the court update and the linked To-Dos are committed in a single transaction.
5. **Downstream UI Representation:**
   - In the Court Updates grid, the `.cu-todo-icon` receives the `.has-todo` class (highlighted indigo).
   - Clicking `.cu-todo-icon` opens the floating `.cu-todo-popover` displaying task title, due date, assignee, and completion status without navigating away.
   - The To-Do simultaneously appears in `/todos` and `/matters/{id}` under the Todo tab.

### B. Event Scheduling from Court Updates
1. **Trigger:** User clicks `#addCourtDateRowBtn` ("Add Event" or "Edit Event").
2. **Logic (`openCourtUpdatesEventForm()`):**
   - Checks if an event is already selected (`calendar_event_id_hidden`).
   - If selected: switches to "Edit Event" mode (`params.set('edit_event_id', id)`).
   - If not selected or force-create: switches to "Add Event" mode.
   - Pre-fills `matter_id`, `event_date`, and preserves return routing (`return_to=court-updates`).
3. **Execution:**
   - Navigates to `/calendar/add-event?from=court-updates&matter_id=...&return_to=court-updates`.
   - On saving, the application redirects back to `/court-updates` with the event pre-selected in the Calendar Entries sidebar!
4. **Downstream UI Representation:**
   - Event appears on `/calendar` under the Court Dates category.
   - Event appears in the Matter Workspace Timeline.
   - Event generates a court appearance slot in `/court/billing`.

---

## 4. Deep-Dive: Matter Workspace as a 360° Management Hub

The Matter Workspace (`/matters/{id}`) serves as the central command center for all legal, operational, and financial activities for a specific tenant matter:

1. **General Tab:**
   - Tenant contact details (`Add Phone`, `Add Email`).
   - Multi-tenant management (`Add Another Tenant`).
   - Key dates (`Add/Manage Key Dates` for notices, summons, hearings).
2. **Notes Tab:**
   - Matter-scoped notes with author attribution and timestamp.
3. **Documents Tab:**
   - Document upload with file-type validation.
   - Template generation via `/document-templates` integration.
4. **Todo Tab:**
   - `Add Task`: creates matter-scoped tasks that mirror to `/todos`.
   - `Create Client To Do`: client-accessible action items.
5. **Financial Shortcuts:**
   - `Add Time Entry`: logs billable time pre-scoped to this matter.
   - `Log Expense`: logs client disbursements pre-scoped to this matter.
   - `Create Invoice`: generates draft invoice pre-scoped to this matter.
6. **Timeline Tab:**
   - Chronological audit trail aggregating Court Updates, Events, Notes, and Document generations.

---

## 5. Deep-Dive: Financial & Billing Reconciliation Pipeline

```
┌───────────────────────┐
│ Court Updates Screen  │
└───────────┬───────────┘
            │ Submits hearing outcome
            ▼
┌───────────────────────┐         ┌───────────────────────┐
│  Court Time Billing   │         │  Manual Time Entry    │
│   (/court/billing)    │         │       (/time)         │
└───────────┬───────────┘         └───────────┬───────────┘
            │ Generates approved              │
            │ billable time entry             │
            └─────────────────┬───────────────┘
                              │
                              ▼
                 ┌─────────────────────────┐         ┌─────────────────────────┐
                 │  Active Time Ledger     │         │   Expenses Ledger       │
                 │   (Status: UNBILLED)    │         │      (/expenses)        │
                 └────────────┬────────────┘         └────────────┬────────────┘
                              │                                  │
                              └─────────────────┬────────────────┘
                                                │
                                                ▼
                               ┌─────────────────────────────────┐
                               │     Invoice Draft Session       │
                               │        (/invoice/draft)         │
                               │  • Auto-aggregates unbilled     │
                               │    time and expenses            │
                               │  • Computes line item totals    │
                               │  • Renders PDF preview          │
                               └────────────────┬────────────────┘
                                                │
                                                ▼
                               ┌─────────────────────────────────┐
                               │  Finalized & Sent Invoice       │
                               │  • Time & Expenses -> BILLED    │
                               │  • Read-only audit lock         │
                               └─────────────────────────────────┘
```

---

## 6. Verification & Automated Testing Plan

To ensure no regression occurs across these interconnected hubs, the automation suite must execute **Cross-Screen Propagation Verification**:

1. **Court Updates $\rightarrow$ To-Do Propagation:**
   - Create To-Do via `#addTodoBtn` inside Court Updates.
   - Verify presence in `/court-updates` `.cu-todo-popover`.
   - Verify presence in `/todos`.
   - Verify presence in `/matters/{id}` Todo tab.
   - Edit To-Do in `/todos` $\rightarrow$ verify updated in Court Updates.
   - Complete To-Do in `/court-updates` $\rightarrow$ verify completed in `/todos`.
2. **Court Updates $\rightarrow$ Calendar Event Propagation:**
   - Create Extra Event via `#addCourtDateRowBtn` inside Court Updates.
   - Verify event appears on `/calendar`.
   - Verify event appears on `/court-updates` Calendar Entries sidebar on the scheduled date.
   - Verify event appears in `/court/billing`.
3. **Court Updates $\rightarrow$ Court Time $\rightarrow$ Time Entries $\rightarrow$ Invoice Draft:**
   - Submit Court Update.
   - Open `/court/billing` $\rightarrow$ specify hours and rate $\rightarrow$ Save.
   - Open `/time` $\rightarrow$ assert unbilled time entry exists with exact hours and rate.
   - Open `/invoice/draft` $\rightarrow$ create draft $\rightarrow$ assert line item is present and calculated accurately.
   - Send invoice $\rightarrow$ assert time entry transitions to Billed.
