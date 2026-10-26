# Latest execution report

Run: `run_20261026_123638` on the approved local target.

Result: 18 executed, 18 passed, 0 failed, 0 skipped, 0 errors; duration 401.7 seconds.

Artifact: `target/automation-artifacts/run_20261026_123638/extent-report.pdf`.

The result applies to the registry rows RAD-TC-00001 through RAD-TC-00018 only. Designed, blocked, restricted, and unknown scenarios are not included in the pass rate.

Field-contract observation run: `run_20261026_125537` on the same local target.
Result: 1 executed, 1 passed, 0 failures/errors/skips. It opened the Company,
Property, and Matter forms and asserted controls only; it entered no values and
submitted no form.

Expanded native-constraint observation run: `run_20261026_130646` on the same
local target. Result: 3 executed, 3 passed, 0 failures/errors/skips. It verified
the hierarchy form controls plus read-only Task, Event, and blank Template form
metadata. Captured facts include the native `required`, `type`, `min`, `maxlength`,
and `pattern` attributes. No form was filled, submitted, uploaded, or saved.
Browser/custom validation message text remains unobserved, because exercising it
would require a submission probe whose non-persistence must first be confirmed.

Court Update extension: `run_20261026_130848` on the same local target. Result:
4 executed, 4 passed, 0 failures/errors/skips. It added blank Court Update
metadata: Matter, Notes, and case-status controls are natively required; Notes
has `maxlength=5000`; calendar date is an optional date input. No Event was
selected and no Court Update was submitted.

Latest complete serial regression: `run_20261026_142452` on the approved local
target. Result: 19 executed, 19 passed, 0 failures, 0 errors, 0 skipped; duration
408.2 seconds. It includes permitted update persistence and propagation checks,
including revised Time Entry data in its owned draft-invoice source. Restricted
modules and payment/send/allocation/settlement controls remain excluded.
