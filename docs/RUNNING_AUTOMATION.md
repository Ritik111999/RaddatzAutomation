# Running the Raddatz PWA Automation Framework

## Safety model

The default suite is deliberately **read-only**. Mutable suites require the explicit `NONPROD_APPROVED` execution mode and use only records created and tagged by the current run. These approved suites may create, edit, view, download, upload, and clean up owned records. Billing entries and invoice workflow are in scope for owned data; external payment or settlement controls are not. Local browser tests are disabled by default so `mvn test` is safe to run in a fresh checkout.

The execution mode defaults to `DOCS_ONLY`, which also blocks browser startup. An authorized local observation run requires `-Draddatz.execution.mode=PROD_OBSERVE`; approved non-production mutations require `-Draddatz.execution.mode=NONPROD_APPROVED` in addition to the existing explicit mutation switch.

The default target is the client-approved local environment: `http://192.168.0.229:8000/login`. Override it only when a different local server is explicitly supplied:

```zsh
mvn test -Draddatz.run.local=true -Draddatz.base.url=http://<host>:<port>/login
```

Keep credentials out of source control.  Set them only in the calling shell or CI secret store:

```zsh
export RADDATZ_USERNAME='your-automation-account@example.com'
export RADDATZ_PASSWORD='your-secret'
mvn test -Draddatz.run.local=true
```

The framework uses Playwright Chromium. Its download check is disabled during normal test runs; install the runtime once when setting up a new machine:

```zsh
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args='install chromium'
```

Useful optional flags:

```zsh
mvn test -Draddatz.run.local=true -Draddatz.headless=false
mvn test -Draddatz.run.local=true -Draddatz.browser=firefox
mvn test -Draddatz.run.local=true -Draddatz.report.pdf=false
```

`target/automation-artifacts/run_<timestamp>/extent-report.html` is always produced. When Chromium is available, the listener also renders `extent-report.pdf`. Failure screenshots and Log4j2 logs are stored under `target/automation-artifacts`.

## Framework layout

### Serial regression with one browser

The dedicated regression suite sets `reuseBrowser=true` and `parallel="none"`.
It keeps one Playwright browser for the suite, opens a fresh context per scenario,
and closes the browser in suite teardown. It authenticates once and holds normal
session state in memory, avoiding repeated logins and the server's login throttle.
Client-side filters/pages are isolated; server-side Billing Mode can persist and
its session fixture accepts either the password gate or already-active mode.
Session state is never written to disk and is discarded at suite teardown.

```zsh
mvn test \
  -Draddatz.suite.file=src/test/resources/suites/admin-regression-serial.xml \
  -Draddatz.run.local=true \
  -Draddatz.execution.mode=NONPROD_APPROVED \
  -Draddatz.allow.mutation=true
```

Focused suites can use `-Draddatz.reuse.browser=true` with the same behavior.
Browser-based PDF rendering happens after the application scenarios finish.
Invoice/template/search expansion suites remain separate until their live
verification passes; their presence in source is not a coverage claim.

- `config` — property/environment resolution; no secret persistence
- `core` — isolated Playwright browser context lifecycle
- `pages` — POM classes and reusable navigation components
- `reports` — ExtentReports listener plus HTML-to-PDF renderer
- `tests` — TestNG read-only smoke suite

Before adding any test that changes data, place it in a separate `mutation` or `destructive` TestNG group, use dedicated disposable fixtures, and leave it excluded from the default suite.

Admin data-creation rules and the validated local field contract are in [ADMIN_AUTOMATION_DATA_CONTRACT.md](ADMIN_AUTOMATION_DATA_CONTRACT.md). Data-changing scenarios also require `-Draddatz.allow.mutation=true`.

To run the owned Company creation slice intentionally:

```zsh
mvn test \
  -Draddatz.suite.file=src/test/resources/suites/admin-mutation.xml \
  -Draddatz.run.local=true \
  -Draddatz.allow.mutation=true
```

To create an isolated Company → Property scenario, run the separately guarded dependency slice:

```bash
mvn test \
  -Draddatz.suite.file=src/test/resources/suites/admin-property.xml \
  -Draddatz.run.local=true \
  -Draddatz.allow.mutation=true
```

To create an isolated Company → Property → Matter scenario, run:

```bash
mvn test \
  -Draddatz.suite.file=src/test/resources/suites/admin-matter.xml \
  -Draddatz.run.local=true \
  -Draddatz.allow.mutation=true
```

To create an isolated Company → Property → Matter → Task → Event workflow, run:

```bash
mvn test \
  -Draddatz.suite.file=src/test/resources/suites/admin-task-event.xml \
  -Draddatz.run.local=true \
  -Draddatz.allow.mutation=true
```
