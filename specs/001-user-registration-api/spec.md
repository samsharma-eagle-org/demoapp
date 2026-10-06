# Feature Specification: User Registration API

**Feature Branch**: `001-user-registration-api`

**Created**: 2026-10-05

**Status**: Draft

**Input**: User description: "Specify a User Registration API that creates user profiles with generated employee IDs and unique email addresses, returns a consistent JSON response, and validates required names."

## Clarifications

### Session 2026-10-05

- Q: Should successful and error responses use the proposed `success`/`data`/`error` JSON envelope? → A: Yes; use the proposed `success`/`data`/`error` envelope.
- Q: Should `userId` be returned as a JSON number or a JSON string? → A: Return `userId` as a JSON string.
- Q: How should the service normalize names when generating email addresses? → A: Trim and lowercase each name, convert accented letters to ASCII, then remove spaces and punctuation.
- Q: If the base email and all `a`–`z` variants are taken, how should the service find another unique email? → A: Continue with two-or-more-letter suffixes: `aa`, `ab`, and so on.
- Q: What peak registration rate must the service support? → A: No feature-specific target for now; this is a test service.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Register a User (Priority: P1)

A frontend client submits a person's first and last name and receives a newly created user profile with a public employee identifier, generated email address, and active status.

**Why this priority**: Creating a user profile is the feature's primary value and the basis for the other scenarios.

**Independent Test**: Submit a valid pair of names and verify the success status and complete profile response without relying on other feature flows.

**Acceptance Scenarios**:

1. **Given** a valid first name and last name, **When** the client submits `POST /api/v1/users`, **Then** the service creates the profile and returns HTTP 201 with `userId`, `firstName`, `lastName`, `emailId`, and `active` inside the standard success envelope.
2. **Given** a successfully created profile, **When** the client inspects the response, **Then** `userId` is a five-digit employee identifier, `active` is `true`, and no internal database identifier is present.
3. **Given** names whose generated base email is not already used, **When** the profile is created, **Then** `emailId` is the lowercase `first.last@abs.com` form.

---

### User Story 2 - Resolve Duplicate Email Addresses (Priority: P2)

A client can register people with the same names without creating duplicate email addresses or failing registration.

**Why this priority**: Name collisions are expected in real user populations; automatic resolution keeps registration usable without requesting extra input.

**Independent Test**: Prepopulate the base email and successive variants, submit matching names, and verify that the first unused variant is returned and saved.

**Acceptance Scenarios**:

1. **Given** the base email is already in use, **When** a user with matching names is registered, **Then** the service assigns the first available alphabetic variant, beginning with `a`, and records a unique email.
2. **Given** the `a` and `b` variants are already in use, **When** another matching user is registered, **Then** the service assigns the `c` variant and returns a successful profile response.
3. **Given** concurrent registrations contend for the same email variant, **When** both requests complete, **Then** each successful profile has a distinct email address.

---

### User Story 3 - Reject Incomplete Registration (Priority: P1)

A client receives a clear, consistent error when either required name is missing or blank, and no incomplete profile is created.

**Why this priority**: Required-name validation protects profile quality and lets frontend clients correct input predictably.

**Independent Test**: Submit requests with each name omitted or blank and verify the error status, standard error envelope, and absence of a created profile.

**Acceptance Scenarios**:

1. **Given** the request omits `firstName` or `lastName`, **When** it is submitted, **Then** the service returns HTTP 400 with the standard error envelope containing a timestamp, status, error code, and descriptive message.
2. **Given** either name contains only whitespace, **When** the request is submitted, **Then** the service returns HTTP 400 with the standard error envelope and creates no profile.
3. **Given** a validation error response, **When** the client inspects the payload, **Then** it follows the same outer response envelope as successful responses and contains no internal database identifier.

### Edge Cases

