# System overview

## Verified scope

Raddatz is a case-management PWA with an administrator-facing workspace centered on a company, property, and matter hierarchy. The verified current environment is the supplied local PWA, observed on 2026-10-23 under an administrator session. No live production URL was used.

## Observed operational model

1. A company may be shown without a property or matter.
2. A property is shown beneath its company once a matter exists against that property.
3. A matter holds one or more tenants, case data, key dates, work items, documents, notes, and timeline entries.
4. Tasks and calendar events surface matter references in their list and calendar views.
5. Court Updates start from a calendar entry and can carry matter, note, status, task/event, and billing-related behavior.
6. Document templates are reusable Word-based source documents with custom variables and optional billing entries.
7. Company and property detail views provide reciprocal hierarchy links, contact/document panels, case groupings, and financial/communication affordances.

All details are `OBSERVED` from RAD-E-0002 through RAD-E-0011 unless called out otherwise. Names, case identifiers, addresses, and user data are deliberately omitted.

## Safety boundary

Read-only/excluded areas are listed in [24_UNKNOWN_AND_BLOCKED_AREAS.md](24_UNKNOWN_AND_BLOCKED_AREAS.md). Outside those areas, future create/edit automation must use only uniquely identifiable, automation-owned records. Delete is prohibited.
