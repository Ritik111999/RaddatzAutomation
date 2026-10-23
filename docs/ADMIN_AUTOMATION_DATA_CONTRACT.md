# Admin Automation Data Contract

## Scope

This suite automates the local Raddatz administrator panel only. Authentication is a fixture used to establish an administrator session; it is not a user-account or login test.

The allowed data hierarchy is:

```text
Company
├── Property A
│   ├── Matter A1 → Tenant 1, Tenant 2, ...
│   │                ├── Task
│   │                └── Calendar Event
│   └── Matter A2 → Tenant 1, Tenant 2, ...
└── Property B
    └── Matter B1 → Tenant 1, Tenant 2, ...
```

A Company may have multiple Properties. A Property may have multiple Matters, and the Matter form supports multiple tenant cards with one designated as primary. Each downstream record must be linked to the parent record created by the same scenario. Existing companies, properties, matters, tasks, and events are never selected, edited, completed, or deleted.

## Ownership rule

Every scenario creates a `Scenario reference <id>` in a Notes field and uses a unique `@knoxweb.us` contact email. Every factory call produces a fresh Company portfolio, Property building, tenant, telephone numbers, license/RAD numbers, docket number, task title, and event title. The visible names remain realistic business/person names without `test`, `automation`, or placeholder labels. The internal scenario reference is the ownership marker.

Each mutation test creates its required parent chain in the same run. It never relies on a Company, Property, Matter, Task, or Event created by an earlier run.

Mutation scenarios require both switches:

```zsh
mvn test -Draddatz.run.local=true -Draddatz.allow.mutation=true
```

Without the mutation switch, the framework fails before it can create application data.

## Validated local form fields

### Company

Use Company Name; contact name/title/email/phones; street/suite/city/state/ZIP; and Notes. The fixture supplies realistic values for every one of those fields: a property-management company, named regional manager, complete US postal address, formatted office/mobile numbers, and an `@knoxweb.us` email. Keep welcome email unchecked so a test run never sends external email.

### Property

Use Client; Property Name; rental-office address; business-license Address/number; RAD Address/number/expiration; total units; contact name/title/email/phones; hourly rate; and Notes. The backend names the two Address inputs `descriptor`; supply a full street/city/state/ZIP address, not a license description. Select only the Company created earlier. Use a plausible multifamily property, office address, valid-looking license/RAD values, formatted contact details, a positive rate, and an explicit unit count. Preserve portal controls. Quadrant is available on Edit Property; use the address's actual quadrant and verify its saved value in the edit lifecycle.

### Matter

Use Company; Property; one or more tenant cards with name/address/quadrant/unit/city/ZIP/phone/email; case number/type/status/plan/priority; opening date; and Notes. Select only the scenario Company and Property. When a Matter has more than one tenant, explicitly designate the appropriate primary tenant. Use complete tenant addresses, `@knoxweb.us` tenant emails, and a unique docket-style case number; never enter SSN. Leave direct case links and staff assignments empty unless an authorized, scenario-owned external link or staff fixture is supplied.

### Task

Use Matter; derived Client; task name; description; due date; priority; task type; and assignee. Select only the scenario Matter and confirm Client is derived from it. Use a business task title and future due date.

### Event

Use Matter; derived Property; courtroom; event type; title; category; date/time/duration; and Notes. Select only the scenario Matter. Keep client notifications, SMS, calendar invites, and document upload disabled unless explicitly requested.

## Dependency assertions

1. Every new Property is associated only with the newly created Company; a scenario may add more than one property to that Company.
2. The Matter's Property selector is evaluated only after the new Company is selected and must select the intended owned Property.
3. A multi-tenant Matter designates exactly one primary tenant and never reuses tenants from an existing Matter.
4. The Task's Client is derived from its selected Matter; it is never independently chosen.
5. The Event's Property auto-populates from its selected Matter.
6. Searches and assertions use the scenario reference and relationship context, never list order or an existing business record.
