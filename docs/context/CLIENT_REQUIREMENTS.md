# Client requirements

- Use only the client-supplied local target for approved PWA work.
- Admin automation is the intended scope; authentication is setup, not a login test.
- Use field-appropriate realistic data with unique run ownership and `@knoxweb.us` email addresses.
- Keep Company → Property → Matter → Tenant(s), task, and event records linked to owned data.
- Create, read, update, download, view, and delete only automation-owned records outside restricted modules. Never edit or delete user-owned records.
- Restricted areas are read-only as defined in the safety policy. Billing entry/workflow testing is permitted; official payment processing is not in scope.

Source: client-confirmed conversation history; last consolidated 2026-10-23.

## Primary business lifecycle - client correction, 2026-10-26

Source: direct client explanation in chat on 2026-10-26. Classification:
CLIENT-CONFIRMED; high reliability as a requirement. This takes precedence over
earlier inferred module sequences; it does not imply runtime verification.

1. Authenticate as admin (session setup).
2. Create Company, then its Property, then its Matter. Reuse each parent when
   adding further Properties or Matters.
3. Add documents, notes, and applicable supporting content at Company, Property,
   and Matter levels.
4. Create Tasks on that Matter and assign them.
5. Create Events and the associated court Event for that Matter.
6. Save and submit the Court Update.
7. Continue to Court Time, set the relevant field, and check the amount that
   auto-populates. Exercise permitted owned-entry edit/delete paths.
8. Save Court Time and verify that the resulting entry appears in Time.
9. Exercise create/edit/delete in Time, and the associated Expense flow.
10. Open Invoices, create New Draft, save draft, then Send.

Template creation/editing and document generation are supporting journeys.
Exact Court Time field names, amount/rate formulas, rounding, and the distinction
between Event and court Event need UI evidence before implementation. Invoice
Send is part of the required business journey; its effect must be classified
before execution because external messaging is restricted by AGENTS.md. Record
the endpoint as unverified until an allowed local-only path is established.
