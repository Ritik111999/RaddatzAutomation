# Raddatz automation

Java 21, Playwright for Java, TestNG, Maven, POM, Log4j2, and ExtentReports.

- Treat all law-firm records and artifacts as confidential; redact personal, legal, financial, and document content.
- Default execution mode is `DOCS_ONLY`. Do not launch a browser unless an explicit environment mode and approved target are supplied.
- `PROD_OBSERVE` is read-only; `NONPROD_APPROVED` requires an approved scenario manifest and owned test data.
- Never interact with integrations, backend data control, portal access/publishing, permissions, authentication, imports/exports/bulk operations, external messaging, or unclassified high-risk controls. Billing entries and invoice workflow are permitted only for automation-owned records; never use external payment or settlement controls.
- Client-confirmed requirements outrank documented, observed, code-inferred, and hypothetical facts. Tag application facts with source/date/reliability.
- Do not mutate records unless specifically authorized, environment-approved, and provably automation-owned. Delete only records created and positively identified as owned by the current automation run; never alter or delete user-owned records.
- Inspect existing implementation before editing. Preserve working POMs, suites, reporting, and unrelated user changes.
- Use focused POMs, stable role/label/test-id locators, Playwright assertions, and no hard waits or force-click workarounds.
- Keep tests sequential unless per-thread Playwright/context/report ownership is proven.
- Never claim an execution passed unless it was run.
- Load only the relevant files from [docs/context/CONTEXT_INDEX.md](docs/context/CONTEXT_INDEX.md). Update [docs/context/SESSION_HANDOFF.md](docs/context/SESSION_HANDOFF.md) at the end of work.
- Safety authority: [docs/context/SAFETY_BOUNDARIES.md](docs/context/SAFETY_BOUNDARIES.md).
