# Implementation Plan: User Registration API

**Branch**: `001-user-registration-api` | **Date**: 2026-10-05 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-user-registration-api/spec.md`

## Summary

Create a Java 8 / Spring Boot 2.7.18 REST service foundation and implement
`POST /api/v1/users` with validation, a consistent success/error envelope,
five-digit public employee IDs, normalized collision-free email addresses, and
H2 persistence. Flyway owns schema changes. A table-backed JPA identifier
generator is selected to avoid vendor-specific identity syntax while keeping the
V2 users migration in ANSI SQL. Tests cover service branches, MVC validation,
and the complete H2-backed request flow; JaCoCo gates line and method coverage
at 100%.

## Technical Context

**Language/Version**: Java 8

**Primary Dependencies**: Spring Boot 2.7.18; Spring MVC; Spring Data JPA;
Hibernate; Lombok; `javax.validation`; Flyway; H2; springdoc-openapi-ui 1.8.0;
Spring Boot Actuator

**Storage**: H2 in-memory for local development and tests; Flyway migrations

**Testing**: JUnit 5, Mockito, MockMvc, `@SpringBootTest`, JaCoCo 0.8.x

**Target Platform**: Java 8 server runtime

**Project Type**: Single Spring Boot REST microservice

**Performance Goals**: No feature-specific target for the test-service stage;
production objectives follow the broader service SLO.

**Constraints**: Spring Boot 2.7.x only; no in-service authentication or
authorization; one documented JSON envelope; internal database ID never leaves
the persistence boundary; all migrations use ANSI SQL; JaCoCo line and method
coverage are each at least 100%; no coverage exclusions.

**Scale/Scope**: One create-user endpoint; no feature-specific load target.
The workspace currently has no Java source tree or build manifest, so the first
implementation step establishes the Maven service foundation.

**Planning Assumptions**: Maven is selected because `mvn clean verify` is the
requested verification path. Request names are limited to 100 characters;
generated email storage allows 320 characters. Store the Boolean active state
as ANSI `VARCHAR(1)` values `Y`/`N` and convert to/from Java Boolean for Oracle
portability.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Constitution requirement | Pre-design check | Post-design check |
| --- | --- | --- |
| Java 8 and latest Spring Boot 2.7.x | PASS: pin Boot 2.7.18 and Java 8 in Maven | PASS: plan retains the pinned baseline |
| Stable JSON contract for Angular/React clients | PASS: clarified envelope and response fields are specified | PASS: OpenAPI contract defines success and error schemas |
| Central global exception handling | PASS: validation and persistence failures use one response envelope | PASS: global advice is in the controller layer |
| H2 and portable ANSI Flyway migrations | PASS: use standard DDL and a table-backed ID generator | PASS: `VARCHAR(1)` Boolean storage works across the target database families |
| JUnit 5, Mockito, Spring Boot component tests | PASS: all required test layers are in scope | PASS: service, MVC, and H2 integration coverage planned |
| 100% JaCoCo line and method coverage | PASS: no production-code exclusions | PASS: verify gate enforces both counters; service branch coverage is also tested |
| External security boundary | PASS: no Spring Security or in-service auth modules | PASS: gateway remains responsible for authentication and authorization |
| Structured logs and Actuator endpoints | PASS: INFO/WARN behavior follows constitution | PASS: existing operational endpoints remain enabled and documented |

**Gate result**: PASS. The API/entity expose Boolean while portable ANSI
`VARCHAR(1)` storage and a JPA converter avoid vendor-specific SQL.

## Project Structure

### Documentation (this feature)

```text
specs/001-user-registration-api/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── user-registration.openapi.yaml
├── checklists/
│   └── requirements.md
└── tasks.md                 # Generated later by /speckit-tasks
```

### Source Code (repository root)

```text
pom.xml
src/
├── main/
│   ├── java/com/abs/user/
│   │   ├── UserRegistrationApplication.java
│   │   ├── controller/UserController.java
│   │   ├── dto/ApiError.java
│   │   ├── dto/ApiEnvelope.java
│   │   ├── dto/UserRegistrationRequest.java
│   │   ├── dto/UserResponse.java
│   │   ├── entity/User.java
│   │   ├── entity/converter/BooleanYnConverter.java
│   │   ├── exception/GlobalExceptionHandler.java
│   │   ├── exception/BusinessValidationException.java
│   │   ├── repository/UserRepository.java
│   │   ├── service/UserRegistrationService.java
│   │   ├── service/UserCreationAttempt.java
│   │   └── service/EmployeeIdGenerator.java
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           ├── V1__create_id_generator_table.sql
│           └── V2__create_users_table.sql
└── test/
   ├── java/com/abs/user/
   │   ├── controller/UserControllerTest.java
   │   ├── dto/UserDtoTest.java
    │   ├── entity/UserTest.java
    │   ├── service/EmployeeIdGeneratorTest.java
    │   ├── service/UserRegistrationServiceTest.java
   │   └── UserRegistrationIntegrationTest.java
    └── resources/application-test.yml
