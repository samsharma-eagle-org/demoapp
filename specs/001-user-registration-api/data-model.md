# Data Model: User Registration API

## User

Represents a registered person. The persistence entity is not serialized as an
API response; `UserResponse` is an explicit projection.

| Field | API name | Type | Required / constraint | Notes |
| --- | --- | --- | --- | --- |
| Internal ID | Not exposed | `Long` / SQL `BIGINT` | Primary key, generated | JPA table-backed generator; never returned or logged |
| Employee ID | `userId` | String / SQL `VARCHAR(5)` | Required, unique, exactly five digits | Random numeric candidate from 10000 through 99999, serialized as a JSON string |
| First name | `firstName` | String / SQL `VARCHAR(100)` | Required, non-blank, max 100 characters | Trimmed for persistence; response preserves submitted casing |
| Last name | `lastName` | String / SQL `VARCHAR(100)` | Required, non-blank, max 100 characters | Trimmed for persistence; response preserves submitted casing |
| Email address | `emailId` | String / SQL `VARCHAR(320)` | Required, unique | Lowercase normalized segments and progressive alphabetic collision suffix |
| Active state | `active` | Boolean / SQL `VARCHAR(1)` | Required, default `Y`; allowed values `Y` or `N` | JPA converter maps `Y` to true and `N` to false; API remains Boolean |

## ID Generator

The internal ID allocation table is persistence infrastructure, not a public
entity or API resource.

| Field | Type | Constraint | Notes |
| --- | --- | --- | --- |
| `generator_name` | `VARCHAR(50)` | Primary key | Generator key for the users table |
| `next_value` | `BIGINT` | Not null | Current allocation state consumed by JPA `GenerationType.TABLE` |

Seed the `users` generator row in V1. Configure the entity generator name and
column mappings to match this table exactly. The internal value must never be
copied into `userId`, `UserResponse`, success/error envelopes, or logs.

## Email Normalization and Uniqueness

1. Trim each name for persistence and email derivation.
2. For email segments, lowercase, convert accented letters to unaccented ASCII
   bases, and remove whitespace and punctuation.
3. Build `first.last@abs.com`.
4. If occupied, try `first.a.last@abs.com` through `first.z.last@abs.com` in
   order, followed by `first.aa.last@abs.com`, `first.ab.last@abs.com`, and
   subsequent alphabetic suffixes.
5. Enforce uniqueness in the database. An existence check is an optimization,
   not a substitute for the unique constraint during concurrent requests.

## Relationships and State

There are no relationships to other entities in this feature. A profile is
created with `active=true`; update/deactivation lifecycle behavior is out of
scope. The in-memory database retains rows only for the configured database
lifetime.

## Persistence Constraints

- `users.id` is the primary key and is distinct from `employee_id`.
- `employee_id` and `email_id` have unique constraints.
- `first_name`, `last_name`, and `email_id` are not null.
- `is_active VARCHAR(1)` is not null, defaults to `Y`, and has a check constraint restricting values to `Y` or `N`; JPA maps it to the Boolean entity property.
- Use ANSI SQL in both Flyway migrations. SQL `BOOLEAN` portability to Oracle
   is not required; the portable `VARCHAR(1)` representation supports the target
   database families without vendor-specific SQL.
