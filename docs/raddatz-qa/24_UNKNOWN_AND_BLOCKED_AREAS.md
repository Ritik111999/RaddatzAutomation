# Restricted, Unknown, and Blocked Areas

| Area | Status | Reason | Allowed evidence method |
| --- | --- | --- | --- |
| Integrations | RESTRICTED | Connection, token, and external-service risk | Existing approved documentation only |
| Backend Data Controller | RESTRICTED | Potential production-data impact | Existing approved documentation only |
| Portal Access / Client Portal publishing | RESTRICTED | External sharing and access risk | Existing approved documentation only |
| Roles, permissions, authentication | RESTRICTED | Access-control risk | Existing approved documentation only |
| Imports, exports, bulk operations | RESTRICTED | Broad data movement/modification risk | Existing approved documentation only |
| Billing entry and invoice workflow | PERMITTED OWNED-DATA TESTING | Client confirmed this is application-entry workflow, not an official payment service | Create/read/update/delete only automation-owned entries; do not use external settlement controls |
| Email/SMS and external communication | RESTRICTED | Third-party communication risk | Existing approved documentation only |
| Document editor autosave behavior | UNKNOWN | Opening may persist a lock/history/update | Inspect only after a safe observation method is confirmed |
| Company/property portal-access controls, email, and spreadsheet | RESTRICTED / UNKNOWN | May grant access, communicate externally, or expose/persist shared data | Do not interact; document only at affordance level |
| Existing-Matter tenant editor | PRODUCT DEFECT / PARTIAL COVERAGE | A second tenant entered through the existing-Matter control did not persist after Save Changes and reload on 2026-10-23. The Create Matter flow does persist both tenants to the Companies & Matters list. | Create both owned tenants through the supported Create Matter flow; retain the failure trace for defect triage and do not claim edit coverage until the workspace persistence/display issue is repaired. |

Observed 2026-10-23 on an owned document template: its registry displays `0 fields` while the editor retains the custom variable label, merge token, type, and hint. Treat this as a registry display discrepancy; use editor values to verify persistence. Full generated-document variable substitution remains unverified. The generation form also shows checked/disabled billing controls; the actual effect must be bounded against template billing configuration and the owned Matter before claiming billing/substitution coverage.
