## Purpose

Provide a single read-only hub to search operation logs, login logs, and slow SQL with shared dimension filters without replacing the existing detail menus.

## ADDED Requirements

### Requirement: Log hub menu and query permission
The admin console MUST expose a「日志中心」menu entry gated by a read permission (e.g. `monitor:logHub:query`). Destructive clean/delete MUST remain on the original detail menus' permissions.

#### Scenario: Open log hub
- **WHEN** an operator with log-hub query permission opens the menu
- **THEN** the log-hub page loads

### Requirement: Unified log row model
Each aggregated row MUST identify `source` as one of `oper`, `login`, or `slow_sql`, and include occurred time, title/summary, actor when available, normalized status, original `refId`, and optional `traceId` / `operationId`.

#### Scenario: Mixed sources in one list
- **WHEN** the operator queries without restricting source
- **THEN** the result may include rows from all three sources using the unified fields

### Requirement: Dimension filters
The log hub MUST support filtering by time range, source multi-select, actor/user, keyword, success/failure, and optional `traceId`.

#### Scenario: Filter by source and user
- **WHEN** an operator selects sources `oper` and `login` and a user name
- **THEN** only matching rows from those sources for that user are returned (within the approximate merge rules)

### Requirement: Approximate merge pagination is disclosed
Phase-one aggregation MAY merge per-source limited windows in memory. Responses MUST indicate that pagination is approximate (e.g. `approximate=true`) so clients do not assume exact global offsets.

#### Scenario: Response marks approximate paging
- **WHEN** the log-hub list API returns a page
- **THEN** the payload indicates approximate pagination

### Requirement: Navigate to original detail
Selecting a row MUST allow opening the corresponding original monitor page (operation log, login log, or slow SQL) using the row's source and `refId` (or equivalent query).

#### Scenario: Open slow SQL from hub row
- **WHEN** an operator opens a `slow_sql` row from the hub
- **THEN** the slow-SQL detail or list view is reached with that record identifiable
