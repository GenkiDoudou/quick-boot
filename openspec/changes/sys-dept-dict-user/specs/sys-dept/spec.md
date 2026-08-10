## ADDED Requirements

### Requirement: Department tree list and treeselect
The system SHALL expose department management under `/sys/dept`. Authorized users MUST be able to list departments as a tree (`GET /sys/dept/list` with optional name/status filters) and load an enabled-only tree for selectors (`GET /sys/dept/treeselect`).

#### Scenario: List department tree
- **WHEN** an authorized user calls `GET /sys/dept/list`
- **THEN** the response is a tree of departments ordered for display

#### Scenario: Treeselect enabled only
- **WHEN** an authorized user calls `GET /sys/dept/treeselect`
- **THEN** only enabled departments are returned in tree form

### Requirement: Department CRUD with OauthClient-style paths
The system SHALL support create (`POST /sys/dept/add`), update (`POST /sys/dept/update`), detail (`GET /sys/dept/{id}`), single delete (`GET /sys/dept/remove/{id}`), and batch delete (`POST /sys/dept/remove`). Service code MUST explicitly assign writable fields and defaults. Deleting a department that still has child departments OR has users with that `deptId` MUST fail.

#### Scenario: Reject delete when users bound
- **WHEN** a user attempts to delete a department that still has users referencing it
- **THEN** the operation fails with a business error and the department remains

#### Scenario: Add department under parent
- **WHEN** an authorized user posts a valid department under an existing parent (or root `0`)
- **THEN** the department is persisted and the new id is returned

### Requirement: Department sync import export
The system SHALL provide sync Excel export (`POST /sys/dept/export`) and import (`GET /sys/dept/import/template`, `POST /sys/dept/import` with `updateSupport`), following the OauthClient import/export contract (ids-first export, 5000-row cap, partial success with error detail file).

#### Scenario: Export departments
- **WHEN** an authorized user posts export with optional ids or filters
- **THEN** an xlsx of flat department rows is downloaded
