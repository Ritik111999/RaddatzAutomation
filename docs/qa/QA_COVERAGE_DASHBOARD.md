# QA coverage dashboard

As of 2026-10-26:

Latest attempt `run_20261026_150215`: **12 passed, 1 failed, 6 skipped** from
19 selected methods. The table below records earlier verified scenario results;
it must not be interpreted as a clean latest-suite pass. See
`TEST_EXECUTION_REPORT.md` for the run history and current failures.

| Measure | Count | Denominator | Result |
| --- | ---: | ---: | --- |
| Executed automated scenarios | 23 | 23 executed scenarios | 100% pass rate |
| Passed scenarios | 23 | 23 executed | 100% |
| Designed validation/negative scenarios | 5 | N/A: confirmed-rule denominator unknown | N/A |
| Blocked scenarios | 3 | N/A | Inline editor, existing tenant editor, unconfirmed billing/document rules |
| Restricted modules | 3 | 12 mapped modules | 25% explicitly non-executable |

These are execution and inventory metrics, not a claim of complete functional or field coverage. The next approved batch is evidence-only mapping of validation constraints and field dependencies, followed by no-submit validation automation where the expected messages are observed.
