# Quickstart: User Registration API

This guide is a validation sequence for the implementation phase. It does not
contain application source, full test suites, or migration contents.

## Prerequisites

- JDK 8
- Maven (or the project Maven wrapper)
- The implementation created from this plan, including Flyway V1 and V2

## Build and Verify

Run the full unit, MVC, and H2-backed component suite and enforce coverage:

```powershell
mvn clean verify
```

Expected: the build succeeds only when JaCoCo reports 100% line and method
coverage overall and 100% service branch coverage. Inspect
`target/site/jacoco/index.html` for the report. No package or class is excluded
from coverage.

## Start the Service

```powershell
mvn spring-boot:run
```

Expected: Flyway applies V1 and V2 before the service accepts requests. The
local H2 console is enabled for debugging at `/h2-console`; Actuator health and
info are available at `/actuator/health` and `/actuator/info`.

## Register a User

```powershell
curl.exe -i -X POST http://localhost:8080/api/v1/users `
  -H "Content-Type: application/json" `
  -d '{"firstName":"Ada","lastName":"Lovelace"}'
```

Expected: HTTP 201 and a `success`/`data`/`error` envelope. The `data` profile
contains a five-digit string `userId`, the names, a lowercase generated
`emailId`, and `active: true`. The internal database ID is absent.

## Validate Error and Collision Paths

- Submit an omitted or whitespace-only name. Expect HTTP 400, `success: false`,
  `data: null`, and an error object containing timestamp, status, error code,
  and descriptive message; no row is created.
- Submit a non-blank name made only of punctuation or spaces that normalizes to
  an empty email segment. Expect HTTP 400 with `INVALID_NAME_FOR_EMAIL` and no
  persisted profile.
- Register the same normalized name repeatedly. Expect `first.last@abs.com`,
  then `first.a.last@abs.com`, `first.b.last@abs.com`, and subsequent suffixes
  through `aa` as required. No two rows share an email.
- Verify that no response body includes `id`, even though the persisted row has
  a private primary key.

## Contract and Data References

- [OpenAPI contract](contracts/user-registration.openapi.yaml)
- [Data model](data-model.md)
- [Research decisions](research.md)
