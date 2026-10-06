# User Registration API

A Spring Boot REST API test service for registering user profiles. It provides
a JSON backend contract that Angular, React, or other HTTP clients can consume;
no frontend application is included.

Registration creates an active user, assigns a random five-digit employee
identifier, and derives a unique email address from the submitted names. The
internal database primary key is never included in API responses.

## Features

- User registration through `POST /api/v1/users`.
- Required, non-blank first and last names, limited to 100 characters each.
- Public employee identifiers returned as five-digit JSON strings.
- Lowercase email generation with accent, whitespace, and punctuation normalization.
- Email collision resolution: `first.last@abs.com`, then
  `first.a.last@abs.com`, `first.b.last@abs.com`, through `z`, then `aa`, `ab`, and onward.
- Employee-ID collision retries and database uniqueness constraints.
- Separate insert transactions for recovery from concurrent uniqueness conflicts.
- A shared `success`/`data`/`error` envelope for registration success and errors.
- Central exception handling, OpenAPI documentation, and health/info endpoints.

## Technology Stack

| Tool / Library | Purpose |
| --- | --- |
| Java 8 | Application language and runtime baseline |
| Spring Boot 2.7.18 | Application configuration, startup, and embedded server |
| Maven | Dependency management, compilation, testing, and packaging |
| Spring MVC | REST controllers and JSON request/response handling |
| Spring Data JPA / Hibernate | Entity mapping and repository access |
| H2 | In-memory local and test database |
| Flyway | Versioned database migrations |
| Lombok | Constructors, accessors, builders, and SLF4J logger generation |
| `javax.validation` | Request constraints compatible with Spring Boot 2.7 |
| springdoc-openapi-ui 1.8.0 | OpenAPI 3 documentation and Swagger UI |
| Spring Boot Actuator | Infrastructure health and application information |
| JUnit 5 / Mockito / MockMvc | Unit, controller, and component tests |
| JaCoCo 0.8.13 | Coverage reporting and mandatory build thresholds |
| GitHub Spec Kit | Specification-driven development artifacts and workflows |
| GitHub Copilot in VS Code | Development assistance and Spec Kit skill execution |
| Git | Version control and the installed Spec Kit Git workflow extension |

Versions not explicitly pinned by the project are managed by the Spring Boot
parent. Spec Kit, Copilot, and VS Code are development tools, not runtime
dependencies of the API.

## Getting Started

### Prerequisites

- JDK 8, including the Java compiler.
- Maven 3.x available on `PATH`; development was verified with Maven 3.9.16.
- Git for cloning the repository.
- Internet access for the first Maven dependency download.

Check your environment:

```powershell
java -version
mvn -version
```

Ensure Maven reports the intended Java runtime and that `JAVA_HOME` points to
the JDK, not a standalone JRE. This repository does not include a Maven wrapper.

### Clone and Build

```powershell
git clone https://github.com/samsharma-eagle-org/demoapp.git
cd demoapp
mvn clean verify
```

Run commands from the directory containing [pom.xml](pom.xml). Maven creates
generated files under `target/`, which is excluded by [.gitignore](.gitignore).

### Run the Service

```powershell
mvn spring-boot:run
```

Alternatively, after a successful build:

```powershell
java -jar target/user-registration-api-0.0.1-SNAPSHOT.jar
```

The default address is `http://localhost:8080`. Stop the foreground process
with `Ctrl+C`. For a different port:

```powershell
java -jar target/user-registration-api-0.0.1-SNAPSHOT.jar --server.port=8081
```

In VS Code, launch `com.abs.user.UserRegistrationApplication` with **Run Java**
or the Java debugger. Do not run the service class as a standalone Java file;
the entry point is [UserRegistrationApplication.java](src/main/java/com/abs/user/UserRegistrationApplication.java).

## Registration API

### Request

`POST /api/v1/users` with `Content-Type: application/json`:

```json
{
  "firstName": "Ada",
  "lastName": "Lovelace"
}
```

Example for Windows PowerShell:

```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/users' -Method Post -ContentType 'application/json' -Body '{"firstName":"Ada","lastName":"Lovelace"}' | ConvertTo-Json -Depth 5
```

Names are trimmed for persistence while preserving their casing. Email segments
are lowercased, decomposed to remove accent marks, and stripped of non-ASCII
letters/digits. A name that produces an empty email segment is rejected.

