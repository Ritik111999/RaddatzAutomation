# Master coverage matrix

Scope reconciled on 2026-10-26 from the verified inventory, Java/TestNG suite,
and client-confirmed safety boundaries. IDs are traceability identifiers, not
claims of unobserved product requirements.

| ID | Module / screen | Confirmed function | Design | Automation | Latest execution | Scope |
| --- | --- | --- | --- | --- | --- | --- |
| RAD-MOD-001 | Companies & Matters | Owned Company/Property/Matter hierarchy | Partial | Yes | PASS, RAD-TC-00001/02/03 | In scope |
| RAD-MOD-002 | Matter workspace | Tenant, Notes, Documents, Timeline context | Partial | Yes | PASS, RAD-TC-00004/07/08 | In scope |
| RAD-MOD-003 | To-dos | Owned task lifecycle and list search/filter | Partial | Yes | PASS, RAD-TC-00005/06 | In scope |
| RAD-MOD-004 | Calendar | Owned event lifecycle, date/view/search controls | Partial | Yes | PASS, RAD-TC-00009/10 | In scope |
| RAD-MOD-005 | Court Updates | Owned Event-linked Court Update lifecycle | Partial | Yes | PASS, RAD-TC-00011 | In scope; billing handoff excluded |
| RAD-MOD-006 | Billing | Owned time, expense, draft invoice/PDF | Partial | Yes | PASS, RAD-TC-00012/13/14 | Owned entries/drafts only; settlement excluded |
| RAD-MOD-007 | Templates/documents | Owned upload/template/variable generation | Partial | Yes | PASS, RAD-TC-00015/16 | Inline editor unknown |
| RAD-MOD-008 | Search and reporting | Search/reset, Calendar views, Reports tabs | Partial | Yes | PASS, RAD-TC-00017 | Observation only |
| RAD-MOD-009 | Workflows | List and unsaved-form contract | Partial | Yes | PASS, RAD-TC-00018 | Observe only |
| RAD-MOD-010 | Settings | Menu/destination contract | Partial | Yes | PASS, RAD-TC-00019 | Restricted destinations not opened |
| RAD-MOD-011 | Client Portal | Publishing/access | No | No | Not executed | Restricted |
| RAD-MOD-012 | Integrations, backend, bulk, permissions | High-impact administration | No | No | Not executed | Restricted |

Confirmed dependency chain: Company → Property → Matter → Tenant(s) → Task/Event/Note/Document/Time/Expense; Event → Court Update; Template → generated owned document. Sources: client-confirmed 2026-10-23; RAD-E-0004 through RAD-E-0014; final local run `run_20261026_123638`.

Known design gaps are intentionally tracked as DESIGNED or UNKNOWN in the registry rather than represented as passed tests.
