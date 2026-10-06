<!--
Sync Impact Report
Version change: template (unratified) -> 1.0.0
Modified principles:
- [PRINCIPLE_1_NAME] -> Stable JSON API Contract
- [PRINCIPLE_2_NAME] -> Centralized Exception Handling
- [PRINCIPLE_3_NAME] -> Portable Persistence and Migrations
- [PRINCIPLE_4_NAME] -> Automated Test and Coverage Gates
- [PRINCIPLE_5_NAME] -> Documentation and Operational Observability
Added sections:
- Technology Stack and Security Boundary
- Quality and Change Workflow
Removed sections: None
Follow-up TODOs: None
-->
# Spring Boot REST API Constitution

## Core Principles

### I. Stable JSON API Contract

Every REST endpoint MUST accept and return strictly structured JSON using `application/json` where a body is present. Success and error payloads MUST use the same documented response envelope. The exact envelope shape MUST be defined in the OpenAPI contract before endpoint implementation so Angular and React clients can rely on a stable, framework-neutral contract. Error envelopes MUST include an ISO-8601 timestamp, HTTP status, stable error code, and descriptive message.

### II. Centralized Exception Handling

The service MUST use `@RestControllerAdvice` for consistent global exception-to-HTTP/JSON mapping, including validation failures such as `MethodArgumentNotValidException` and unmatched routes such as `NoHandlerFoundException`. Standard custom exceptions MUST represent expected domain failures, including resource-not-found and business-validation cases. Controllers and services MUST NOT return ad hoc error payloads or expose stack traces in API responses.

### III. Portable Persistence and Migrations

H2 in-memory storage MUST be the default database for local development and tests, with the H2 console enabled for local debugging. Flyway MUST own schema changes. Every migration MUST use standard, vendor-agnostic ANSI SQL and MUST avoid database-specific syntax so the schema can move to MySQL, PostgreSQL, or Oracle without rewriting migration scripts.

### IV. Automated Test and Coverage Gates

Service and controller unit tests MUST use JUnit 5 and Mockito. JaCoCo MUST enforce at least 100% line coverage and 100% method coverage in the build; coverage exclusions MUST NOT be used to weaken these thresholds. Component and integration tests MUST use `@SpringBootTest`, an isolated application context, and an in-memory test database configuration.

### V. Documentation and Operational Observability

Every endpoint MUST be represented in generated OpenAPI 3.0 documentation using `springdoc-openapi-ui`. Spring Boot Actuator MUST expose `/actuator/health` and `/actuator/info` for infrastructure monitoring. Application logs MUST use structured, parseable SLF4J logging through Lombok `@Slf4j`: INFO for business milestones and successful operations, WARN for handled exceptions, retries, and expected edge cases, and ERROR for unhandled exceptions and system failures. Log output MUST be suitable for aggregation by Splunk, Datadog, or ELK-compatible tooling.

## Technology Stack and Security Boundary

The service MUST use Java 8 and the latest available Spring Boot 2.7.x release. The selected Spring Boot patch version MUST be explicitly pinned in the build. Spring Boot 3 or later MUST NOT be adopted unless the Java baseline is separately changed through a constitution amendment. Lombok MUST be integrated for boilerplate reduction, including getters, setters, builders, and `@Slf4j` logging where applicable.

Authentication and authorization MUST remain outside this service and be provided by the external gateway/security team. Spring Security and other in-service authentication or authorization modules MUST NOT be added. Infrastructure exposure of the service and its monitoring endpoints MUST follow the external gateway/security team's controls.

## Quality and Change Workflow

Changes MUST preserve the documented JSON contract and migration portability. Pull requests MUST include relevant unit and component/integration tests, generated API documentation updates when endpoint contracts change, and a successful JaCoCo gate. Reviewers MUST verify this constitution's requirements before approval. A change that cannot meet a requirement MUST be proposed as a constitution amendment before implementation; tests or coverage rules MUST NOT be silently weakened.

## Governance

This constitution is the governing source for architectural and quality decisions in this project. Amendments MUST be reviewed and approved by the project owners, document the rationale and impact on existing services and clients, and update affected specifications and tests. Versioning follows semantic versioning: MAJOR for incompatible governance changes, MINOR for new or materially expanded requirements, and PATCH for clarifications or non-semantic corrections. Every review MUST assess compliance with the principles and constraints above; exceptions require an explicit amendment rather than an undocumented waiver.

**Version**: 1.0.0 | **Ratified**: 2026-10-05 | **Last Amended**: 2026-10-05