### Success Response

HTTP `201 Created`; the randomly generated `userId` will vary:

```json
{
  "success": true,
  "data": {
    "userId": "48291",
    "firstName": "Ada",
    "lastName": "Lovelace",
    "emailId": "ada.lovelace@abs.com",
    "active": true
  },
  "error": null
}
```

Repeated registrations with the same normalized names create separate profiles
with different email addresses; this endpoint is not idempotent. Email addresses
are generated identifiers only: the service does not provision mailboxes or send email.

### Error Response

Example HTTP `400 Bad Request`:

```json
{
  "success": false,
  "data": null,
  "error": {
    "timestamp": "2026-10-06T10:00:00Z",
    "status": 400,
    "errorCode": "VALIDATION_ERROR",
    "message": "firstName: must not be blank"
  }
}
```

| HTTP Status | Error Code | Meaning |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | A required name is missing, null, blank, or over 100 characters |
| 400 | `INVALID_REQUEST` | Request body is missing or malformed |
| 400 | `INVALID_NAME_FOR_EMAIL` | A name has no usable email characters after normalization |
| 404 | `RESOURCE_NOT_FOUND` | No matching endpoint exists |
| 500 | `INTERNAL_SERVER_ERROR` | Unexpected failure; internal details are not returned |

See the [OpenAPI contract](specs/001-user-registration-api/contracts/user-registration.openapi.yaml)
for the documented request and response schemas.

## Documentation and Monitoring

| URL | Purpose |
| --- | --- |
| `http://localhost:8080/swagger-ui.html` | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs` | Generated OpenAPI document |
| `http://localhost:8080/actuator/health` | Health status |
| `http://localhost:8080/actuator/info` | Application information; may return an empty object |
| `http://localhost:8080/h2-console` | Local database debugging console |

Application logs use SLF4J through Lombok `@Slf4j`, with parseable event names:
INFO for creation, WARN for handled validation/collision cases, and ERROR for
unexpected exceptions. Logs use the default Spring Boot text format, not a JSON
log encoder.

## Database and Configuration

Local settings are defined in [application.yml](src/main/resources/application.yml):

- JDBC URL: `jdbc:h2:mem:userdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- Username: `sa`
- Password: empty for local development.
- Hibernate validates the schema; it does not create or update tables.
- Flyway applies migrations at startup.

Use that JDBC URL and local credentials in the H2 console. Data is lost when
the application JVM stops; this configuration is not durable production storage.

| Migration | Purpose |
| --- | --- |
| [V1](src/main/resources/db/migration/V1__create_id_generator_table.sql) | Create and seed the table-backed internal ID generator |
| [V2](src/main/resources/db/migration/V2__create_users_table.sql) | Create users with primary key, unique employee/email constraints, and active-state validation |

The public employee identifier is separate from the generated internal `id`.
The Java/API `active` property is Boolean; SQL stores `Y`/`N` in `VARCHAR(1)`
through [BooleanYnConverter.java](src/main/java/com/abs/user/entity/converter/BooleanYnConverter.java).

Migration scripts avoid vendor-specific identity syntax. Actual migration to
MySQL, PostgreSQL, or Oracle still requires validation against the chosen
database version, driver, dialect, and supported SQL types; cross-database
execution has not been verified by the H2 tests.

The test profile uses a separate in-memory database configured in
[application-test.yml](src/test/resources/application-test.yml), with the H2
console disabled. Runtime settings can be overridden through Spring Boot
environment variables or command-line properties.

## Architecture and Repository Layout

```text
pom.xml                         Maven dependencies and coverage gates
src/main/java/com/abs/user/
  UserRegistrationApplication.java
  controller/                   HTTP routing
  dto/                          Public request, response, and envelope types
  entity/                       Persistence model and Boolean converter
  exception/                    Business exceptions and global error mapping
  repository/                   Spring Data persistence access
  service/                      Registration, ID allocation, and insert attempts
src/main/resources/
  application.yml               Local configuration
  db/migration/                 Flyway migrations
