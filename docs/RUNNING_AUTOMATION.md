# Running the Raddatz PWA Automation Framework

## Safety model

The included suite is deliberately **read-only**.  It does not use create, update, delete, send, upload, generate, import, reset, void or billing-allocation controls.  Live browser tests are disabled by default so `mvn test` is safe to run in a fresh checkout.

Keep credentials out of source control.  Set them only in the calling shell or CI secret store:

```zsh
export RADDATZ_USERNAME='your-automation-account@example.com'
export RADDATZ_PASSWORD='your-secret'
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args='install chromium'
mvn test -Draddatz.run.live=true
```

Useful optional flags:

```zsh
mvn test -Draddatz.run.live=true -Draddatz.headless=false
mvn test -Draddatz.run.live=true -Draddatz.browser=firefox
mvn test -Draddatz.run.live=true -Draddatz.report.pdf=false
```

`target/automation-artifacts/run_<timestamp>/extent-report.html` is always produced.  When Chromium is available, the listener also renders `extent-report.pdf`.  Failure screenshots and Log4j2 logs are stored under `target/automation-artifacts`.

## Framework layout

- `config` — property/environment resolution; no secret persistence
- `core` — isolated Playwright browser context lifecycle
- `pages` — POM classes and reusable navigation components
- `reports` — ExtentReports listener plus HTML-to-PDF renderer
- `tests` — TestNG read-only smoke suite

Before adding any test that changes data, place it in a separate `mutation` or `destructive` TestNG group, use dedicated disposable fixtures, and leave it excluded from the default suite.
