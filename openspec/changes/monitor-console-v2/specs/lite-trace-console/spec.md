## Purpose

Improve the request-trace (Lite Trace) console so engineers can filter by time and outcome and inspect richer span details with related-log jumps.

## ADDED Requirements

### Requirement: Trace list supports time range filter
The system MUST allow filtering `trace_index` list results by a begin/end time window applied to trace start time. Absent an explicit window, the UI MUST default to a recent window (e.g. last 24 hours) that is sent to the API.

#### Scenario: Filter by time window
- **WHEN** an operator sets begin and end times and refreshes the request-trace list
- **THEN** only traces whose start time falls in that window are returned

### Requirement: Trace list supports success/failure filter
The system MUST allow filtering the request-trace list by outcome: all, success (`okFlag` success), or failure (`okFlag` failure).

#### Scenario: Show only failures
- **WHEN** an operator selects the failure outcome filter
- **THEN** the list contains only traces marked unsuccessful

### Requirement: Trace detail header exposes index fields
When a trace is selected, the detail header MUST present the available index fields needed for triage (at least traceId, operationId, uin, root source, entry/caller, page/fromPage/action, ok/status, duration, times, client IP, UA, error summary when present).

#### Scenario: Header shows key index fields
- **WHEN** an operator selects a trace with populated index fields
- **THEN** those fields are visible in the detail header without requiring a separate dump page

### Requirement: Span rows can expand attributes
The system MUST allow expanding a span row to show status/ok and parsed `attrsJson` key-values (or an explicit empty state when attributes are absent).

#### Scenario: Expand span with SQL attrs
- **WHEN** an operator expands a span that stores SQL or URL attributes in attrs
- **THEN** those attributes are displayed in a readable form under the span row

### Requirement: Related navigation from a trace
When identifiers are present, the UI MUST offer navigation to related slow-SQL and operation-log views (via query parameters) and a way to list other traces sharing the same `operationId`.

#### Scenario: Jump to slow SQL by traceId
- **WHEN** an operator opens related slow SQL for a trace that has a traceId
- **THEN** the slow-SQL list is opened (or filtered) using that traceId
