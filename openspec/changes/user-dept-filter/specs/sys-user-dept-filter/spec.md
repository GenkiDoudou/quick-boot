## ADDED Requirements

### Requirement: User list search by department including descendants
The system SHALL allow filtering the user management page list by affiliation department. When a `deptId` is provided to user page or export queries, the system MUST include users whose `deptId` equals the selected department **or any descendant** of that department under `sys_dept.parentId` hierarchy. When `deptId` is absent or null, the system MUST NOT apply a department filter. Disabled (停用) descendant department IDs MUST still be included in the ID set so users assigned to those departments remain visible in the filter result. The HTTP API MUST continue to accept a single `deptId` (no new endpoint or permission code required for this capability).

#### Scenario: Filter includes child department users
- **WHEN** an authorized client requests user page (or export) with a parent `deptId` that has child departments containing users
- **THEN** the result includes users from the selected department and all descendant departments, and excludes users only in unrelated branches

#### Scenario: Leaf department behaves like exact match
- **WHEN** an authorized client requests user page with a leaf `deptId`
- **THEN** the result includes only users whose `deptId` equals that value

#### Scenario: No department filter
- **WHEN** an authorized client requests user page without a `deptId`
- **THEN** the result is not restricted by department affiliation

#### Scenario: Page and export use the same scope
- **WHEN** the same `deptId` (and other filters) are applied to user page and to user export
- **THEN** the set of users matched by the department condition is the same for both operations

### Requirement: User page department search control
The user management UI SHALL expose an 「归属部门」 tree select in the list search area, backed by the existing department treeselect data, clearable and resettable with other search fields. Selecting a department and searching MUST pass that `deptId` into the user page request. The independent department management page and the user create/edit department field MUST remain unchanged by this capability.

#### Scenario: Search and clear department
- **WHEN** an operator selects a department in user list search, searches, then clears or resets the search
- **THEN** the list first filters by that department (including descendants per backend rules), and after clear/reset no longer applies a department filter
