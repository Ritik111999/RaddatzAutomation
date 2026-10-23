# Dependency graph

| Parent | Dependent | Verified relationship | State |
| --- | --- | --- | --- |
| Company | Property | Grid and breadcrumb show a property beneath its company | OBSERVED |
| Property | Matter | Grid and breadcrumb show a matter beneath a property | OBSERVED |
| Matter | Tenant(s) | Matter General renders numbered tenant section and add-another control | OBSERVED |
| Matter | Task | Matter task list and task links show scoped work | OBSERVED |
| Matter | Event | Calendar event exposes a Matter link | OBSERVED |
| Event | Court Update | Court Updates lists calendar entries and loads a matter placeholder | OBSERVED |
| Template | Generated document | UI advertises generation after Word upload/conversion | DOCUMENTED by UI text; unexecuted |
| Template | Billing entries | UI advertises automatic time/expense creation | DOCUMENTED by UI text; unexecuted |