```

**Structure Decision**: Use the single-project Maven layout and the user-proposed
`com.abs.user` root package. Keep API DTOs separate from the JPA entity so
serialization cannot expose `users.id`. Put business collision logic in the
service, persistence access in the repository, and cross-cutting exception
mapping in controller advice. Use focused, explicit classes rather than a
generic utility or a second module.

## Architecture and Implementation Sequence

Implementation is dependency ordered. Add focused tests alongside each layer;
the final verification step runs the complete suite and coverage gate.

1. **Create the service foundation**: add `pom.xml`, the Java 8 Spring Boot
  2.7.18 application entry point, Maven wrapper if available, and compile/test
  source roots. Pin dependency/plugin versions. Add MVC, JPA, validation,
  Lombok, Flyway, H2, springdoc 1.8.0, and Actuator. Use
  `spring-boot-starter-test` for JUnit 5, Mockito, and MockMvc, and pin the
  JaCoCo Maven plugin to 0.8.13. Do not add Spring Security.
2. **Configure runtime profiles**: configure the H2 in-memory datasource,
  `spring.h2.console.enabled=true` for local debugging, Flyway startup, the
  test profile, and Actuator health/info exposure. Keep secrets and
  environment-specific values externalized.
3. **Create portable migrations**: add `V1__create_id_generator_table.sql`
  with a standards-based generator table and initial `users` generator row;
  then add `V2__create_users_table.sql` for `users`, its required columns,
  primary key, and uniqueness constraints. Use only standard `CREATE TABLE`,
  `INSERT`, `VARCHAR`, `BIGINT`, `DEFAULT`, `NOT NULL`, `CHECK`,
  and constraint syntax. Do not use `AUTO_INCREMENT`, `SERIAL`, `IDENTITY`,
  vendor functions, or vendor-specific types.
4. **Implement the JPA entity and repository**: map `User` to `users`; use
  `Long id` with `GenerationType.TABLE` and the V1 generator row. Map the
  Boolean active property using `BooleanYnConverter` to SQL `VARCHAR(1)` values
  `Y`/`N`. Use Lombok
  `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, and
  `@Builder`; avoid `@Data` and unintended equality semantics. Map employee ID
  as a five-character string. Add repository signatures
  `boolean existsByEmailId(String emailId)` and
  `boolean existsByEmployeeId(String employeeId)`. Keep database uniqueness
  constraints authoritative when requests race.
5. **Implement request, response, and envelope DTOs**: add `UserRegistrationRequest`
  with `javax.validation` `@NotBlank` and `@Size(max = 100)` constraints;
  add `UserResponse` with only `userId`, `firstName`, `lastName`, `emailId`,
  and `active`; add `ApiEnvelope<T>` and `ApiError`. Never serialize `User`
  directly. `ApiEnvelope` avoids a Java simple-name collision with Swagger's
  `@ApiResponse` annotation.
6. **Implement service logic**: use `EmployeeIdGenerator` to generate
  candidates in `[10000, 99999]` and expose each as a five-digit JSON string.
  Check employee ID uniqueness and regenerate on collision. Normalize email segments by trimming/lowercasing,
  converting accented letters to ASCII bases, and removing whitespace and
  punctuation. Try the base address, then `a` through `z`, then `aa`, `ab`,
  and subsequent alphabetic suffixes. Use Lombok `@Slf4j` for structured,
  parseable logs: INFO for successful creation and WARN for email collisions;
  never log internal IDs or personal data.
