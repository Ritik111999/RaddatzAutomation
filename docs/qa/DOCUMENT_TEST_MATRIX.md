# Document test matrix

| ID | Scenario | Status | Evidence / safety boundary |
| --- | --- | --- | --- |
| RAD-DOC-001 | Owned Matter DOCX upload/view/download/delete | PASS | RAD-TC-00008; disposable fixture only |
| RAD-DOC-002 | Owned template metadata/variable/source persistence | PASS | RAD-TC-00014; editor field values are oracle |
| RAD-DOC-003 | Owned custom-token generated DOCX content | PASS | RAD-TC-00015; XML parser blocks external entities |
| RAD-DOC-004 | Inline Word editor save/reopen | BLOCKED | OnlyOffice unavailable/read-only in local environment |
| RAD-DOC-005 | Unsupported/corrupt/oversize fixture rejection | DESIGNED | Validation contract not yet observed; no confidential content |
| RAD-DOC-006 | Template billing side effects | BLOCKED | Requires client rule and bounded owned-data approval |
