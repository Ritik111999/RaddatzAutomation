# Template mapping

| Template property | Visible source | Downstream relationship | Status |
| --- | --- | --- | --- |
| Name / description / category | Basic Information | Template registry filters and list columns | OBSERVED |
| Active status | Basic Information | Template registry status | OBSERVED |
| Flat fee | Basic Information | Potential billed fixed fee on generation | UI-DOCUMENTED; unexecuted |
| Time entries | Template Billing Entries | Potential automatic time entry creation | UI-DOCUMENTED; unexecuted |
| Expense entries | Template Billing Entries | Potential automatic expense creation | UI-DOCUMENTED; unexecuted |
| Custom variables | Template Variables | Required-field count in registry | OBSERVED existence; syntax unknown |
| Word `.docx` | Template Document | Editor/generation | UI-DOCUMENTED; unexecuted |

Evidence: RAD-E-0011 and RAD-E-0012. Since billing effects are described by the UI, template generation remains excluded until a dedicated owned-data safety scenario is approved.
