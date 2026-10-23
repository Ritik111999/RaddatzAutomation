# Safety boundaries

`DOCS_ONLY` is the default and permits local source/document inspection only. `PROD_OBSERVE` permits explicitly authorized observation only. `NONPROD_APPROVED` requires explicit target, approved scenario, role, allowed operations, and automation-owned data. Unknown classification fails closed.

Never automate or enter restricted integration, backend-control, portal, access-control, import/export/bulk, billing/finalization, external messaging, or unclassified high-impact areas. Browser requests, APIs, and database access may not bypass this policy. Store artifacts locally and redact them before sharing.
