# Session handoff

**Objective:** implement the senior QA automation intelligence framework locally.

**Completed:** repository audit inputs collected; `docs/raddatz-qa/` forensic knowledge base exists; context and safety foundation added. The approved local admin suite now validates one automation-owned Company with two Properties and three Matters, including two Matters under the same Property. It creates child records only through the explicit **Add Property** and **Add New Matter** routes and verifies each Matter row's Company, Property, Tenant, and case lineage. Task and Event creation reuse the initial owned Matter instead of creating another Company. The full expanded local suite passed on 2026-10-23 (1 test, 0 failures, 0 errors).

**Evidence:** RAD-E-0001 through RAD-E-0013 in `docs/raddatz-qa/23_EVIDENCE_INDEX.md`.

**Constraints:** `DOCS_ONLY` by default; no runtime action without explicit approved mode; restricted areas excluded. Mutations, including cleanup deletion, are limited to records positively identified as owned by the current automation run. Billing entry and invoice workflow are permitted for owned records only; external payment/settlement controls remain excluded.

**Next:** expand owned-record lifecycle coverage from the verified portfolio fixture (tenants, tasks, events, notes, documents, billing entries) while retaining per-run ownership checks and restricted-area exclusions.

**Latest verified addition:** `admin-multi-tenant.xml` passed on 2026-10-23 (1 test, 0 failures, 0 errors). It creates a second fully populated tenant through the Create Matter flow and confirms both tenants on the owned Matter row. The existing-Matter tenant editor does not persist a newly added tenant after Save Changes/reload; this remains a documented product defect, not passing edit coverage.

**Latest verified billing repair:** `admin-time-entry.xml` passed on 2026-10-23 (1 test, 0 failures, 0 errors). Its create/edit/delete lifecycle now identifies the owned entry through the exact full description retained in the application's title tooltip; visual table text is intentionally truncated. No payment or settlement control is used.

**Latest verified workflow additions:** `admin-court-update.xml` and `admin-task-lifecycle.xml` each passed on 2026-10-23 (1 test, 0 failures, 0 errors). The Court scenario creates a fresh Company → Property → Matter → Event chain, selects that Event by its unique hearing title, and persists an owned Court Update without using the billing handoff. The Task scenario creates, edits, verifies, and deletes only its own Task. Matter pickers now search by the unique case number rather than a human tenant name, preventing a recurring realistic name from selecting another Matter. The Task editor persists changes in place and leaves its modal open; the POM closes it before attempting the owned-row delete.

**Cleanup:** the sole Task left by the first failed Task-lifecycle run was explicitly searched by its exact automation-owned title, deleted, and its zero-result list state was verified. No pre-existing Task was changed.

**Fresh regression evidence:** `admin-matter-notes.xml` passed on 2026-10-23 (1 test, 0 failures, 0 errors): owned Matter Note create/edit/delete. `admin-matter-documents.xml` passed on 2026-10-23 (1 test, 0 failures, 0 errors): owned document upload/view/download/delete. A first Documents invocation received `ERR_CONNECTION_REFUSED` before login and performed no PWA action; a read-only endpoint check returned HTTP 200, and the immediate retry passed.

**Parallel expansion, 2026-10-23:** three independent discovery tasks mapped Billing expense/draft-invoice lineage, template variables/Word upload prerequisites, and search/filter/workflow affordances. No existing records were changed by discovery. Invoice drafts include outstanding time/expenses in the selected date range; Send and Allocate Payment remain excluded. Template variables support Single-line/Block text, Date, Number, and Dropdown; substitution/persistence needs an owned fixture. Workflow enablement may affect other records and requires inspection of trigger/action scope before mutation.

**Latest verified Event/Expense additions:** `admin-event-lifecycle.xml` passed (1 test, 0 failures/errors/skips), report `target/automation-artifacts/run_20261023_173120/extent-report.pdf`. It creates an Event on a fresh Matter, verifies Court Updates propagation, edits title/notes, identity-checks the saved title, deletes that Event, and verifies its absence on the Court Updates date. Deletion returns to Dashboard. `admin-expense.xml` passed (1 test, 0 failures/errors/skips), report `target/automation-artifacts/run_20261023_173027/extent-report.pdf`. It creates a $75 expense, edits it, reopens its editor after reload to verify the exact persisted description, then deletes it. Both use only owned data.

**Residual data:** failed runs can leave newly created owned parent records/events/expenses; no broad cleanup is authorized. Exact residual owned expense from the first saved run: case `DC-LT-2026-684333`; further residuals must be resolved from their run evidence before cleanup. An Event failure after deletion reflected only an incorrect redirect assertion; its Event was removed successfully.

**Expansion in progress:** invoice-draft and template metadata suites added by parallel implementation; not yet live-verified. `-Draddatz.reuse.browser=true` now supports one browser across a serial suite, with fresh scenario contexts and final suite teardown. Shared browser ownership rejects cross-thread access. Invoice focused run is verifying this mode.

