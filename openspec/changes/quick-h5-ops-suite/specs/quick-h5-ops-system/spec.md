## Purpose

Provides mobile (quick-h5) system operations for parameters, dictionaries, OAuth clients, file classifies, and files against existing backend APIs, with workbench menu entries and permission-gated actions.

## ADDED Requirements

### Requirement: Parameter config list and CRUD

The H5 client SHALL provide a paginated parameter list via `POST /sys/config/page`, support search, create, edit, delete, and cache refresh using the same endpoints as PC. Write actions MUST be hidden when the user lacks the corresponding permissions.

#### Scenario: Search and paginate configs

- **WHEN** the user opens the parameter list and submits a keyword
- **THEN** the list reloads from page 1 filtered by that keyword and supports load-more when more rows exist

#### Scenario: Create or update config

- **WHEN** the user submits a valid create or edit form with name, key, and value
- **THEN** the client calls the add or update API and returns to the list on success

#### Scenario: Refresh config cache

- **WHEN** the user with refresh permission taps refresh cache
- **THEN** the client calls the existing refresh endpoint and shows a success or error toast

### Requirement: Dictionary type and data management

The H5 client SHALL list dictionary types, allow type CRUD and cache refresh, and navigate into a data list for a selected `dictType` where data items can be created, edited, and deleted. Excel import/export MUST NOT be offered.

#### Scenario: Open dict data from type

- **WHEN** the user taps a type row to manage data
- **THEN** the app navigates to the data list scoped by that `dictType`

#### Scenario: Create dict data

- **WHEN** the user submits a valid data form under a type
- **THEN** the client persists via the dict data add API and refreshes the data list

### Requirement: OAuth client list and edit with masked secret

The H5 client SHALL list OAuth clients via `POST /sys/oauthclient/page`, support create/update and status changes for permitted users, and show `clientSecret` masked by default on detail with an explicit reveal action.

#### Scenario: Secret masked by default

- **WHEN** the user opens client detail
- **THEN** the secret is displayed masked until the user explicitly reveals it

#### Scenario: Toggle client status

- **WHEN** the user with permission changes client status
- **THEN** the client calls the existing update or status API and the list reflects the new status

### Requirement: File classify CRUD with immutable key on edit

The H5 client SHALL list file classifies and allow create, edit, and delete. On edit, the `classify` key MUST be read-only. Fields MUST include the subset needed for upload rules (name, extensions, size limit, status as applicable).

#### Scenario: Edit keeps classify key read-only

- **WHEN** the user opens edit for an existing classify
- **THEN** the classify key cannot be changed and save uses the update API

### Requirement: File list, upload with classify, preview, download, delete

The H5 client SHALL list files via `GET /system/file/list`, allow upload only after selecting a classify, and support preview, download, and delete using the existing authenticated file APIs. Import/export of file metadata MUST NOT be required.

#### Scenario: Upload requires classify

- **WHEN** the user attempts upload without selecting a classify
- **THEN** the client blocks the request and prompts to select a classify

#### Scenario: Upload with classify succeeds

- **WHEN** the user selects a classify and a file and confirms upload
- **THEN** the client posts to `/system/file/upload/{classify}` and refreshes the list on success

#### Scenario: Delete file

- **WHEN** the user with delete permission confirms delete
- **THEN** the client calls the delete API and removes the row from the list on success

### Requirement: System ops menus and permission gating

Flyway or equivalent seed MUST add H5 workbench menu entries whose `path` values start with `/pages/system/` for the system ops pages, reuse existing PC button permission strings for F nodes, and grant them to the admin role. List write actions MUST use `hasPermi` (or equivalent) so unauthorized users do not see those controls.

#### Scenario: Admin sees system ops tiles

- **WHEN** an admin with seeded menus opens the workbench
- **THEN** tiles for config, dict, oauth client, file, and file classify are available according to role menus

#### Scenario: Unauthorized write hidden

- **WHEN** a user lacks a write permission such as config add
- **THEN** the corresponding add control is not shown on the H5 page
