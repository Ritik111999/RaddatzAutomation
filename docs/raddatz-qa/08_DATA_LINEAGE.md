# Data lineage

```mermaid
flowchart LR
  Company --> Property
  Company --> CompanyContacts
  Property --> Matter
  Property --> PropertyContacts
  Matter --> Tenant
  Matter --> CaseDetails
  Matter --> KeyDates
  Matter --> Task
  Matter --> Note
  Matter --> Document
  Matter --> Event
  Task --> Dashboard
  Task --> TaskList
  Task --> MatterTimeline
  Event --> Calendar
  Event --> CourtUpdate
  Template --> GeneratedDocument
  Template --> CustomVariables
```

Observed evidence confirms the hierarchy and visible consumers. It does not yet prove database cardinalities, cascade behavior, or dynamic-variable substitution. `OBSERVED: RAD-E-0002, RAD-E-0004 through RAD-E-0011`.
