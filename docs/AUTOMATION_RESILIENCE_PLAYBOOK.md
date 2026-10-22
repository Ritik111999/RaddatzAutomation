# Raddatz Automation Resilience Playbook

## Purpose and scope

This is the operating standard for the Raddatz PWA test suite. It keeps shared-admin testing safe, makes UI changes diagnosable, and reduces false failures without hiding genuine product regressions. It applies to the Java Playwright, TestNG, POM, Log4j, and ExtentReports implementation in this repository.

## Research-backed decisions

| Decision | Applied standard | Why |
| --- | --- | --- |
| Locator strategy | Use role/name, label, placeholder, or intentional test IDs; scope duplicate content. | User-facing locators align with how users perceive the UI and are more resilient to implementation changes. |
| Test isolation | One fresh browser context per TestNG method. | Cookies, storage, and test state cannot leak between tests. |
| Failure evidence | Retain a Playwright trace only for a failed test, plus application logs and Extent HTML/PDF output. | The action timeline, DOM snapshot, screenshot, and source references make a failure reproducible. |
| Retry policy | Do not retry by default. Classify the failure first; enable a narrowly documented CI retry only for a proven transient infrastructure condition. | Blind retries hide product defects and create false confidence. |
| Live safety | Shared-admin tests remain navigation/assertion-only. Mutation tests require explicit opt-in, uniquely named disposable data, and cleanup approval. | Prevents accidental customer-data changes. |

## Project skills decision

The official Playwright skill is the browser-inspection and live-locator tool for this project. The official `security-best-practices` skill is installed in the local Codex skill library for secure test-data and secret-handling review.

The curated `playwright-interactive` skill was intentionally not installed: its tracked GitHub issue reports a removed runtime dependency. No unreviewed third-party “autonomous testing” skill was added, because it would introduce unverified execution behavior and does not match this Java/TestNG framework.

The repository-local skill, `.cursor/skills/raddatz-pwa-automation`, captures the project-specific rules so they travel with the repository.

## Delivery workflow

1. Map the target screen and user intent in `docs/RADDATZ_PWA_SCREEN_MAP.md`.
2. Use the Playwright skill to inspect a real, non-destructive UI state.
3. Implement the smallest page-object method with a semantic locator.
4. Add an independent TestNG test that makes only the approved actions.
5. Run the focused suite. On a failure, inspect the generated trace before changing code.
6. Run `mvn -q test` to ensure the safe default suite still builds and reports correctly.
7. Add a brief entry to `docs/LOCATOR_STANDARDS.md` if the UI required a non-obvious locator rule.

## Locator contract

Each production locator must satisfy all of the following:

- Describes a stable user-visible contract, not implementation structure.
- Is unique inside its page or explicitly scoped to a semantic container.
- Does not depend on a record name, timestamp, sort order, animation, generated ID, CSS hash, or position.
- Is verified against the current application snapshot before it is added.
- Produces a clear failure when its expected user-visible contract disappears.

### Preferred patterns

```java
page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
page.getByLabel("Email address");
page.getByPlaceholder("Search tasks");
page.getByTestId("matter-status"); // only when deliberately provided by the product
```

### Rejected patterns

```java
page.locator("//div[4]/button[2]");
page.locator(".css-1x7z9q");
page.getByText("Acme Client").nth(0);
page.locator("[id='react-aria123']");
```

## Failure evidence and triage

On every failed local test, `ReadOnlyTestBase` writes a trace to:

```text
target/automation-artifacts/traces/<TestClass>-<testMethod>.zip
```

Open it with `npx playwright show-trace <trace.zip>` or the Trace Viewer. Review the last action and classify the failure before editing:

1. **Application regression:** the expected user-facing behavior changed.
2. **Locator drift:** the UI behavior remains, but its accessible contract changed.
3. **Environment/authentication:** login, deployment, network, permissions, or test account issue.
4. **Test defect:** an unsupported assumption, race, or prohibited shared state exists in the test.

Do not “fix” a failure by adding sleeps, positional selectors, or an unconditional retry.

## Change gates

| Change type | Required evidence before merge |
| --- | --- |
| New read-only screen coverage | Current UI locator validation, focused test pass, safe-default Maven pass. |
| Locator replacement | Failure trace or current snapshot showing the old contract is invalid, plus live verification of the new contract. |
| Mutation workflow | Written authorization, isolated test data plan, cleanup decision, and dedicated opt-in suite. |
| Framework/dependency update | Compile, safe-default suite, and one live smoke run when credentials/environment are available. |

## Primary references

- [Playwright best practices](https://playwright.dev/docs/best-practices)
- [Playwright locators](https://playwright.dev/docs/locators)
- [Playwright browser contexts and isolation](https://playwright.dev/docs/browser-contexts)
- [Playwright Trace Viewer](https://playwright.dev/docs/trace-viewer-intro)
- [Playwright retries](https://playwright.dev/docs/test-retries)
- [Known `playwright-interactive` dependency issue](https://github.com/openai/skills/issues/386)
