# Research: User Registration API

**Date**: 2026-10-05
**Scope**: Resolve implementation choices from the feature specification and
project constitution. The workspace has no pre-existing Java build or source
tree; this plan therefore includes the service foundation.

## Decisions

### Java and Spring baseline

- **Decision**: Use Java 8 and Spring Boot 2.7.18, with dependency versions
  pinned in Maven.
- **Rationale**: The constitution requires Java 8 and the latest Spring Boot
  2.7.x release. Boot 3.x is incompatible with the Java 8 baseline.
- **Alternatives considered**: Spring Boot 3.x was rejected because it requires
  a newer Java baseline and a separate constitution amendment.

### Validation and OpenAPI compatibility

- **Decision**: Use `javax.validation` and `springdoc-openapi-ui` 1.8.0.
- **Rationale**: Spring Boot 2.7 uses the `javax.validation` API family and the
  springdoc 1.x line. The springdoc 2.x line targets Spring Boot 3.
- **Alternatives considered**: `jakarta.validation` and springdoc 2.x were
  rejected for incompatibility with the selected Boot 2.7 runtime.

### Build system

- **Decision**: Use Maven and verify with `mvn clean verify`.
- **Rationale**: The workspace has no existing build manifest, and the request
  explicitly names the Maven verification command and Maven JaCoCo plugin.
- **Alternatives considered**: Gradle is viable but would add an unrequested
  build-system choice and different verification wiring.

### Portable internal ID generation

- **Decision**: Use JPA `GenerationType.TABLE` backed by an ANSI SQL generator
  table introduced in V1; V2 creates `users`.
- **Rationale**: SQL-standard native identity/sequence declarations are not
  implemented with the same syntax across all requested database products.
  A table-backed generator keeps DDL portable while still generating an
  internal persistence ID.
- **Alternatives considered**: `AUTO_INCREMENT`, `SERIAL`, native sequences,
  and identity columns were rejected because they require vendor-specific
  migration syntax or vary by database/version.

### User table DDL and constraints

- **Decision**: Use standard `BIGINT`, `VARCHAR`, `DEFAULT 'Y'`,
  `NOT NULL`, `CHECK`, primary key, and unique constraints. Map `is_active`
  `VARCHAR(1)` values `Y`/`N` to the Java Boolean property with a JPA attribute converter.
  Enforce uniqueness on both employee ID and email. Let the unique email
  constraint provide the lookup index rather than creating a duplicate index.
- **Rationale**: This keeps the entity and API Boolean while using a portable
  SQL representation supported by H2, MySQL, PostgreSQL, and Oracle.
- **Alternatives considered**: A separate non-unique email index duplicates
  the unique index. SQL `BOOLEAN` and `CHAR(1)` were not selected because the
  Java 8 / Hibernate 5 converter maps consistently to `VARCHAR(1)`.

### API and identifier representation

- **Decision**: Follow the clarified contract: shared `success`/`data`/`error`
  envelope and `userId` as a JSON string containing five digits.
- **Rationale**: The shape and type are explicitly settled in the clarification
  session. Separate response DTOs prevent internal ID serialization.
- **Alternatives considered**: Bare response payloads and numeric JSON IDs were
  not selected.

### Test-service performance scope

- **Decision**: Do not set a feature-specific throughput or latency target for
  the test service.
- **Rationale**: The user explicitly deferred performance requirements; a
  production target will follow the broader service SLO.
- **Alternatives considered**: An arbitrary rate target was rejected because
  the user has not provided expected production demand.

## Research Method

Decisions were resolved from the clarified feature specification, the project
constitution, and the current workspace structure. No external research agent
was dispatched; version-family and SQL-portability constraints are documented
as assumptions to verify when implementation dependencies and target database
versions are pinned.
