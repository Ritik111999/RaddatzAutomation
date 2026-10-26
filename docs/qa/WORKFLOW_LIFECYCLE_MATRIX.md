# Workflow lifecycle matrix

Primary acceptance journey (CLIENT-CONFIRMED, 2026-10-26): admin setup → Company
→ Property → Matter → documents/notes at all three levels → assigned Matter Tasks
→ Events/court Event → submitted Court Update → Court Time auto-amount/edit/delete
→ saved Time entry → Time/Expenses → New Invoice Draft → saved draft → Send.

RAD-FLOW-006 is the parent end-to-end journey for the rows below. Its status is
**REQUIRED / NOT IMPLEMENTED AS ONE CONNECTED TEST**. Company/Property supporting
content, Court Time handoff, automatic amount oracle, and invoice Send endpoint
are not established by the existing 19-method suite. Send remains unexecuted until
its effect is classified against the external-messaging boundary. Historical
passing component assertions must not be counted as a passing RAD-FLOW-006.

| Flow ID | Verified prerequisite chain | Verified checkpoints | Unverified / blocked transition |
| --- | --- | --- | --- |
| RAD-FLOW-001 | Company → Property → Matter → Tenant(s) | Grid lineage; parent IDs; reopen edits | Existing-Matter second-tenant persistence is blocked by known defect |
| RAD-FLOW-002 | Matter → Task | Derived Client; task list; edit/delete | Dashboard/timeline propagation is not a passing oracle until expected behavior is confirmed |
| RAD-FLOW-003 | Matter → Event → Calendar → Court Update | Calendar card; Court Update lifecycle; event removal | Notifications/invites and billing handoff excluded |
| RAD-FLOW-004 | Matter → Time/Expense → Draft Invoice | Revised owned Time Entry survives reload and appears in draft source; local PDF | Expense date-range inclusion rule; send, allocation, settlement, void excluded |
| RAD-FLOW-005 | Template + Matter → generated DOCX | Custom token replaced; downloaded XML checked | Inline editor and unconfigured variable source mapping unknown |

Source: client-confirmed dependency model and final 2026-10-26 regression. No transition is treated as a requirement unless shown above or marked UNKNOWN.
