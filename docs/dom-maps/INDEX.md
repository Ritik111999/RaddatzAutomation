# Raddatz PWA — DOM Map Index

Index of captured DOM snapshots from physical screen inspections.

Location: `target/automation-artifacts/dom-maps/`

| # | File | URL / Route | Description |
|---|:---|:---|:---|
| 1 | `01_court_updates_main.html` | `/court-updates` | Court Updates main screen with Calendar Entries strip, matter search, and updates table |
| 2 | `02_court_updates_todo_modal.html` | `/court-updates` | Court Updates `#courtAddTodoModal` with matter pre-fill, due date, and assignee selector |
| 3 | `03_court_updates_event_modal.html` | `/court-updates` | Court Updates Add/Edit Event interaction linking to `/calendar/add-event` |
| 4 | `04_calendar_main.html` | `/calendar` | Central Calendar month view with event categories (Court Dates, Deadlines, Meetings) |
| 5 | `06_todos_main.html` | `/todos` | Central To-Dos manager with status tabs (Active, Completed), bulk actions, and filters |
| 6 | `07_todos_create_modal.html` | `/todos` | Global Create Task modal with matter autocomplete, staff assignment, and priority |
| 7 | `08_matter_workspace_general.html`| `/matters/{id}` | Matter Workspace General tab: tenant info, multi-tenant action, key dates, shortcuts |
| 8 | `09_matter_workspace_todos.html` | `/matters/{id}` | Matter Workspace Todo tab: matter-scoped tasks list and task creation button |
| 9 | `10_matter_workspace_notes.html` | `/matters/{id}/notes` | Matter Notes tab: legal notes log and add note form |
| 10| `11_matter_workspace_documents.html`| `/matters/{id}/documents` | Matter Documents tab: uploaded files, category tags, and template generator trigger |
| 11| `12_court_time_billing.html` | `/court/billing` | Court Time Billing: court appearances, billable hours entry, hourly rates |
| 12| `13_time_entries.html` | `/time` | Central Time Entries ledger: matter autocomplete, billable status, monthly totals |
| 13| `14_expenses.html` | `/expenses` | Central Expenses ledger: matter autocomplete, expense codes, receipts, amounts |
| 14| `15_invoice_drafts.html` | `/invoice/draft` | Invoice Drafts workspace: client selection, aggregation of unbilled items, PDF export |

---

## Key Selectors Extracted from Inspection

- **Court Updates Add To-Do Button:** `#addTodoBtn` (calls `openAddTodoPage()`)
- **Court Updates To-Do Modal:** `#courtAddTodoModal`
  - Matter Pre-fill: `#courtTodoMatterId`, `#courtTodoMatterDisplay`
  - Due Date Input: `#courtTodoDueDate`
  - Assignee Type Buttons: `.court-todo-type-btn` (`office`, `staff`, `client`)
  - Submit Button: `#courtTodoSubmitBtn`
  - Linked Queue: `#linkedTodoSection`, `#pendingTodosList`
  - Serialized Form Inputs: `#pendingTodosJson`, `#existingTodosJson`, `#removedTodoIdsJson`
- **Court Updates Linked To-Do Popover:**
  - Table Icon: `.cu-todo-icon` (class `.has-todo` when linked)
  - Popover Container: `.cu-todo-popover`
  - Fields: `.pop-head strong`, `.pop-label`, `.pop-val`
- **Court Updates Add Event Button:** `#addCourtDateRowBtn` (calls `openCourtUpdatesEventForm()`)
  - Target URL: `/calendar/add-event?from=court-updates&return_to=court-updates&matter_id={id}`
- **Matter Workspace Shortcuts:**
  - Add Task: `button:has-text('Add Task')`
  - Create Client To Do: `a:has-text('Create Client To Do')`
  - Add Time Entry: `a:has-text('Add Time Entry')`
  - Log Expense: `button:has-text('Log Expense')`
  - Add/Manage Key Dates: `button:has-text('Add/Manage Key Dates')`
  - Add Another Tenant: `button:has-text('Add Another Tenant')`
