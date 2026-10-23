# Coverage matrix

| Area | Navigation mapped | Fields mapped | Data lineage mapped | Mutation tested | Status |
| --- | --- | --- | --- | --- | --- |
| Dashboard | Yes | Partial | Partial | No | OBSERVED |
| Companies & Matters | Yes | Yes | Yes | Historical only | OBSERVED |
| Matter workspace | Yes | Yes | Yes | Historical only | OBSERVED |
| To-dos | Yes | Yes | Yes | Historical only | OBSERVED |
| Calendar | Yes | Partial | Yes | Historical only | OBSERVED |
| Workflows | Yes | Partial | Partial | No | OBSERVED |
| Court Updates | Yes | Yes | Partial | No | OBSERVED / restricted mutation |
| Documents/templates | Yes | Yes | Partial | No | OBSERVED / deferred |
| Billing/portal/bulk/settings-risk areas | Menu-level only | No | No | No | RESTRICTED |

The table above records the initial observation pass and is retained as historical evidence. The current verified automation additions below supersede its mutation-status column (local/admin, 2026-10-23).

- Portfolio hierarchy: one owned Company, two Properties, three Matters; explicit Add Property/Add New Matter and row lineage assertions passed.
- Multiple tenants: Create Matter with two fully populated tenants passed. Adding a tenant through the existing-Matter editor remains a product defect.
- Task: create/edit/delete passed.
- Event: create/edit/delete and Court Updates appearance/removal passed; PDF `run_20261023_173120`.
- Matter Notes: create/edit/delete passed.
- Matter Documents: upload/view/download/delete passed.
- Billing Time: create/edit/delete passed.
- Billing Expense: create/edit/reopen-to-verify/delete passed; PDF `run_20261023_173027`.
- Court Update: owned creation/edit/reopen-to-verify/delete passed.
- Invoice draft: create from exact owned billable time, verify case source/hours/totals, preview availability and actual PDF download passed. Sending/payment allocation excluded; void/advanced billing transitions incomplete.
- Template: owned DOCX upload/create, full metadata and variable persistence, preview/download/edit/delete passed. Word-content editor unavailable in this environment; generated substitution is a separate unverified study.
- Search/Calendar/Settings/Reports/Workflows: five read-only scenarios passed, including seven Report tab/filter contracts and unsaved Workflow form. Workflow execution/configuration mutations are not covered.

The combined serial regression passed 16 tests with 0 failures/errors/skips in
295.8 seconds; report `run_20261023_175408/extent-report.pdf`. It uses one application
browser and one authenticated session retained only in memory. This is scoped
regression evidence, not complete PWA coverage. Direct Calendar-card propagation
assertions added afterward are under focused verification; generated-document
substitution remains excluded until its study passes. Restricted areas and actions
remain excluded as defined in `docs/context/SAFETY_BOUNDARIES.md`.
