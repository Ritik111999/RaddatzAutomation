# Admin Automation Data Contract

## Scope

This suite automates the local Raddatz administrator panel only. Authentication is a fixture used to establish an administrator session; it is not a user-account or login test.

The allowed data chain is:

```text
Company → Property → Matter → Task
                            └→ Calendar Event
```

Each downstream record must be linked to the parent record created by the same scenario. Existing companies, properties, matters, tasks, and events are never selected, edited, completed, or deleted.

## Ownership rule

Every scenario creates a `Scenario reference <id>` in a Notes field and uses a unique `@knoxweb.us` contact email. The visible names are realistic business/person names without `test`, `automation`, or placeholder labels. The internal scenario reference is the only ownership marker.

Mutation scenarios require both switches:

```zsh
mvn test -Draddatz.run.local=true -Draddatz.allow.mutation=true
```

Without the mutation switch, the framework fails before it can create application data.

## Validated local form fields

### Company

Use Company Name; contact name/title/email/phones; street/suite/city/state/ZIP; and Notes. Use a property-management company, named contact, complete US postal address, and an `@knoxweb.us` email. Keep welcome email unchecked.

### Property

Use Client; Property Name; rental-office address; business-license address/number; RAD address/number/expiration; total units; hourly rate; and Notes. Select only the Company created earlier. Use a plausible multifamily property, office address, valid-looking license/RAD values, and a positive rate.

### Matter

Use Company; Property; tenant name/address/quadrant/unit/city/ZIP; case type/status; opening date; and Notes. Select only the scenario Company and Property. Use a complete tenant address and never enter SSN.

### Task

Use Matter; derived Client; task name; description; due date; priority; task type; and assignee. Select only the scenario Matter and confirm Client is derived from it. Use a business task title and future due date.

### Event

Use Matter; derived Property; courtroom; event type; title; category; date/time/duration; and Notes. Select only the scenario Matter. Keep client notifications, SMS, calendar invites, and document upload disabled unless explicitly requested.

## Dependency assertions

1. A new Property can be associated only with the newly created Company.
2. The Matter's Property selector is evaluated only after the new Company is selected.
3. The Task's Client is derived from its selected Matter; it is never independently chosen.
4. The Event's Property auto-populates from its selected Matter.
5. Searches and assertions use the scenario reference and relationship context, never list order or an existing business record.
