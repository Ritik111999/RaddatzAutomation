# Executive report — forensic PWA exploration

## Outcome

The local Raddatz PWA has a confirmed central operational spine: **Company → Property → Matter → Tenant(s)**. The matter is the central workspace for case details, tasks, notes, documents, timeline, events, time/expense entry points, and invoice drafting links.

## High-confidence findings

- Company, property, and matter are visibly linked in the main index and matter breadcrumb.
- Tasks propagate across the matter workspace, all-task list, dashboard, and timeline.
- Calendar events can link back to a matter and are exposed to Court Updates.
- Court Updates include status, notes, optional task/event linkage, and an explicit billing handoff.
- Document templates support custom variables, Word `.docx` source handling, optional fixed fee/time/expense setup, and a template registry.

## Controls applied

- Only the supplied local environment was explored.
- No record was deleted.
- No existing user-owned record was edited during this pass.
- No billing, portal, bulk, export/import, permissions, integration, backend-data-controller, email, or system-configuration action was performed.
- No existing document/template was opened, downloaded, edited, or generated.
- Evidence files redact names, legal identifiers, contact information, and other record details.

## Automation recommendation

Expand first through unique, automation-owned company/property/matter/task/event/note flows, with per-run ownership tokens and lineage verification. Keep Court Updates and document generation out of mutation suites until their billing and persistence side effects are verified on disposable owned fixtures.

See [00_README.md](00_README.md) for evidence rules and [24_UNKNOWN_AND_BLOCKED_AREAS.md](24_UNKNOWN_AND_BLOCKED_AREAS.md) for the exclusion register.
