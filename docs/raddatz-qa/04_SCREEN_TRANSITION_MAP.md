# Screen transition map

```mermaid
stateDiagram-v2
  Dashboard --> CompaniesMatters: Clients
  Dashboard --> Todos: To-dos
  Dashboard --> Calendar: Calendar
  Dashboard --> Workflows: Workflows
  Dashboard --> CourtUpdates: Court Updates
  CompaniesMatters --> Company: company link
  CompaniesMatters --> Property: property link
  CompaniesMatters --> MatterGeneral: matter link
  MatterGeneral --> MatterNotes: Notes tab
  MatterGeneral --> MatterDocuments: Documents tab
  MatterGeneral --> MatterTodos: To-do tab
  MatterGeneral --> MatterTimeline: Timeline tab
  MatterDocuments --> TemplateFlow: Use Template/Add Document
  Calendar --> MatterGeneral: Matter link
  CourtUpdates --> Calendar: View Event Details
```

These are route-level transitions observed in the UI only. The TemplateFlow is intentionally not executed; it is a documented transition inferred from the visible link. `OBSERVED: RAD-E-0004, RAD-E-0006, RAD-E-0007, RAD-E-0008, RAD-E-0009`.
