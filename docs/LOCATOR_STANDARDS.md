# Locator Standards

The POM uses **semantic, user-visible locators** validated against the live Raddatz PWA on 2026-10-22.

## Required priority order

1. `getByRole` with an exact accessible name for buttons, links, headings, tables, columns and groups.
2. `getByLabel` for editable form controls.
3. `getByPlaceholder` when the UI does not supply an accessible label.
4. A narrowly scoped CSS locator only when the application exposes no stable accessible contract; document the reason beside it.

Avoid XPath, positional selectors (`nth()`), generated IDs, icon glyphs, CSS class names, and text derived from test data.  Do not use dynamic counts, dates, tenant names, staff names, case numbers or row order as a primary locator.

## Current validated contracts

- Login: `Email address`, `Password`, `Sign In`
- Dashboard: a heading matching `Welcome back, .+!`, `My Tasks`, navigation links
- Client list: `Companies & Matters`, client search placeholder, and the eight-column table schema.  Sort headers intentionally are not matched by exact text because their accessible names include dynamic sort glyphs.
- To-dos: `All Tasks`, `Search tasks`, Task column header
- Calendar: `Calendar`, the icon-prefixed Week button (pattern match), `Search events, cases, people...`, `Calendar date picker`

Each navigation action waits for its route before constructing the next Page Object.  Read-only page objects intentionally omit create, update, delete, complete, send, upload and generate operations.