src/test/                       Unit, MVC, startup, and H2 integration tests
.github/skills/                 Copilot-discoverable Spec Kit skills
.specify/                       Constitution, templates, scripts, and extensions
specs/001-user-registration-api/ Feature spec, design, tasks, and checklists
```

Request flow: controller validation, registration service, repository/isolated
insert transaction, then explicit response DTO mapping. Persistence entities are
not returned from controllers, preventing internal-primary-key serialization.

## Testing and Coverage

```powershell
mvn test
mvn clean verify
```

Tests cover valid creation, request validation, shared error envelopes,
normalization, suffix progression/rollover, employee-ID collisions, and concurrent
registration. Component tests use `@SpringBootTest`, MockMvc, real Flyway
migrations, and an isolated H2 database.

`mvn clean verify` enforces:

- 100% production line coverage.
- 100% production method coverage.
- 100% branch coverage in `com.abs.user.service`.
- No coverage exclusions.

Read the HTML report at `target/site/jacoco/index.html` and test reports under
`target/surefire-reports/`. The last implementation verification passed 39 tests
and all configured coverage checks; rerun verification after changes.

Do not use `-DforkCount=0` for coverage verification: JaCoCo's configured agent is
attached to the forked test JVM, so disabling forks can cause coverage checks to
be skipped. The bundled Flyway/H2 versions may also emit a compatibility warning;
successful H2 tests are not evidence of compatibility with other databases.

## Spec Kit Development Workflow

This repository was initialized with GitHub Spec Kit `1.1.1.dev0`, Copilot skills
integration, PowerShell scripts, and sequential feature numbering. The project
constitution defines the architectural and quality constraints.

The workflow used for this feature is:

1. `/speckit-constitution`: establish project principles.
2. `/speckit-specify`: describe feature behavior and acceptance criteria.
3. `/speckit-clarify`: resolve ambiguous product decisions.
4. `/speckit-plan`: document architecture, data model, contracts, and research.
5. `/speckit-checklist`: create reviewer-owned requirements-quality checklists.
6. `/speckit-tasks`: generate dependency-ordered implementation tasks.
7. `/speckit-analyze`: review artifact consistency without modifying files.
8. `/speckit-implement`: implement and validate tasks.
9. `/speckit-converge`: identify remaining unbuilt work when needed.

Use specific skill names such as `/speckit-plan`; there is no single `/seckit`
command. These skills run through Copilot, not as application endpoints.

The installed Git extension adds branch, validation, initialization, remote,
and commit skills. Hook registrations are in [.specify/extensions.yml](.specify/extensions.yml);
automatic commits are disabled in [git-config.yml](.specify/extensions/git/git-config.yml).
An optional hook notice does not mean a commit was made.

Custom checklist checkboxes represent reviewer approval of requirements quality,
not implementation completion. Task checkboxes track implementation work. Neither
replaces executable tests or the Maven coverage gate.

### Project Documents

- [Constitution](.specify/memory/constitution.md)
- [Feature specification](specs/001-user-registration-api/spec.md)
- [Implementation plan](specs/001-user-registration-api/plan.md)
- [Data model](specs/001-user-registration-api/data-model.md)
- [Research decisions](specs/001-user-registration-api/research.md)
- [Tasks](specs/001-user-registration-api/tasks.md)
- [Quickstart validation guide](specs/001-user-registration-api/quickstart.md)
- [API requirements checklist](specs/001-user-registration-api/checklists/api.md)
- [Specification quality checklist](specs/001-user-registration-api/checklists/requirements.md)

## Security Boundary and Limitations

This is a test-service foundation, not a production-ready identity system:

- Authentication and authorization are delegated to an external gateway/security
  team; Spring Security is intentionally absent.
- Do not expose the unauthenticated API, H2 console, or monitoring endpoints
  publicly without appropriate external controls.
- No login, password storage, mailbox provisioning, profile updates/deletion, or
  frontend application is included.
- No feature-specific performance target is defined for the test-service stage.
- There are only 90,000 possible five-digit employee IDs. Retry loops currently
  have no exhaustion bound; capacity and failure policies need review before
  production use.
- Java 8 and Spring Boot 2.7 are intentionally retained project constraints;
  evaluate their support and dependency-security posture before production deployment.

## Contributing

Review the constitution and feature documents before changing behavior. Keep the
API contract and relevant design/task documents aligned, add focused tests, and
run `mvn clean verify` before committing. Never commit generated `target/` files
or local credentials. Architectural exceptions require an explicit governance
decision rather than weakening coverage checks or silently changing constraints.
