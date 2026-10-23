# Safety boundaries

`DOCS_ONLY` is the default and permits local source/document inspection only. `PROD_OBSERVE` permits explicitly authorized observation only. `NONPROD_APPROVED` requires explicit target, approved scenario, role, allowed operations, and automation-owned data. Unknown classification fails closed.

Restricted modules are integrations, backend-control, portal access/publishing, access-control, import/export/bulk, external messaging, and unclassified high-impact areas; these are observation-only. Billing entry/workflow testing is permitted because this application has no official payment-service flow in scope. Outside restricted modules, CRUD, view, and download testing is permitted only on records created and tagged by the current automation run. Deletion is permitted only for those owned records. Browser requests, APIs, and database access may not bypass this policy. Store artifacts locally and redact them before sharing.
