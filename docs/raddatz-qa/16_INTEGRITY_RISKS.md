# Integrity risks

| Risk | Why it matters | Test control |
| --- | --- | --- |
| Editing a user-owned entity | Could corrupt live local test data and invalidate evidence | Mutate only records bearing current-run ownership marker |
| Status selector on Companies & Matters | Visible inline state control may change case lifecycle | No direct status toggling except isolated owned record with verification |
| Court Update billing handoff | Form explicitly references pushing time entries to billing | Treat all Court Update mutation as restricted |
| Template flat fee / billing entries | UI states generation can create fixed/time/expense billing data | Do not generate templates in discovery |
| Word editor persistence | UI says edits write into real `.docx` | Validate only using owned template and disposable fixture, after save behavior is mapped |
| Bulk/portal/export features | Can publish, export, or transform broad data sets | Excluded |
| Delete controls | Irreversible and explicitly prohibited | Never automate delete |

Evidence: RAD-E-0003 through RAD-E-0011.
