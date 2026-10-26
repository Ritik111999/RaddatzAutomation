# Workflow lifecycle matrix

| Flow ID | Verified prerequisite chain | Verified checkpoints | Unverified / blocked transition |
| --- | --- | --- | --- |
| RAD-FLOW-001 | Company → Property → Matter → Tenant(s) | Grid lineage; parent IDs; reopen edits | Existing-Matter second-tenant persistence is blocked by known defect |
| RAD-FLOW-002 | Matter → Task | Derived Client; task list; edit/delete | Dashboard/timeline propagation is not a passing oracle until expected behavior is confirmed |
| RAD-FLOW-003 | Matter → Event → Calendar → Court Update | Calendar card; Court Update lifecycle; event removal | Notifications/invites and billing handoff excluded |
| RAD-FLOW-004 | Matter → Time/Expense → Draft Invoice | Owned source entry; total; local PDF | Send, allocation, settlement, void excluded |
| RAD-FLOW-005 | Template + Matter → generated DOCX | Custom token replaced; downloaded XML checked | Inline editor and unconfigured variable source mapping unknown |

Source: client-confirmed dependency model and final 2026-10-26 regression. No transition is treated as a requirement unless shown above or marked UNKNOWN.
