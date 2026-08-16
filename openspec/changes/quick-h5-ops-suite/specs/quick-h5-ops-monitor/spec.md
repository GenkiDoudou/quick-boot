## Purpose

Provides mobile (quick-h5) monitoring operations for scheduled jobs, job logs, login and operation logs, online users, and slow SQL against existing monitor APIs, with workbench menus and permission-gated actions.

## ADDED Requirements

### Requirement: Job list with status change and run-once

The H5 client SHALL list jobs via `GET /monitor/job/list` and allow permitted users to change job status and run a job once via existing APIs. The client MUST NOT provide mobile Cron or invoke-target editors for creating or fully editing job definitions in phase one.

#### Scenario: Enable or disable job

- **WHEN** the user with permission toggles job status
- **THEN** the client calls `/monitor/job/changeStatus` and the list reflects the new status

#### Scenario: Run job once

- **WHEN** the user with permission confirms run once
- **THEN** the client calls `/monitor/job/run` and shows a success or error toast

#### Scenario: No Cron editor on H5

- **WHEN** the user views the job module on H5
- **THEN** there is no UI to edit Cron expressions or create full job definitions

### Requirement: Navigate from job to job log

The job list SHALL offer navigation to the job log list with a filter such as job name (or equivalent query) so operators can inspect recent runs.

#### Scenario: Open job logs for a job

- **WHEN** the user taps the log action on a job row
- **THEN** the app opens `/pages/monitor/jobLog/index` with a filter related to that job

### Requirement: Job log list, detail, delete, and clean

The H5 client SHALL list job logs, show detail for a selected log, and allow delete and clean when permitted. Export MUST NOT be required.

#### Scenario: View job log detail

- **WHEN** the user opens a job log row
- **THEN** the client loads detail and shows key fields including result and message as available

### Requirement: Login log list with delete, clean, and unlock

The H5 client SHALL page login logs via `POST /monitor/logininfor/page` and allow remove, clean, and unlock for permitted users. Export MUST NOT be required.

#### Scenario: Unlock locked user from login log

- **WHEN** the user with unlock permission confirms unlock for a username
- **THEN** the client calls the existing unlock API and shows a success or error toast

### Requirement: Operation log list, detail, delete, and clean

The H5 client SHALL page operation logs via `POST /monitor/operlog/page`, show detail for a selected row, and allow delete and clean when permitted. Long text fields MAY be collapsible. Export MUST NOT be required.

#### Scenario: View operlog detail

- **WHEN** the user opens an operation log
- **THEN** the client shows detail including identity, IP, and timing fields as returned by the API

### Requirement: Online user list and force logout

The H5 client SHALL list online users via `GET /monitor/online/list` and allow force logout with a confirmation step for permitted users.

#### Scenario: Force logout with confirmation

- **WHEN** the user confirms force logout for a session
- **THEN** the client posts to `/monitor/online/forceLogout` with the session token id and refreshes the list on success

### Requirement: Slow SQL list, detail, and delete

The H5 client SHALL list slow SQL records, show detail including SQL text, and allow delete when permitted.

#### Scenario: View slow SQL detail

- **WHEN** the user opens a slow SQL row
- **THEN** the client shows the SQL text and related metrics from the detail API

### Requirement: Monitor ops menus and permission gating

Flyway or equivalent seed MUST add H5 workbench menu entries under a monitor group with `path` values starting with `/pages/monitor/` for the monitor ops pages, reuse existing PC monitor permission strings for F nodes, and grant them to the admin role. Write actions MUST be hidden without permission.

#### Scenario: Admin sees monitor ops tiles

- **WHEN** an admin with seeded menus opens the workbench
- **THEN** tiles for job, job log, login log, operlog, online, and slow SQL are available according to role menus

#### Scenario: Unauthorized force logout hidden

- **WHEN** a user lacks online force-logout permission
- **THEN** the force logout control is not shown
