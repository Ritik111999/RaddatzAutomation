# QA scenarios

## Permitted automation candidates

1. **Owned hierarchy visibility** — create an owned company, property, matter, and tenant; verify the exact lineage appears in Companies & Matters and the matter breadcrumb.
2. **Multi-tenant matter** — create two fully populated, owned tenants through the Create Matter flow; verify both names in the exact Companies & Matters row. The existing-Matter tenant editor is tracked separately as a product defect.
3. **Task propagation** — create an owned task linked to an owned matter; verify matter To-do, all-task list, dashboard, and timeline appearances.
4. **Event propagation** — create an owned event linked to an owned matter; verify calendar appearance and matter link.
5. **Note rich text** — create/edit one owned note; verify rendering and timeline impact only if a documented expected behavior exists.
6. **Search/filter non-mutation** — verify task/calendar/template filters preserve list semantics.

7. **Owned billing lifecycle** — create, edit, view, download, and delete only owned billing entries/invoices; verify the documented application state without external payment processing.
8. **Owned document lifecycle** — upload/edit/view/download/delete a disposable fixture on an owned matter; verify file and template relationships.
9. **Owned template lifecycle** — create/edit/view/download/delete an owned template with disposable variables and validate its registry appearance.
10. **Owned Court Update** — create/edit/view/delete an owned Court Update linked to an owned event and matter; verify its task/event linkage.

## Still restricted

- Portal publishing/access, integrations, backend control, permissions/authentication, imports/exports/bulk, and external email/SMS remain observation-only.

Every mutation scenario must generate a new run token and verify ownership before edit or delete.
