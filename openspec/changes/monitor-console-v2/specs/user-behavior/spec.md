## Purpose

Rebuild user journey analysis (page → action → next page) from Lite RUM events without restoring the retired clientTrack store.

## ADDED Requirements

### Requirement: Persist uin on RUM ingest when authenticated
When a logged-in user reports RUM events, the ingest path MUST persist the user identity on `sys_rum_event` and project it onto `sys_trace_index.uin` when creating/updating index rows for those events.

#### Scenario: Authenticated ingest stores uin
- **WHEN** an authenticated client posts a RUM batch containing pv/action events
- **THEN** stored rum events include a non-empty `uin` for that user and related trace index rows carry the same identity when projected

### Requirement: User behavior menu and query permission
The admin console MUST expose a dedicated「用户行为」menu entry gated by a query permission (e.g. `monitor:userBehavior:query`).

#### Scenario: Authorized user opens user behavior
- **WHEN** an operator with the user-behavior query permission opens the menu
- **THEN** the user-behavior page loads

### Requirement: List sessions for a user or session id
The system MUST provide an API to list recent sessions (or session summaries) filtered by `uin` / user name / `sessionId` and a time window.

#### Scenario: List sessions by uin
- **WHEN** an operator queries sessions for a given uin and time window
- **THEN** the response lists matching session identifiers with enough metadata to select one for timeline view

### Requirement: Timeline shows page and action sequence
Given a session (or equivalent scope), the system MUST return an ordered timeline of `pv` and `action` rum events showing page path, from-page, action label when present, and optional `traceId` / `operationId` for drill-down.

#### Scenario: Page then action then next page
- **WHEN** a session contains a pv, then an action, then a pv to another page
- **THEN** the timeline presents those nodes in time order reflecting page → action → next page

#### Scenario: Drill to request trace
- **WHEN** a timeline node includes a traceId and the operator chooses to open the request trace
- **THEN** the request-trace console opens focused on that traceId
