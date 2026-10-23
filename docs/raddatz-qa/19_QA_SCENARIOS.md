# QA scenarios

## Safe automation candidates

1. **Owned hierarchy visibility** — create an owned company, property, matter, and tenant; verify the exact lineage appears in Companies & Matters and the matter breadcrumb.
2. **Multi-tenant matter** — create additional tenant only on an owned matter; verify numbered tenant sections and exact display locations.
3. **Task propagation** — create an owned task linked to an owned matter; verify matter To-do, all-task list, dashboard, and timeline appearances.
4. **Event propagation** — create an owned event linked to an owned matter; verify calendar appearance and matter link.
5. **Note rich text** — create/edit one owned note; verify rendering and timeline impact only if a documented expected behavior exists.
6. **Search/filter non-mutation** — verify task/calendar/template filters preserve list semantics.

## Deferred scenarios

- Court Update submission: deferred due to billing handoff.
- Document template generation/editor save: deferred until owned fixture, save behavior, and billing isolation are mapped.
- Billing, portal, export, permissions, custom fields, masters, theme, email templates, and staff: excluded.

Every mutation scenario must generate a new run token and verify ownership before edit. No scenario may delete a record.
