# Business workflows

## Matter lifecycle — observed

Company → Property → Matter → Tenant(s) → Case details/key dates → Tasks, notes, documents, timeline. A Company can own multiple Properties, and a Property can own multiple Matters; each additional branch is entered through **Add Property** or **Add New Matter**, never by creating a duplicate Company. `OBSERVED: RAD-E-0007, RAD-E-0008, RAD-E-0009`; `CLIENT-CONFIRMED: 2026-10-23`.

## Work planning — observed

Matter tasks are visible in the matter workspace, dashboard task cards, and the all-task list. Calendar events carry matter links and are filterable by category, person, and courtroom. `OBSERVED: RAD-E-0002, RAD-E-0005, RAD-E-0006, RAD-E-0009`.

## Court processing — partially observed

Calendar entry selection appears to load a matter into Court Updates. The form includes notes, case status, and optional linked to-do/event choices. Because it also advertises a billing handoff, the submission path is not exercised. `OBSERVED: RAD-E-0004`.

## Document lifecycle — partially observed

Matter Documents supports upload and template entry. A document template accepts custom variables and a Word `.docx` source, which the UI says is converted for editing and generation. Generation/save behavior remains untested. `OBSERVED: RAD-E-0009, RAD-E-0011`; `UNKNOWN: editor persistence and variable substitution syntax`.
