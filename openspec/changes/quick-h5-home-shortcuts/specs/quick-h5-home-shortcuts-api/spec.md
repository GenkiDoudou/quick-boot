## Purpose

Defines server-side storage and GET/POST APIs for per-user H5 home shortcuts, including default resolution and permission-safe candidate pools.

## ADDED Requirements

### Requirement: Persist per-user home shortcuts

The system SHALL store each user's selected home shortcut menu ids and display order in a dedicated table. Absence of rows for a user SHALL mean the user has not customized shortcuts and defaults apply.

#### Scenario: First-time user has no preference rows

- **WHEN** a user has never saved home shortcuts
- **THEN** the system treats them as uncustomized and resolves the default shortcut list

### Requirement: Resolve final shortcuts with permission filter

When returning home shortcuts, the system SHALL intersect the user's preference (or system defaults) with the same authorized H5 page menu set used for the workbench (type C, path starting with `/pages/`, visible). Unauthorized or missing menus MUST be omitted. The result MUST contain at most 8 items.

#### Scenario: Default list filtered by permission

- **WHEN** an uncustomized user requests home shortcuts
- **THEN** only default menu ids that remain in their authorized H5 candidate set are returned, ordered, up to 8

#### Scenario: Lost permission drops a saved shortcut

- **WHEN** a customized user loses access to a previously saved menu
- **THEN** that menu MUST NOT appear in the returned home shortcuts

### Requirement: List candidates and save via GET/POST only

The system SHALL provide a GET endpoint for the current user's final home shortcuts, a GET endpoint for the flat candidate pool, and a POST save endpoint that replaces the user's preference with the submitted ordered `menuIds`. Save MUST reject ids outside the candidate pool or more than 8 ids. An empty `menuIds` array MUST clear preference rows so defaults apply again. The system MUST NOT require PUT or DELETE for this feature.

#### Scenario: Save valid selection

- **WHEN** a logged-in user POSTs up to 8 authorized menu ids
- **THEN** subsequent GET home shortcuts returns that ordered set (still permission-filtered)

#### Scenario: Restore defaults with empty array

- **WHEN** a user POSTs `menuIds` as an empty array
- **THEN** preference rows for that user are removed and GET home shortcuts returns the default resolution

#### Scenario: Reject over-limit or unauthorized ids

- **WHEN** a user POSTs more than 8 ids or any id not in their candidate pool
- **THEN** the system rejects the save with a business error and does not partially apply
