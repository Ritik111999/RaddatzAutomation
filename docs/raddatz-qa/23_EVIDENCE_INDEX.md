# Evidence Index

| Evidence ID | Date | Environment | Role | Module/screen | Method | Classification | Redaction |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RAD-E-0001 | 2026-10-23 | Local PWA | admin | Login | Accessibility snapshot | OBSERVED | No credential values retained |
| RAD-E-0002 | 2026-10-23 | Local PWA | admin | Dashboard | Accessibility snapshot after authorized sign-in | OBSERVED | Task, person, case, and audit values omitted |

## RAD-E-0001

The login screen exposes email, password, remember-me, forgot-password, and sign-in controls. Authentication behavior is not a test target.

## RAD-E-0002

The authenticated shell exposes Dashboard, Clients, To-dos, Calendar, Billing, Workflows, Court Updates, Client Portal, Bulk Uploader, and Settings. Dashboard cards visibly include My Tasks, Office Tasks, a weekly calendar, and an audit-log preview. Billing, Client Portal, Bulk Uploader, and Settings require scope classification before any deeper observation; restricted areas are not entered.
