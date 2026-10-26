# Requirement clarifications

1. Confirm required/format/length/uniqueness rules and validation messages for Company, Property, Matter, Tenant, Task, Event, Expense, and template fields.
2. Confirm whether matter/timeline/dashboard propagation is immediate, eventual, or intentionally omitted for each entity.
3. Confirm invoice date-range inclusion rules for owned Expenses as well as Time entries.
4. Confirm source mappings and missing-data behavior for built-in document variables.
5. Confirm supported document file types, size limits, and rejection behavior.
6. Confirm PWA offline, reconnection, cache, and supported viewport expectations.

Until answered, related negative/boundary cases remain DESIGN or BLOCKED, not product defects.

Client-confirmed on 2026-10-26: Court Update continues through Court Time, where a
field selection auto-populates an amount, then Save produces an entry in Time.
Next evidence needed: exact UI field/option labels, rate/amount sources and
rounding, edit/delete behavior, and proof of the same record in Time and invoicing.
Invoice Send belongs to the business endpoint; determine whether it only updates
local state or delivers externally before executing it under the messaging boundary.