**Combined single-browser regression verified:** `admin-regression-serial.xml` passed on 2026-10-23: 9 tests, 0 failures/errors/skips, 237.5 seconds. One shared application browser with fresh contexts; Company portfolio hierarchy, multi-tenant creation, Task, Event, Notes, Documents, Time Entry, Expense, and Court Update creation. Report: `target/automation-artifacts/run_20261023_173526/extent-report.pdf`. This is a passing scoped regression, not a full-PWA coverage claim.

**Expansion validation running:** `admin-expansion-validation.xml` combines invoice draft/source/totals/PDF download, template import/variable persistence/metadata update/view/download/delete, and 5 read-only scenarios for search/reset, Calendar views, Workflows, Settings, and Report tabs. All run serially in one browser. Earlier invoice assertion was corrected because creation navigates directly to Invoice Details; full Company/Property grid matching was the wrong oracle. Earlier template grid-description failure was corrected through exact editor-field values. The registry shows 0 fields despite persisted variables; this display discrepancy is not evidence that variables were lost.

**Latest expansion evidence:** batch `run_20261023_174545` executed 7 tests: 6 passed, 1 failed, no skips. Owned invoice/PDF and full template metadata/variable/view/download/edit/delete lifecycle passed, as did Reports, search/reset, Calendar, and Settings checks. Sole Workflow failure expected its Status header to be sortable; actual Status header is not sortable. POM corrected; readonly suite rerun in progress. Invoice focused run also passed in `run_20261023_174129`.

**Environment limitation:** Word template screen explicitly reports `Read-only Word layout — read-only preview (OnlyOffice unavailable)`. Inline Word editing cannot currently be verified in this environment. Controlled DOCX re-upload/variable definition work; actual generated-document substitution is still unverified. Template 23 has zero configured Time/Expense entries and blank flat fee, despite generic disabled checked billing controls on the generation form. Do not assume those controls create billing entries without measuring owned-data behavior.

**Further implementation awaiting runtime:** Court Update test expanded to create/edit/reopen-verify/delete from exact run notes. It compiles; dedicated live rerun pending. Company/Property/Matter edit routes were mapped on verified owned records; new edit lifecycle implementation remains pending. Parallel hierarchy implementation task ended at a workspace spend cap before writing new files; root work continues on available tools.

**Readonly suite verified:** `admin-search-filter.xml` passed 5 tests, 0 failures/errors/skips in 23.66 seconds; report `run_20261023_175134/extent-report.pdf`. Includes all report-tab/filter observation, Workflow list and unsaved form, Settings navigation, Calendar views, and search/reset. Repeated fresh logins previously hit the local server's explicit “Too many attempts” response. Serial suites now authenticate once, retain state only in memory, and restore it into fresh contexts; Billing setup accepts both the normal gate and already-active mode. No rate-limit bypass or blanket retry was added. Setup failures save their own trace. The master suite now contains 16 tests and needs a fresh whole-suite run after this change.

**Latest whole-suite result:** the expanded `admin-regression-serial.xml` passed on 2026-10-23: **16 tests, 0 failures, 0 errors, 0 skipped**, 295.8 seconds in one application browser with memory-only session reuse. Report `target/automation-artifacts/run_20261023_175408/extent-report.pdf`. Includes full Court Update create/edit/reopen-verify/delete (its focused run also passed in `run_20261023_175318`), owned invoice/PDF, owned template lifecycle, and the five read-only checks. Long Court notes are scoped through their exact `data-full-notes`, because displayed notes are clipped.

**Post-regression additions under focused verification:** Event lifecycle now also uses the real Calendar date picker and case-scoped event cards to assert create/edit/removal propagation. New `admin-document-generation.xml` uses a controlled DOCX containing the confirmed token and checks the generated Word XML for the entered value and absence of the raw token. This generation study is compiled but has not run; it is excluded from the 16-test master suite until verified. Generated Word fixture, template, and Matter are owned by the new run. Failed generation can leave its owned fixture chain for evidence-led cleanup; never sweep unrelated data.

**Direct Calendar propagation verified:** expanded `admin-event-lifecycle.xml` passed (1 test, 0 failures/errors/skips), report `run_20261023_180100/extent-report.pdf`. Date picker selects exact event month/year/day; the case-scoped card confirms Tenant and Company, updated hearing title, then absence after deletion. PWA Calendar splits the Company legal suffix into a separate span; assertions account for that markup without dropping Company identity.

**Generation study current state:** first attempt failed before saving due to a create-form prerequisite label mismatch. Second attempt created its owned template/Company/Property/Matter, then failed before generation due to an icon-prefixed empty inventory heading. Those assertions are repaired; third focused run is active. The new fixture source contains the real observed custom token; downloaded Word XML is parsed with external XML access disabled, and output must contain the entered value and no raw token. The study remains outside the master regression until it passes.

