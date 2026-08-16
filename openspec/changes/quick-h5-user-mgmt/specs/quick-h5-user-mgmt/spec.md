## Purpose

Provides mobile (quick-h5) user administration for listing, creating, editing, enabling/disabling, and resetting passwords against the existing system user APIs.

## ADDED Requirements

### Requirement: Workbench navigates to user list

The workbench「用户」entry SHALL navigate to the H5 user list page when a path is configured. If no path is configured, the client MAY show a placeholder toast.

#### Scenario: Configured path opens list

- **WHEN** the user taps 用户 on the workbench and the menu item has path `/pages/system/user/index`
- **THEN** the app navigates to the user list page

#### Scenario: Missing path keeps placeholder

- **WHEN** the user taps a workbench item without a path
- **THEN** the app shows a non-blocking toast and does not navigate

### Requirement: User list with search and pagination

The system SHALL display a paginated user list loaded from `POST /sys/user/page`, supporting search by user account name, pull-to-refresh, and load-more.

#### Scenario: Search by account

- **WHEN** the user submits an account keyword
- **THEN** the list reloads from page 1 filtered by that account name

#### Scenario: Load more

- **WHEN** the user reaches the end of the list and more records exist
- **THEN** the next page is appended

#### Scenario: No more data

- **WHEN** the user reaches the end and no further records exist
- **THEN** the client indicates there is no more data

### Requirement: Create and edit user with simplified fields

The system SHALL allow creating and editing users with fields: account, nickname, password (create only, optional), phone, and status. Account and nickname MUST be required. Create MUST require selecting at least one role. Edit MUST keep the account read-only, MUST NOT show a role editor, MUST resubmit the existing `roleIds` from detail, and MUST NOT send `deptId`.

#### Scenario: Create user

- **WHEN** the user submits a valid create form including at least one role
- **THEN** the client calls `POST /sys/user/add` and returns to the list on success

#### Scenario: Empty password on create

- **WHEN** the user creates a user without a password (with valid roles)
- **THEN** the backend applies its default password and the create still succeeds (subject to other validation)

#### Scenario: Edit user preserves roles

- **WHEN** the user opens edit for an existing user and saves nickname, phone, or status
- **THEN** the client loads detail via `GET /sys/user/{id}`, submits `POST /sys/user/update` with the detail's `roleIds` and without `deptId`, and returns to the list on success

### Requirement: Change status and reset password from list

The list SHALL allow changing user status and resetting password. The built-in super admin (`userId` equal to `1`) MUST NOT be disableable from the client UI.

#### Scenario: Toggle status

- **WHEN** the user toggles status for a non-super-admin user
- **THEN** the client calls `POST /sys/user/changeStatus` with `userId` and `status` (`0` normal / `1` disabled)

#### Scenario: Super admin cannot be disabled

- **WHEN** the list renders the super admin user
- **THEN** disable control is unavailable for that user

#### Scenario: Reset password

- **WHEN** the user enters a non-empty new password in the reset dialog and confirms
- **THEN** the client calls `POST /sys/user/resetPwd` and shows a success toast

### Requirement: Auth and error handling

All user-management requests SHALL use the shared authenticated HTTP client. On auth expiry the client MUST redirect to login. Other API failures MUST surface an error toast with the server or client message.

#### Scenario: Unauthorized

- **WHEN** a user API returns 401 / not-login
- **THEN** the client clears the session and relaunches the login page

#### Scenario: Business error toast

- **WHEN** a user API returns a non-success business code
- **THEN** the client shows a toast with the error message and does not navigate away unexpectedly
