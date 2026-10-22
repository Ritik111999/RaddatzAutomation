---
name: raddatz-pwa-automation
description: Build and maintain the Raddatz PWA Java Playwright, TestNG, Page Object Model automation. Use when adding or changing Raddatz page objects, locators, tests, reports, test data, or reliability safeguards.
---

# Raddatz PWA Automation

## Operating constraints

- Treat shared Raddatz environments as read-only unless the user explicitly authorizes a specific data-changing flow.
- Never persist credentials, cookies, trace files, screenshots, or generated reports in Git. Use environment variables for secrets.
- Default live execution stays off. Require `raddatz.run.live=true` and external credentials to contact the application.
- Consult `docs/RADDATZ_PWA_SCREEN_MAP.md` before covering a new screen and update it after a verified UI change.

## Locator workflow

1. Inspect the real UI with the Playwright skill before writing a production locator.
2. Prefer, in order: semantic role plus accessible name; associated label; placeholder; a stable `data-testid` that the product team intentionally supports.
3. Scope locators to a landmark, dialog, form, or table when the same text occurs elsewhere.
4. Use a regular expression only when the accessible name includes a stable icon glyph or visual prefix.
5. Prohibit XPath, positional selectors (`nth`, `first`, `last`), generated CSS classes, runtime IDs, and text from mutable customer records.
6. Record locator exceptions and verified evidence in `docs/LOCATOR_STANDARDS.md`.

## Test design

- Keep each TestNG method independent. `PlaywrightSession` must create one browser context per test.
- Page objects expose user intent and page state; tests hold business assertions and must not contain selectors.
- Wait on Playwright locators, URL patterns, or explicit UI state. Never add a fixed sleep.
- Use read-only navigation and assertions for shared-admin coverage. Put any approved create/update/delete flow in an opt-in suite with cleanup and a unique disposable record.

## Diagnosing failures

1. Preserve the HTML/PDF Extent report and Log4j output.
2. Open `target/automation-artifacts/traces/*.zip` in Playwright Trace Viewer for failed tests.
3. Classify the failure as application regression, locator drift, environment/authentication issue, or test defect before changing selectors.
4. Correct the smallest responsible page object or test, then run the affected test and the read-only smoke suite.

## Completion checklist

- [ ] New locator follows the locator workflow and is live-verified.
- [ ] Test makes no unapproved data change.
- [ ] New assertions are user-visible and deterministic.
- [ ] Failure evidence is available and ignored by Git.
- [ ] `mvn -q test` passes with live runs disabled; run the affected live suite only when authorized.

For reliability rationale and operating policy, read [AUTOMATION_RESILIENCE_PLAYBOOK.md](../../../docs/AUTOMATION_RESILIENCE_PLAYBOOK.md).
