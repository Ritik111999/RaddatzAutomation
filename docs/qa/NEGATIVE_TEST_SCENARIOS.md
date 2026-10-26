# Negative test scenarios

| ID | Scenario | Execution status | Safety rationale |
| --- | --- | --- | --- |
| RAD-TC-00019 | Empty/whitespace required Company fields | DESIGN | No submit until local validation messages are observed |
| RAD-TC-00020 | Matter Property selection without its Company prerequisite | DESIGN | Must not create a record |
| RAD-TC-00022 | Unsupported/corrupt owned document fixture | DESIGN | Requires confirmed server validation behavior |
| RAD-TC-00023 | Draft invoice date-range/source exclusion | DESIGN | Requires confirmed invoice inclusion rule |

Negative tests will be promoted to automation only after their expected application behavior is observed or client-confirmed. They are not defects by default.
