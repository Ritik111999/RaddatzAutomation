# Document lifecycle

1. Open a matter's Documents tab.
2. Select file upload, Add Document, or Use Template.
3. A template is defined with basic information, category, active state, optional flat fee, optional billing entries, custom variables, and a Word `.docx` source.
4. The UI states that the Word source is converted for document generation and may be edited in a Word editor; it also states that edits are saved into the real `.docx`, not HTML.
5. The visible template registry tracks category, flat fee, required fields, status, last use, and created date.

Steps 1–3 and 5 are `OBSERVED`. Step 4 is a `DOCUMENTED` claim from on-screen UI text; save behavior, generated output, preview, substitution, and downstream document association remain `UNKNOWN`. `Evidence: RAD-E-0009, RAD-E-0011`.

No document was downloaded, uploaded, previewed, generated, or edited during this research pass.
