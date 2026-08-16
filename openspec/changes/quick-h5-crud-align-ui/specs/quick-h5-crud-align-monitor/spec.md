## Purpose

Aligns quick-h5 monitor list pages with practical search and card field coverage from corresponding quick-ui monitor screens, without adding Cron editors or export flows.

## ADDED Requirements

### Requirement: Job and job log practical alignment

The job list SHALL support name keyword and status filtering, and card fields SHALL include group, Cron, and invoke target summaries when available. Job log list SHALL support job name and status filtering and show result message fields. The client MUST NOT provide mobile Cron editing.

#### Scenario: Filter jobs by status

- **WHEN** the operator filters jobs by status
- **THEN** the list reloads with that status applied

#### Scenario: No Cron editor

- **WHEN** the operator opens the job module on H5
- **THEN** there is no UI to edit Cron expressions

### Requirement: Login, operlog, online, slow SQL alignment

Login log, operation log, online user, and slow SQL lists SHALL support practical keyword search and status or equivalent filters where the API allows, and cards SHALL expose additional key fields (location/browser, cost/method, dept/IP, cost/type/operator as applicable). Detail pages for operlog and slow SQL SHALL remain available for long text.

#### Scenario: Operlog keyword search

- **WHEN** the operator searches operation logs by title keyword
- **THEN** matching rows are listed

#### Scenario: Online force logout still confirmed

- **WHEN** the operator force-logs out a session
- **THEN** a confirmation step is required before the API call
