# Restricted, Unknown, and Blocked Areas

| Area | Status | Reason | Allowed evidence method |
| --- | --- | --- | --- |
| Integrations | RESTRICTED | Connection, token, and external-service risk | Existing approved documentation only |
| Backend Data Controller | RESTRICTED | Potential production-data impact | Existing approved documentation only |
| Portal Access / Client Portal publishing | RESTRICTED | External sharing and access risk | Existing approved documentation only |
| Roles, permissions, authentication | RESTRICTED | Access-control risk | Existing approved documentation only |
| Imports, exports, bulk operations | RESTRICTED | Broad data movement/modification risk | Existing approved documentation only |
| Billing and invoice finalization | RESTRICTED | Financial posting risk | Existing approved documentation only |
| Email/SMS and external communication | RESTRICTED | Third-party communication risk | Existing approved documentation only |
| Document editor autosave behavior | UNKNOWN | Opening may persist a lock/history/update | Inspect only after a safe observation method is confirmed |
| Company/property portal-access controls, email, and spreadsheet | RESTRICTED / UNKNOWN | May grant access, communicate externally, or expose/persist shared data | Do not interact; document only at affordance level |
