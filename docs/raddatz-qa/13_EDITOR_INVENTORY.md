# Editor inventory

## Matter Note editor

Observed controls include heading/format buttons, ordered and unordered list buttons, color choices, emoji picker, text-entry area, and Submit. `OBSERVED: RAD-E-0009`.

## Word template editor

The template form advertises: Word `.docx` upload, conversion for document generation, editing below in a Word editor, replacement by new `.docx`, and persistence back into the actual Word file. The editor itself is not opened because no file was uploaded and its persistence/autosave behavior is unverified. `DOCUMENTED by UI text: RAD-E-0011`; `UNKNOWN: editor control set, autosave, preview, recovery, and substitution behavior`.

Later owned-fixture inspection (2026-10-23, RAD-E-0014) uploaded and reopened a
fictional DOCX template. The local environment displays `Read-only Word layout —
read-only preview (OnlyOffice unavailable)`. Metadata and variable definitions
persist and are verified through the template editor form. Inline Word content
editing is unavailable here; the UI offers copy-merge-field and Word re-upload
guidance instead. Document-content save/autosave and rendered substitution remain
unverified; do not treat metadata editing as Word-content editor coverage.
