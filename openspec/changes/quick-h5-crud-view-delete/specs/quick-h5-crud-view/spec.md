## Purpose

Defines H5 read-only viewing for system and monitor list modules via form `mode=view` or lightweight read-only pages, without requiring edit permission.

## ADDED Requirements

### Requirement: Form pages support view mode

CRUD form pages that already exist for system modules SHALL accept `mode=view`. In view mode the page MUST load detail data, disable editable controls, hide save actions, and MUST NOT call create/update APIs.

#### Scenario: Open user form in view mode

- **WHEN** the user opens a user form URL with `mode=view`
- **THEN** fields are read-only and no save control is available

### Requirement: Lists expose a view entry gated by query or list

System and monitor list pages SHALL provide a view entry when the user has either `*:query` or `*:list` for that module. For modules with an existing form, view SHALL navigate with `mode=view`. For modules that already have a dedicated detail page, the existing detail navigation MAY remain. Modules without a form (job, login log, online, file) SHALL gain a lightweight read-only page.

#### Scenario: View from role list

- **WHEN** a user with role query or list permission taps view on a role row
- **THEN** the client opens the role form in view mode

#### Scenario: Monitor log detail unchanged

- **WHEN** the user opens detail from operlog, jobLog, or slowSql lists
- **THEN** the existing detail page is used
