# Defect and environment register

| ID | Classification | Description | Status | Evidence |
| --- | --- | --- | --- | --- |
| RAD-DEF-001 | Product defect / partial coverage | Adding a second tenant in the existing-Matter editor did not persist after reload; Create Matter with two tenants does persist. | Open; do not automate as passing edit coverage | Session handoff, 2026-10-23 |
| RAD-ENV-001 | Environment limitation | Inline OnlyOffice Word editor is unavailable/read-only locally. | Blocked | RAD-E-0014 |
| RAD-AUTO-001 | Automation repair | Calendar picker uses visible searchable inputs and view switcher uses ARIA tabs. | Resolved | run_20261026_123638 |
| RAD-AUTO-002 | Automation repair | Expense reopened-edit modal requires normal Escape dismissal before owned delete. | Resolved | run_20261026_123638 |
