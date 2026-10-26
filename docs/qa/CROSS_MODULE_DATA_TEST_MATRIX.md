# Cross-module data test matrix

| Source | Consumer | Current evidence | Test status | Next safe condition |
| --- | --- | --- | --- | --- |
| Company | Property selector; Companies & Matters | Owned parent ID asserted | PASS | Add parent-switch reset observation |
| Property | Matter selector; grid; breadcrumb | Owned parent ID and grid lineage asserted | PASS | Add stale-selection negative after Company change |
| Matter/Tenant | Task derived Client | Derived Client asserted | PASS | Timeline expectation requires clarification |
| Matter | Event derived Property | Auto-populated Property asserted | PASS | Notification paths excluded |
| Event | Calendar and Court Updates | Date-scoped Calendar card and Court Update lifecycle | PASS | Court billing handoff excluded |
| Time | Draft Invoice | Revised owned bill note asserted after server reload and in draft source row | PASS | `run_20261026_134420`; no send/payment controls used |
| Expense | Draft Invoice | Expense CRUD/reopen persistence only | DESIGN | Exact date-range inclusion rule remains unconfirmed |
| Template variable | Generated DOCX XML | Custom token substitution asserted | PASS for one custom variable | Other variable sources UNKNOWN |
