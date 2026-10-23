# Evidence Index

| Evidence ID | Date | Environment | Role | Module/screen | Method | Classification | Redaction |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RAD-E-0001 | 2026-10-23 | Local PWA | admin | Login | Accessibility snapshot | OBSERVED | No credential values retained |
| RAD-E-0002 | 2026-10-23 | Local PWA | admin | Dashboard | Accessibility snapshot after authorized sign-in | OBSERVED | Task, person, case, and audit values omitted |

## RAD-E-0001

The login screen exposes email, password, remember-me, forgot-password, and sign-in controls. Authentication behavior is not a test target.

## RAD-E-0002

The authenticated shell exposes Dashboard, Clients, To-dos, Calendar, Billing, Workflows, Court Updates, Client Portal, Bulk Uploader, and Settings. Dashboard cards visibly include My Tasks, Office Tasks, a weekly calendar, and an audit-log preview. Billing, Client Portal, Bulk Uploader, and Settings require scope classification before any deeper observation; restricted areas are not entered.

## RAD-E-0003 — Workflows

The read-only list exposes workflow name, trigger, action count, enablement state, edit, and delete controls. `OBSERVED`; confidence high. Values redacted.

## RAD-E-0004 — Court Updates

The intake view is calendar-entry-led and exposes matter, notes, case status, optional to-do/event actions, and a billing handoff. `OBSERVED`; confidence high. Mutation is excluded pending billing-impact review.

## RAD-E-0005 — To-dos

The list exposes search, sort, date/type filters, matter link, due date, assignee, task type, priority, edit, complete, and delete controls. `OBSERVED`; confidence high. Values redacted.

## RAD-E-0006 — Calendar

The calendar exposes month/week/day/agenda views, search, category, people, courtroom filters, and event links to matter and docket. `OBSERVED`; confidence high. Values redacted.

## RAD-E-0007 — Companies & Matters

The index exposes Company, Property, Matter, Unit, Case Type, Court Date, Case Number, and Status columns. `OBSERVED`; confidence high. Values redacted.

## RAD-E-0008 — Matter General

An automation-owned matter showed breadcrumb lineage, a tenant collection, case details, key dates, upcoming tasks, summary, and quick actions for time, expense, documents, events, and invoice drafting. `OBSERVED`; confidence high. Values redacted.

## RAD-E-0009 — Matter sub-workspaces

Notes expose a rich-text toolbar and note list. Documents expose file intake and a template route. To-dos expose matter-scoped filters and task actions. Timeline exposes type filters and chronological activity. `OBSERVED`; confidence high. Values redacted.

## RAD-E-0010 — Settings menu

The visible menu exposes staff, reports, custom fields, email templates, permissions, backup/export, masters, theme, and document-template creation. All except document-template creation are classified restricted or unknown. `OBSERVED`; confidence high.

## RAD-E-0011 — Create Document Template

The unsaved form exposes metadata, category, active status, optional flat fee, automatic time/expense entries, custom variables, Word `.docx` upload, conversion/editor messaging, cancel, and save. No file was selected or saved. `OBSERVED`; confidence high.

## RAD-E-0012 — Document Template registry

The read-only registry exposes search, category and status filters; columns for category, flat fee, required field count, status, last use, and created date; and view/download/edit/delete actions. No template was opened, downloaded, changed, or deleted. `OBSERVED`; confidence high.

## RAD-E-0013 — Company and Property detail

Automation-owned company and property detail views expose overview, properties/cases, notes, documents, contact, and billing-related tabs or panels. Company-to-property and property-to-company links are reciprocal; property exposes active/inactive case grouping. Financial, portal-access, spreadsheet, email, and document-upload controls were not used. `OBSERVED`; confidence high. All values redacted.