**Generation and 17-test master now verified:** `admin-document-generation.xml` passed 1/1 in `run_20261023_180800` after generated-file cleanup was scoped by the full accessible `View {name}` identity, since displayed names are clipped. Controlled DOCX custom-token substitution is verified from downloaded Word XML; generated owned documents and template are deleted. The master then passed **17 tests, 0 failures/errors/skips**, 337.8 seconds, one application browser/normal authenticated session. Report `target/automation-artifacts/run_20261023_180949/extent-report.pdf`. This includes the direct Calendar-card assertions. Inline OnlyOffice editing remains unavailable, independently of the passing DOCX re-upload/generation flow.

**Current next slice:** `admin-hierarchy-edit.xml` is live-verifying a fresh Company contact role update, Property unit/quadrant update, and Matter priority update with before-write ownership notes and reopen/reload persistence checks. Actual UI has duplicate Edit Property links; the POM scopes to the header action. New-property license/RAD Address values now use real street/city/state/ZIP addresses; backend `descriptor` keys had previously been mistaken for descriptive labels. Existing records were not retroactively changed. This data correction and hierarchy edits need their focused result before coverage is claimed.

**Load next:** `AGENTS.md`, this file, `docs/context/SAFETY_BOUNDARIES.md`, `docs/architecture/EXISTING_FRAMEWORK_AUDIT.md`, then the task-specific skill.

**Local-target migration and regression, 2026-10-26:** client supplied and approved
`http://192.168.0.229:8000/login`; `AutomationConfig` and the run guide now default
to and fail closed outside that local origin. The focused owned hierarchy-edit test
passed 1/1. A first full run exposed three genuine UI-contract issues (Calendar's
searchable picker overlay, Expense modal dismissal, and Matter submit/navigation)
plus one read-only Calendar tab-role change; all were repaired using normal visible
UI interactions, then reverified in focused suites. The final serial master passed
**18 tests, 0 failures, 0 errors, 0 skips** in 401.7 seconds. Its report is
`target/automation-artifacts/run_20261026_123638/extent-report.pdf`. It covers
owned Company/Property/Matter hierarchy and persistence edits; multi-tenant Matter
creation; Task/Event/Court Update/Notes/Documents/Time/Expense CRUD; draft invoice
PDF; template metadata and controlled DOCX variable generation; and five
observation-only search, Calendar, Workflow, Settings, and Reports contracts.
Restricted controls remain untouched; this is a passing scoped regression, not a
claim of complete PWA coverage.

**QA intelligence foundation, 2026-10-26:** the client-provided functional-QA plan
was reconciled with the existing evidence and suite. New `docs/qa/` artifacts
provide the master inventory, executable/design/blocked test registry, field
catalog, lifecycle and data-lineage matrices, document/variable/PWA matrices,
approval register, execution report, defect register, clarification register, and
coverage dashboard. They preserve unknowns rather than inventing validation rules.
`approvals/local-owned-scenarios.properties` is now enforced by
`AutomationConfig` before any mutation test opens a browser. Focused verification
of the manifest using `admin-hierarchy-edit.xml` passed 1/1 on the approved local
target; report `target/automation-artifacts/run_20261026_124812/extent-report.pdf`.
Next safe batch: observe and document no-submit field-validation behavior, then
implement only validation cases with confirmed expected messages.

**Field-contract observation, 2026-10-26:** `AdminFormContractObservationTest`
now opens Company, Property, and Matter forms in `PROD_OBSERVE` mode and asserts
their mapped visible controls without filling, choosing, uploading, or submitting
anything. It passed 1/1; report
`target/automation-artifacts/run_20261026_125537/extent-report.pdf`. Registry row
RAD-TC-00024 is now PASS. Field validation constraints and messages remain UNKNOWN;
the next test-design batch must observe them safely before attempting a negative
submission.

**Expanded validation-attribute observation, 2026-10-26:** the same read-only
test now maps native `type`, `required`, `min`, `max`, `maxlength`, `pattern`, and
`aria-required` attributes for hierarchy, Task, Event, and Template controls. It
passed 3/3 in `run_20261026_130646`; report
`target/automation-artifacts/run_20261026_130646/extent-report.pdf`. Confirmed
examples: Company name, Property name, rental-office address, business-license
number, Matter case type/opening date/tenant address/ZIP, Task name/due date,
Event date/start time/title, and template name/category expose required contracts;
tenant ZIP has `maxlength=10` and a 5-or-9-digit ZIP pattern; Event duration has
`maxlength=5` and a 24-hour duration pattern. Nothing was entered, uploaded, or
submitted. Validation-message text and persistence after a rejected submit remain
unknown and must not be inferred.

**Court Update contract observation, 2026-10-26:** `AdminFormContractObservationTest`
now also opens only the blank Court Updates screen in `PROD_OBSERVE`. It passed
4/4 in `run_20261026_130848`; report
`target/automation-artifacts/run_20261026_130848/extent-report.pdf`. Matter,
Notes, and case status expose required attributes; Notes has `maxlength=5000`.
No calendar entry was selected and no Court Update was submitted.
