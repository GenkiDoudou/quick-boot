## Purpose

Defines backend obligations to create or propagate `traceId` and to emit access, SQL, and exception data that project into the shared trace chain for non-browser callers as well.

## ADDED Requirements

### Requirement: HTTP entry creates or propagates traceId
For inbound HTTP requests the backend SHALL read a trace correlation header if present, otherwise generate a `trace_id`, and MUST make that id available to downstream logging and interceptors for the request lifetime.

#### Scenario: Incoming header reused
- **WHEN** a request arrives with `X-Trace-Id` (or configured equivalent)
- **THEN** access logs and projected spans for that request use the same id

#### Scenario: Missing header generates id
- **WHEN** a request arrives without a trace header
- **THEN** the server generates a `trace_id` used for that request's access/SQL/exception projections

### Requirement: Access, SQL, and exceptions project as spans
Backend access handling SHALL project a `gateway` or `service` span; SQL instrumentation SHALL project a `sql` span when `trace_id` is present; exception handlers SHALL project a `be_error` span and update the trace summary toward failure.

#### Scenario: Failed API with SQL
- **WHEN** a traced request executes SQL and then throws
- **THEN** the trace contains service/sql/be_error spans under one `trace_id` and `trace_index.ok` reflects failure

### Requirement: Pure API and job roots are supported
OpenAPI or partner API traffic and scheduled jobs SHALL be able to create traces with `root_source` of `api` or `job`, setting `entry` and `caller` without requiring frontend `page`/`action` fields.

#### Scenario: Job trace without UI fields
- **WHEN** a scheduled job runs with a generated `trace_id`
- **THEN** a `trace_index` row exists with `root_source=job` and nullable page/action fields
