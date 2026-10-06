---

description: "Task list for the User Registration API feature"
---

# Tasks: User Registration API

**Input**: Design documents from `specs/001-user-registration-api/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`,
`contracts/user-registration.openapi.yaml`, and `quickstart.md`

**Tests**: Included because the feature specification and project constitution
explicitly require JUnit 5, Mockito, MVC/component tests, and 100% JaCoCo
coverage.

**Organization**: Tasks are grouped by user story for incremental delivery.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish the Java 8 / Spring Boot service foundation in the
currently empty source workspace.

- [X] T001 [P] Create `pom.xml` for Java 8 and Spring Boot 2.7.18 with MVC, JPA, validation, Lombok, Flyway, H2, Actuator, springdoc-openapi-ui 1.8.0, JUnit 5, Mockito, and JaCoCo Maven plugin 0.8.13; do not add Spring Security
- [X] T002 [P] Create the Spring Boot entry point in `src/main/java/com/abs/user/UserRegistrationApplication.java`
- [X] T003 [P] Configure the local H2 datasource, Flyway, H2 console, and Actuator health/info exposure in `src/main/resources/application.yml`
- [X] T004 [P] Configure an isolated in-memory H2 test datasource and Flyway test execution in `src/test/resources/application-test.yml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create shared persistence and response foundations required by
every user story. Complete this phase before story implementation.

- [X] T005 Create the ANSI SQL ID-generator table and seed the users generator row in `src/main/resources/db/migration/V1__create_id_generator_table.sql`
- [X] T006 Create `users` in `src/main/resources/db/migration/V2__create_users_table.sql` with `id BIGINT` primary key, `employee_id VARCHAR(5)` unique/not null, `first_name VARCHAR(100)` not null, `last_name VARCHAR(100)` not null, `email_id VARCHAR(320)` unique/not null, and `is_active VARCHAR(1) DEFAULT 'Y' NOT NULL CHECK (is_active IN ('Y', 'N'))`; name constraints `uq_users_employee_id` and `uq_users_email_id` and use only ANSI SQL
- [X] T007 [P] Create shared `ApiEnvelope<T>` and `ApiError` response classes in `src/main/java/com/abs/user/dto/ApiEnvelope.java` and `src/main/java/com/abs/user/dto/ApiError.java`; support the confirmed `success`/`data`/`error` shape and prohibit persistence entities as response data
- [X] T008 Implement shared exception mapping in `src/main/java/com/abs/user/exception/GlobalExceptionHandler.java` and `src/main/java/com/abs/user/exception/BusinessValidationException.java`, including validation errors, `NoHandlerFoundException`, stable error codes, required error fields, and no stack traces or internal IDs in response bodies
- [X] T009 Reconcile the request-name maximum as 100 characters in `specs/001-user-registration-api/spec.md`, `specs/001-user-registration-api/data-model.md`, `specs/001-user-registration-api/plan.md`, and `specs/001-user-registration-api/contracts/user-registration.openapi.yaml`
- [X] T010 Define HTTP 400 with error code `INVALID_NAME_FOR_EMAIL` when name normalization empties a segment in `specs/001-user-registration-api/spec.md`, `specs/001-user-registration-api/contracts/user-registration.openapi.yaml`, and `specs/001-user-registration-api/data-model.md`

**Checkpoint**: Java build, isolated test profile, migrations, shared envelopes,
global errors, and API input rules are ready for story work.

---

## Phase 3: User Story 1 - Register a User (Priority: P1)

**Goal**: Create a user from valid names and return the complete public profile
in the standard success envelope without exposing the internal database ID.

**Independent Test**: Submit valid first/last names through the API and verify
HTTP 201, a five-digit string employee ID, base email, `active: true`, persisted
profile state, and no internal ID in the response.

### Tests for User Story 1

- [X] T011 [P] [US1] Add `UserRegistrationServiceTest` cases for successful persistence, a five-digit employee ID, base email generation, and INFO creation logging in `src/test/java/com/abs/user/service/UserRegistrationServiceTest.java`
- [X] T012 [P] [US1] Add `UserTest`, `UserDtoTest`, and `BooleanYnConverterTest` cases for required constructors, builders, getters, setters, and both `Y`/`N` conversion directions in `src/test/java/com/abs/user/entity/UserTest.java`, `src/test/java/com/abs/user/dto/UserDtoTest.java`, and `src/test/java/com/abs/user/entity/converter/BooleanYnConverterTest.java`
- [X] T013 [P] [US1] Add MockMvc success-contract cases for HTTP 201, the confirmed envelope/profile fields, and absence of `id` in `src/test/java/com/abs/user/controller/UserControllerTest.java`
- [X] T014 [P] [US1] Add the valid-create H2/Flyway end-to-end scenario in `src/test/java/com/abs/user/UserRegistrationIntegrationTest.java`

### Implementation for User Story 1

- [X] T015 [US1] Create the Lombok `User` JPA entity for `users` in `src/main/java/com/abs/user/entity/User.java` and `src/main/java/com/abs/user/entity/converter/BooleanYnConverter.java`, mapping `Long id` to the V1 table generator and mapping Boolean active state to SQL `Y`/`N` without serializing the entity
- [X] T016 [US1] Create `UserRepository` in `src/main/java/com/abs/user/repository/UserRepository.java` with `boolean existsByEmailId(String emailId)` and `boolean existsByEmployeeId(String employeeId)`
- [X] T017 [US1] Create `UserRegistrationRequest` and `UserResponse` in `src/main/java/com/abs/user/dto/UserRegistrationRequest.java` and `src/main/java/com/abs/user/dto/UserResponse.java`; expose only `userId`, `firstName`, `lastName`, `emailId`, and `active` in the response, with `userId` serialized as a five-digit JSON string
- [X] T018 [US1] Implement random employee ID generation in `src/main/java/com/abs/user/service/EmployeeIdGenerator.java` for values 10000 through 99999, returning exactly five digits and allowing collision retries
- [X] T019 [US1] Implement baseline user creation in `src/main/java/com/abs/user/service/UserRegistrationService.java`, persisting trimmed names and generated employee ID/base email, setting active true, returning a response DTO, emitting INFO on success, and never logging the internal ID or personal data
- [X] T020 [US1] Implement `POST /api/v1/users` in `src/main/java/com/abs/user/controller/UserController.java` with HTTP 201, `ApiEnvelope<UserResponse>`, and OpenAPI annotations; keep `contracts/user-registration.openapi.yaml` synchronized with the finalized public contract

**Checkpoint**: A valid registration can be demonstrated independently through
the controller and H2-backed persistence.

---

## Phase 4: User Story 3 - Reject Incomplete Registration (Priority: P1)

**Goal**: Reject omitted, null, empty, or whitespace-only names with HTTP 400
and the standard error envelope without persisting a profile.

**Independent Test**: Submit each invalid-name form and verify the error fields,
HTTP 400, the absence of the internal database ID, and no persisted row.

### Tests for User Story 3

- [X] T021 [US3] Extend `src/test/java/com/abs/user/controller/UserControllerTest.java` with omitted, null, empty, and whitespace-only first/last name cases and assertions for the complete error envelope
- [X] T022 [US3] Extend `src/test/java/com/abs/user/UserRegistrationIntegrationTest.java` with invalid-name requests that return HTTP 400 and leave the H2 users table unchanged

### Implementation for User Story 3

- [X] T023 [US3] Add `javax.validation` `@NotBlank` and the reconciled maximum-length constraint to `src/main/java/com/abs/user/dto/UserRegistrationRequest.java`; ensure `GlobalExceptionHandler` maps request validation failures into the confirmed error envelope

**Checkpoint**: Invalid registration requests are rejected consistently and do
not create profiles.

---

## Phase 5: User Story 2 - Resolve Duplicate Email Addresses (Priority: P2)

**Goal**: Generate unique normalized email addresses for repeated names,
including suffix rollover, without leaking internal IDs.

**Independent Test**: Seed the base address and successive suffixes, then
register matching names and verify that each request receives the first free
address and that concurrent inserts cannot persist duplicates.

### Tests for User Story 2

- [X] T024 [US2] Extend `src/test/java/com/abs/user/service/UserRegistrationServiceTest.java` for base collision to `a`, occupied `a` to `b`, progression through `c`, rollover from `z` to `aa`, normalized accented/punctuated names, WARN logging, employee-ID collisions, and recognized persistence-conflict retries
- [X] T025 [US2] Extend `src/test/java/com/abs/user/UserRegistrationIntegrationTest.java` for duplicate normalized names, unique persisted emails, concurrent registration conflicts, and distinct public employee IDs

### Implementation for User Story 2

- [X] T026 [US2] Implement name-to-email normalization and ordered suffix generation in `src/main/java/com/abs/user/service/UserRegistrationService.java`: trim and lowercase, convert accented letters to ASCII bases, remove whitespace/punctuation, then try base, `a` through `z`, and `aa`, `ab`, and subsequent alphabetic suffixes; follow the empty-normalization policy from `spec.md` and emit WARN on collision
- [X] T027 [US2] Implement constraint-specific unique-conflict retry in `src/main/java/com/abs/user/service/UserCreationAttempt.java` and `src/main/java/com/abs/user/service/UserRegistrationService.java`, using a fresh transaction per attempt, advancing the email only for `uq_users_email_id`, regenerating the employee ID only for `uq_users_employee_id`, and propagating unrelated persistence failures

**Checkpoint**: Duplicate and concurrent registrations produce unique email
and employee identifiers.

---

## Phase 6: Polish and Cross-Cutting Concerns

**Purpose**: Validate all stories, documentation, and the constitution's build
gate.

- [X] T028 [P] Align the contract, data model, and runnable scenarios with final decisions in `specs/001-user-registration-api/contracts/user-registration.openapi.yaml`, `specs/001-user-registration-api/data-model.md`, and `specs/001-user-registration-api/quickstart.md`
- [X] T029 Configure JaCoCo `check` in `pom.xml` to fail below 100% line and method coverage with no exclusions and 100% service-package branch coverage; run `mvn clean verify` and resolve uncovered production code in its owning source/test files
- [X] T030 Run the scenarios in `specs/001-user-registration-api/quickstart.md` and record that the valid create, validation error, duplicate suffix, internal-ID suppression, Actuator, and H2/Flyway startup expectations match the final contract

---

## Dependencies and Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; T001-T004 can start in parallel because
  they establish separate build, application, local configuration, and test
  configuration files.
- **Foundational (Phase 2)**: Depends on Setup. T005 precedes T006 because V2
  follows the V1 generator migration. T007 precedes T008. T009 and T010 must be
  resolved before implementing request validation or email normalization.
- **User Stories (Phase 3+)**: Depend on the foundational phase. User Story 1
  is the MVP. User Story 3 follows the shared endpoint/error foundation. User
  Story 2 builds on persisted profiles and the registration service from US1.
- **Polish (Phase 6)**: Depends on all three user stories.

### User Story Dependencies

- **US1 (P1)**: Starts after Foundation; delivers basic valid registration.
- **US3 (P1)**: Starts after US1 has a request/controller path; adds the invalid
  request behavior to the existing endpoint.
- **US2 (P2)**: Starts after US1 persistence and service exist; adds collision
  handling and constraint-specific retries.

### Parallel Opportunities

- Setup tasks T001-T004 can proceed in parallel; they edit separate files.
- In Foundation, V1 migration T005 and envelope DTO T007 can proceed in
  parallel; V2 T006 waits for T005 and exception advice T008 waits for T007.
- US1 test-authoring tasks T011-T014 can proceed in parallel because they use
  separate test files; implementation tasks T015, T017, and T018 can proceed
  in parallel after the tests and document decisions, while T016 waits for T015.
- US3 tasks T021 and T022 cannot run in parallel with US1 tests because they
  extend the same test files.
- US2 tests T024 and T025 can proceed in parallel after US1; T026 and T027
  share service behavior and should be implemented sequentially.
- Polish task T028 can proceed alongside JaCoCo configuration work only when
  the contract/data-model/quickstart files are not being edited by story tasks.

## Parallel Example: User Story 1

```text
After Phase 2 is complete, author these separate test files concurrently:
Task: T011 UserRegistrationServiceTest.java
Task: T012 UserTest.java and UserDtoTest.java
Task: T013 UserControllerTest.java
Task: T014 UserRegistrationIntegrationTest.java

After tests are prepared, implement the entity, DTOs, and employee ID generator
in their distinct files; then implement the dependent repository, service, and
controller in dependency order.
```

## Implementation Strategy

### MVP First (User Story 1)

1. Complete Setup and Foundation, including the two specification-reconciliation
   tasks.
2. Complete US1 tests and implementation.
3. Run the US1 independent test through MockMvc and H2.
4. Stop for review: the MVP creates valid users but does not yet promise
   collision suffix behavior or invalid-name handling.

### Incremental Delivery

1. Add US3 validation and error-envelope behavior; validate it independently.
2. Add US2 email/employee-ID collision handling and concurrency retries.
3. Run `mvn clean verify` with all JaCoCo thresholds enforced.
4. Complete final quickstart and contract review.

## Notes

- All tasks are unchecked and follow `- [ ] T### [P?] [US?] Description with file path`.
- `[P]` appears only where tasks edit independent files and have no incomplete-task dependency.
- Story labels map to the prioritized user stories in `spec.md`; setup, foundation,
  and polish tasks intentionally omit story labels.
- Tests are included because the specification explicitly requires them.
- The name-length and empty-normalization decisions are blocking prerequisites,
  not silently invented implementation behavior.