- The base email and one or more alphabetic variants are already in use; the service selects the first available variant.
- If the base address and all single-letter variants are occupied, the service continues with `aa`, `ab`, and subsequent alphabetic combinations.
- A randomly generated employee identifier is already assigned; the service generates another five-digit value before completing registration.
- Leading or trailing whitespace in names is removed before deriving the email address; the response preserves the submitted letter casing for the names.
- Accented letters are converted to their unaccented ASCII base, and spaces and punctuation are removed from each name when deriving the email address.
- If either name produces an empty email segment after normalization, registration is rejected with HTTP 400 and error code `INVALID_NAME_FOR_EMAIL`; no profile is created.
- Simultaneous requests encounter the same candidate email or employee identifier; only unique values are persisted and returned.
- Missing, null, empty, or whitespace-only name values are rejected consistently.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The service MUST accept user registration at `POST /api/v1/users` with `firstName` and `lastName` as required, non-blank JSON string fields of at most 100 characters each.
- **FR-002**: For valid input, the service MUST create a profile and return HTTP 201 in the project's standard success envelope. The envelope MUST use `success`, `data`, and `error` fields; a successful response MUST set `success` to `true`, place the profile in `data`, and set `error` to `null`.
- **FR-003**: The profile in `data` MUST contain `userId`, `firstName`, `lastName`, `emailId`, and `active`. `userId` MUST be a JSON string containing a randomly generated five-digit employee identifier from 10000 through 99999; `active` MUST be `true` on creation.
- **FR-004**: The service MUST keep the internal database identifier private. It MUST NOT appear in success or error response bodies or be used as the public `userId`.
- **FR-005**: The service MUST trim and lowercase first and last names, convert accented letters to their unaccented ASCII bases, and remove spaces and punctuation in each email segment. It MUST generate the base email as `first.last@abs.com` and ensure that each persisted email is unique.
- **FR-006**: When the base email already exists, the service MUST try `first.a.last@abs.com`, then subsequent alphabetic suffixes in order, selecting the first unused email. After `z`, suffixes MUST continue as `aa`, `ab`, and so on.
- **FR-007**: The service MUST ensure that generated employee identifiers are unique. If a randomly generated identifier is already assigned, it MUST generate another value before completing registration.
- **FR-008**: The service MUST persist the submitted names, generated employee identifier, generated unique email, and active status for the created profile. The internal identifier MUST remain separate from the public employee identifier.
- **FR-009**: Missing, null, empty, whitespace-only, or email-normalized-to-empty first or last names MUST be rejected with HTTP 400. The normalized-to-empty case MUST use error code `INVALID_NAME_FOR_EMAIL`. Error responses MUST use the standard envelope with `success` set to `false`, `data` set to `null`, and `error` containing an ISO-8601 timestamp, HTTP status, stable error code, and descriptive message.
- **FR-010**: The service MUST log successful profile creation at INFO level and email collision handling at WARN level, following the project's logging guidelines. Logs MUST NOT expose the internal database identifier.

### Key Entities *(include if feature involves data)*

- **User Profile**: A registered person's profile, including first and last name, a private internal persistence identifier, a public five-digit employee identifier, a unique generated email address, and active status.
- **Employee Identifier**: A randomly generated five-digit value exposed as `userId`; it is distinct from and does not reveal the internal persistence identifier.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of valid registration requests in the acceptance suite produce a complete profile with all required fields, a five-digit public identifier, and active status.
- **SC-002**: 100% of duplicate-email cases in the acceptance suite produce a profile with the first available email variant and no duplicate address.
- **SC-003**: 100% of missing or blank name cases in the acceptance suite are rejected with the required error details and create no profile.
- **SC-004**: 0 client-visible success or error responses in the acceptance suite expose the internal database identifier.
- **SC-005**: Client applications can consistently distinguish successful registration from validation failure using the documented response envelope.
- **SC-006**: Feature acceptance for the test-service stage does not depend on a feature-specific throughput or response-time target; production targets are governed by the broader service SLO.
- **SC-007**: 100% of requests whose names normalize to an empty email segment are rejected with HTTP 400 and do not create a profile.

## Assumptions

- Name values are trimmed for persistence; submitted casing is preserved in the returned `firstName` and `lastName`, while email segments use the normalization rule in FR-005.
- Each submitted name is limited to 100 characters.
- Employee identifiers are generated as unique numeric values in the inclusive range 10000–99999 and serialized as JSON strings. A collision causes another random candidate to be generated.
- Only user registration is in scope; login, authentication, authorization, and user update or deletion flows are out of scope. Authentication and authorization remain the responsibility of the external gateway/security team.
- Profile records remain available for the lifetime of the configured in-memory database; this feature does not promise data retention across service restarts.
- No feature-specific performance target is required for the test-service stage; production performance targets are deferred until production expectations are defined.
