# Execution approval register

| ID | Environment | Approved activity | Data boundary | Status |
| --- | --- | --- | --- | --- |
| RAD-APR-001 | `http://192.168.0.229:8000/login` | Admin local observation | No mutation | Active client authorization |
| RAD-APR-002 | Same local target | Owned Company/Property/Matter and downstream CRUD | Current-run synthetic owned records only | Active client authorization |
| RAD-APR-003 | Same local target | Owned billing entries/draft invoice | No payment, settlement, send, allocation, or void | Active client authorization |
| RAD-APR-004 | Same local target | Controlled fictional DOCX/template generation | No portal/email/timeline side effects | Active client authorization |
| RAD-APR-005 | Any | Integrations/backend/portal/permissions/import-export/external messaging | None | Restricted |

The `approvals/local-owned-scenarios.properties` manifest is enforced before any mutation browser session begins. A scenario absent from it fails closed.
