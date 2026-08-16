## Purpose

Retire the monitor overview product surface so operators rely on BI for dashboards and on Lite Trace / ops menus for troubleshooting.

## ADDED Requirements

### Requirement: Overview menu is disabled
The system MUST disable the monitor overview menu entries (including query button children) so they no longer appear for authorized roles after migration.

#### Scenario: Menu hidden after migrate
- **WHEN** Flyway migration for overview retirement has been applied
- **THEN** overview menu items are inactive (`status` disabled) and are not shown in the admin sidebar

### Requirement: Overview APIs and UI are removed
The system MUST remove the monitor overview HTTP APIs and the admin overview page/API client so no supported client can load overview summary/trends from this application.

#### Scenario: Overview endpoint gone
- **WHEN** a client calls the former overview summary or trends path
- **THEN** the request is not handled by an overview controller (404 or equivalent routing miss)

#### Scenario: Overview page assets gone
- **WHEN** an operator or deep link targets the former overview component path
- **THEN** no overview Vue page or `overview` API module remains in the admin frontend source tree