7. **Handle concurrent uniqueness conflicts**: keep orchestration in
  `UserRegistrationService` and put each insert attempt in a separate
  `UserCreationAttempt` method with `REQUIRES_NEW` transaction propagation.
  Rely on named email/employee-ID unique constraints, not only pre-insert
  existence checks. Name the constraints `uq_users_email_id` and
  `uq_users_employee_id`. If the email constraint wins a race, retry from the
  non-transactional orchestrator with the next email suffix and a fresh
  employee ID. If only the employee-ID constraint wins, keep the still-free
  email and generate a fresh employee ID. Each retry runs in a new transaction.
  Retry only these recognized conflicts; propagate unrelated persistence
  failures.
8. **Implement controller and global errors**: expose `POST /api/v1/users`,
  validate the request, return HTTP 201 in the standard envelope, and map
  validation/business/unexpected exceptions and `NoHandlerFoundException` to
  HTTP error responses with the required error envelope. Configure MVC so
  unmatched API routes reach `@RestControllerAdvice`. Add OpenAPI
  `@Operation`/`@ApiResponse`/`@Schema` annotations for the endpoint.
9. **Add and finish tests**: write service/DTO/entity unit tests, MVC tests,
  then `@SpringBootTest` + `@AutoConfigureMockMvc` integration tests against a
  separate in-memory database and real Flyway migrations. Include concurrent
  uniqueness behavior where deterministic test setup allows it.
10. **Run the release gate**: execute `mvn clean verify`; require JaCoCo to
   fail the build below 100% line and method coverage, and below 100% branch
   coverage for the service package. Review the XML/HTML reports and confirm
   the API contract and migration scripts match the design artifacts.

## Database and Migration Design

Use a table-backed JPA ID generator rather than native sequence or identity
syntax. Native auto-increment/sequence declarations differ across the target
databases; the generator table lets the SQL migrations remain standard while
JPA allocates the internal `users.id`. V1 creates the generator table and seed
row. V2 creates the user table.

`users` columns: `id BIGINT` primary key; `employee_id VARCHAR(5)` unique and
not null; `first_name VARCHAR(100)` and `last_name VARCHAR(100)` not null;
`email_id VARCHAR(320)` unique and not null; `is_active VARCHAR(1) DEFAULT 'Y'
NOT NULL CHECK (is_active IN ('Y', 'N'))`. The unique email constraint provides the lookup index on
`email_id`; do not add a redundant second index. The employee identifier also
has a unique constraint.

The API/entity Boolean property maps to `Y`/`N` through a JPA converter. The
database representation uses ANSI `VARCHAR(1)` and a standard check constraint,
so it does not depend on Oracle SQL Boolean support.

## Testing Blueprint

| Test slice | Scope and decisive cases |
| --- | --- |
| `UserRegistrationServiceTest` | Base email with no collision; first collision selects `a`; occupied `a` selects `b`; occupied through `b` selects `c`; all single letters occupied selects `aa`; normalized accented/punctuated names; employee ID collision retries; successful and warning log paths; persistence race retry and unrelated failure propagation |
| `UserControllerTest` | Valid request returns 201 and the exact envelope/profile fields; omitted, null, empty, and whitespace names return 400; response never includes `id`; global error fields are present |
| `UserDtoTest` / `UserTest` / `BooleanYnConverterTest` | Exercise constructors, builders, getters, setters, and both Boolean conversion directions for Lombok/JPA-mapped types without `@Data`-generated equality methods |
| `UserRegistrationIntegrationTest` | Start isolated Spring context, run Flyway V1/V2 on test H2, submit request through MockMvc, assert persisted row and generated email/ID, assert response envelope and absence of internal ID, assert validation does not persist a row |

Configure JaCoCo `check` for 100% `LINE` and `METHOD` coverage on production
code, with no coverage exclusions. Add a service-package `BRANCH` rule at 100%
for the collision and retry algorithm. Exercise DTO/entity generated methods
directly. Keep component tests in the same `mvn clean verify` lifecycle so
their execution contributes to the aggregate JaCoCo report.

## Complexity Tracking

No constitution violations are planned. The table-backed ID generator adds one
small supporting table, justified by the requirement to avoid vendor-specific
sequence/identity migration syntax across H2, MySQL, PostgreSQL, and Oracle.
