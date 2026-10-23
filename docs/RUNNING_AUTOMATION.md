# Running the Raddatz PWA Automation Framework

## Safety model

The included suite is deliberately **read-only**. It does not use create, update, delete, send, upload, generate, import, reset, void or billing-allocation controls. Local browser tests are disabled by default so `mvn test` is safe to run in a fresh checkout.

The execution mode defaults to `DOCS_ONLY`, which also blocks browser startup. An authorized local observation run requires `-Draddatz.execution.mode=PROD_OBSERVE`; approved non-production mutations require `-Draddatz.execution.mode=NONPROD_APPROVED` in addition to the existing explicit mutation switch.

The default target is the shared local environment: `http://192.168.1.12:8000/login`. Override it only when a different local server is explicitly supplied:

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
