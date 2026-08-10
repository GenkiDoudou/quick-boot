## ADDED Requirements

### Requirement: Persist login access logs on login outcomes
The system SHALL write a `sys_logininfor` row on login success and login failure, including username, client id when available, IP, approximate location when available, OS/browser summary when available, status, message, and login time. Failure messages related to lockout MUST remain consistent with `LoginLockSupport` behavior.

#### Scenario: Successful login creates log row
- **WHEN** a user logs in successfully
- **THEN** a login log row with success status is persisted

#### Scenario: Failed login creates log row
- **WHEN** a user fails authentication (wrong password, captcha, or locked)
- **THEN** a login log row with failure status and descriptive message is persisted

### Requirement: Login log management APIs
The system SHALL expose management under `/monitor/logininfor`: page (`POST /monitor/logininfor/page`), batch remove (`POST /monitor/logininfor/remove`), clean all (`POST /monitor/logininfor/clean`), sync export (`POST /monitor/logininfor/export` with `@IgnoreLogger(RESULT)`), and unlock (`GET /monitor/logininfor/unlock/{userName}`). Page filters MUST support IP, username, client id, status, and login time range.

#### Scenario: Page login logs
- **WHEN** an authorized user posts page with username or status filter
- **THEN** matching login log rows are returned with total count

#### Scenario: Unlock clears login lock cache
- **WHEN** an authorized user calls unlock for a username that is locked
- **THEN** the login failure lock cache for that username is cleared via the existing lock support

#### Scenario: Clean all login logs
- **WHEN** an authorized user confirms clean
- **THEN** all login log rows are removed
