# Document lifecycle

1. Open a matter's Documents tab.
2. Select file upload, Add Document, or Use Template.
3. A template is defined with basic information, category, active state, optional flat fee, optional billing entries, custom variables, and a Word `.docx` source.
4. The UI states that the Word source is converted for document generation and may be edited in a Word editor; it also states that edits are saved into the real `.docx`, not HTML.
5. The visible template registry tracks category, flat fee, required fields, status, last use, and created date.

Steps 1–3 and 5 are `OBSERVED`. Step 4 is a `DOCUMENTED` claim from on-screen UI text; save behavior, generated output, preview, substitution, and downstream document association remain `UNKNOWN`. `Evidence: RAD-E-0009, RAD-E-0011`.

No document was downloaded, uploaded, previewed, generated, or edited during this research pass.

## Authorized owned-fixture automation expansion — 2026-10-23

Later local/admin execution supersedes that initial-pass limitation: a fictional
DOCX was uploaded to a newly created owned template; its preview opened and its
source downloaded. Reopening Edit retained exact name, description, category,
custom variable label/token/type/hint. The registry clips descriptions, so the
editor is the persistence oracle. Metadata update and preview after update were
observed; final lifecycle cleanup is under verification. Evidence: RAD-E-0014.

These observations do not prove typing in the Word editor or rendered variable
substitution in a generated Matter document. The unsaved Matter generation form
exposes template-specific variable fields and default case-document/timeline/note
actions; its disabled checked billing controls need confirmation against the
template's configured billing entries before downstream effects are asserted.

Subsequent controlled run (2026-10-23, report `run_20261023_180800`) now proves
custom-token substitution: a Word source containing
`{{HEARING_PREPARATION_INSTRUCTIONS}}` was uploaded to an owned template, selected
from an empty fresh owned Matter, populated, generated, downloaded, and parsed.
Output contains the exact entered instructions and no raw token. Its generated
owned document and template were cleaned up. The 17-test combined regression
repeated this successfully (`run_20261023_180949`). Timeline/note options were off;
portal/email remained unchecked; no template billing rows were configured. This
does not claim inline OnlyOffice editing or every variable type/billing variant.
