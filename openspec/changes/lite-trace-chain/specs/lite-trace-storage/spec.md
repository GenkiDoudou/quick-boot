## Purpose

Defines the shared `trace_index` and `trace_span` storage and projection semantics that power developer trace search and waterfall views across browser, API, and job sources.

## ADDED Requirements

### Requirement: Trace index stores one summary row per traceId
The system SHALL maintain a `trace_index` record keyed by `trace_id` including at least `root_source` (`browser`|`api`|`job`), `entry`, `caller`, `ok`, `duration_ms`, time bounds, and nullable `operation_id`, `action`, `page`, `uin`, `client_ip`, `error_summary`.

#### Scenario: Browser and pure API traces coexist
- **WHEN** both a browser-originated request and an OpenAPI request complete with distinct `trace_id` values
- **THEN** each appears as its own `trace_index` row with the correct `root_source` and `entry`

### Requirement: Trace spans store ordered chain fragments
The system SHALL store `trace_span` rows for each fragment with `trace_id`, `source` (`fe_action`|`fe_api`|`fe_error`|`gateway`|`service`|`sql`|`be_error`), `name`, `service`, timing fields, and optional `attrs`. Spans without a `trace_id` MUST NOT be inserted into `trace_span`.

#### Scenario: Waterfall load by traceId
- **WHEN** a client requests spans for a known `trace_id`
- **THEN** the system returns spans ordered by start time suitable for rendering a waterfall

### Requirement: Multi-source projection updates index and spans
Ingested frontend events and backend access/SQL/exception records that carry `trace_id` SHALL be projected into `trace_span` and SHALL upsert the corresponding `trace_index` summary (including failure/`error_summary` when errors occur).

#### Scenario: SQL fragment joins the same trace
- **WHEN** a SQL interceptor records a statement with the active `trace_id`
- **THEN** a `sql` span exists under that `trace_id` and is visible with other spans for the same id

### Requirement: Query keys supported by index
The storage layer SHALL support efficient lookup by `trace_id`, and filtered list queries by `operation_id`, `uin`, `entry`, `action`, and `root_source` within a time range (via indexes or equivalent).

#### Scenario: Lookup by operationId
- **WHEN** a query filters traces by `operation_id` and a time window
- **THEN** matching `trace_index` rows are returned without scanning unrelated spans first
