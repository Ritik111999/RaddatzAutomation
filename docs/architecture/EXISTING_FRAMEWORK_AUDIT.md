# Existing framework audit — 2026-10-23

## Found and preserved

- Maven root: `raddatz-pwa-automation`; Java 21.
- Pinned versions: Playwright 1.63.0, TestNG 7.11.0, ExtentReports 5.1.2, Log4j 2.25.2, Surefire 3.5.4.
- POMs, `PlaywrightSession`, `AutomationConfig`, reports/listener, sequential default suite, isolated mutation suites, failure traces, and ignored local artifacts.
- Credentials are configuration inputs rather than tracked values.

## Gaps and incremental plan

Current local/mutation switches do not model explicit `DOCS_ONLY`, `PROD_OBSERVE`, and `NONPROD_APPROVED` modes. Route/action deny policy, scenario approval manifest, and confidential-artifact controls are not independently enforced. Preserve all working code; add fail-closed execution policy, project context/skills, and structural checks before any suite expansion.

Method: read-only inspection of Git state, POM, sources, suites, docs, config, listener, reporting, and ignore rules. No full suite or browser test was run.
