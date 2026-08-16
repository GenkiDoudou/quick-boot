## Purpose

Defines the developer-facing console for lite trace chain: Trace-first waterfall (A′) and query console (E), without requiring operational KPI dashboards.

## ADDED Requirements

### Requirement: Trace list and waterfall console (A′)
The console SHALL provide a Trace list showing `trace_id`, source badge (`browser`|`api`|`job`), success/failure, `entry`, duration, and caller, with filters for source. Selecting a trace SHALL show header context and a span waterfall ordered by time.

#### Scenario: Filter pure API traces
- **WHEN** the user filters source to pure API
- **THEN** only `root_source=api` traces appear in the list

#### Scenario: Open waterfall
- **WHEN** the user selects a trace
- **THEN** spans for that `trace_id` render as a timed waterfall including available fe/gateway/service/sql/error fragments

### Requirement: Query console (E)
The console SHALL provide a command-style query that accepts at least `traceId:`, `operationId:`, `uin:`, `page:`, and `action:` terms, returns matching trace summaries, and allows expanding a chain dump of spans.

#### Scenario: Query by traceId
- **WHEN** the user runs `traceId:<id>` for an existing trace
- **THEN** the result list includes that trace and expanding it shows a textual chain dump

#### Scenario: Query by operationId
- **WHEN** the user runs `operationId:<id>` within available data
- **THEN** traces linked to that operation are listed

### Requirement: No dependency on health-score dashboard
The A′ and E consoles MUST be usable without a PV/health-score overview page. Operational KPI dashboards are out of scope for this capability's required UI.

#### Scenario: Navigate directly to Trace console
- **WHEN** a developer opens the Trace console entry
- **THEN** they can search and inspect chains without visiting a health-score overview
