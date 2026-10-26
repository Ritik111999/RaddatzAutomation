# Automation implementation status

Implemented architecture: Java 21, Playwright Java, TestNG, Maven, POM, Log4j2, Extent HTML/PDF. The mutable serial suite has one browser and sequential contexts; credentials and authentication state are never persisted to disk.

Implemented: 18 executable scenarios in `admin-regression-serial.xml`.

Read-only field-contract coverage: RAD-TC-00024 through RAD-TC-00027 map visible controls and native attributes on hierarchy, Task, Event, Court Update, and Template forms. Designed but not implemented: submission-based field validation cases RAD-TC-00019 through RAD-TC-00023, because browser/custom validation messages and non-persistence behavior are not yet confirmed. Restricted modules deliberately have no executable POMs.
