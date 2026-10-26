# Cross-module data test matrix

| Source | Consumer | Current evidence | Test status | Next safe condition |
| --- | --- | --- | --- | --- |
| Company | Property selector; Companies & Matters | Owned parent ID asserted | PASS | Add parent-switch reset observation |
| Property | Matter selector; grid; breadcrumb | Owned parent ID and grid lineage asserted | PASS | Add stale-selection negative after Company change |
| Matter/Tenant | Task derived Client | Derived Client asserted | PASS | Timeline expectation requires clarification |
| Matter | Event derived Property | Auto-populated Property asserted | PASS | Notification paths excluded |
| Event | Calendar and Court Updates | Date-scoped Calendar card and Court Update lifecycle | PASS | Court billing handoff excluded |
| Time/Expense | Draft Invoice | Owned time source and total asserted | PASS for time; expense inclusion is DESIGN | Need controlled date-range rule |
| Template variable | Generated DOCX XML | Custom token substitution asserted | PASS for one custom variable | Other variable sources UNKNOWN |
