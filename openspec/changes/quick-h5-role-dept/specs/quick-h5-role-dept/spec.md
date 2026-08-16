## Purpose

Provides mobile (quick-h5) administration for system roles and departments: list/tree browsing, create/edit, role status toggle, and department delete against existing system APIs.

## ADDED Requirements

### Requirement: Workbench navigates to role and dept pages

The workbench「角色」and「部门」entries SHALL navigate to their H5 pages when paths are configured.

#### Scenario: Open role list

- **WHEN** the user taps 角色 and path is `/pages/system/role/index`
- **THEN** the app navigates to the role list page

#### Scenario: Open dept list

- **WHEN** the user taps 部门 and path is `/pages/system/dept/index`
- **THEN** the app navigates to the department list page

### Requirement: Role list with search, pagination, and status

The system SHALL list roles via `POST /sys/role/page` with optional name search, pull-to-refresh, and load-more. The list SHALL allow toggling status except for the built-in admin role (`roleId` equal to `1`).

#### Scenario: Search roles

- **WHEN** the user searches by role name
- **THEN** the list reloads from page 1 with that filter

#### Scenario: Toggle status

- **WHEN** the user toggles status for a non-admin role
- **THEN** the client calls `POST /sys/role/changeStatus`

#### Scenario: Admin role cannot be disabled

- **WHEN** the list shows role id `1`
- **THEN** disable control is unavailable for that role

### Requirement: Create and edit role

The system SHALL allow creating and editing roles with name, permission key, sort, status, and remark. Name and permission key MUST be required. For role id `1`, permission key MUST be read-only on edit.

#### Scenario: Create role

- **WHEN** the user submits a valid create form
- **THEN** the client calls `POST /sys/role/add` and returns to the list on success

#### Scenario: Edit role

- **WHEN** the user edits an existing role and saves
- **THEN** the client loads `GET /sys/role/{id}`, calls `POST /sys/role/update`, and returns to the list on success

### Requirement: Department tree list and delete

The system SHALL display departments from `GET /sys/dept/list` as an indented tree, support filtering by name, and allow delete with confirmation via `GET /sys/dept/remove/{id}`.

#### Scenario: Show tree

- **WHEN** the department list loads successfully
- **THEN** parent/child relationships are visible via indentation or equivalent hierarchy

#### Scenario: Delete department

- **WHEN** the user confirms deletion of a department
- **THEN** the client calls remove and refreshes the tree

### Requirement: Create and edit department with parent

The system SHALL allow creating and editing departments with parent department, name (required), sort, leader, phone, and status. Parent MAY be empty for root. The parent picker MUST NOT allow selecting the department being edited as its own parent.

#### Scenario: Create under parent

- **WHEN** the user creates a department with a selected parent
- **THEN** the client calls `POST /sys/dept/add` including that `parentId`

#### Scenario: Edit prevents self as parent

- **WHEN** the user edits a department
- **THEN** the parent options exclude at least the current department id

### Requirement: Auth and error handling

Role and department requests SHALL use the shared authenticated HTTP client. Auth expiry MUST redirect to login. Other failures MUST show an error toast.

#### Scenario: Unauthorized

- **WHEN** an API returns 401 / not-login
- **THEN** the client clears the session and relaunches login
