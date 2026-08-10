## ADDED Requirements

### Requirement: System config CRUD under sys/config
The system SHALL expose system parameter management under `/sys/config`. Authorized users MUST be able to page (`POST /sys/config/page`), get detail (`GET /sys/config/{configId}`), create (`POST /sys/config/add`), update (`POST /sys/config/update`), single-delete (`GET /sys/config/remove/{configId}`), and batch-delete (`POST /sys/config/remove`). Service code MUST explicitly assign writable fields and defaults. `configKey` MUST be unique. Built-in parameters (`configType` indicating system built-in) MUST NOT be deleted. When editing a built-in parameter, `configKey` and built-in flag MUST NOT change.

#### Scenario: Add config with unique key
- **WHEN** an authorized user posts a valid config with a new `configKey`
- **THEN** the parameter is persisted and available in subsequent page queries

#### Scenario: Reject delete of built-in config
- **WHEN** a user attempts to delete a built-in system parameter
- **THEN** the operation fails with a business error and the row remains

#### Scenario: Duplicate configKey rejected
- **WHEN** a user creates or updates a parameter to a `configKey` that already exists
- **THEN** the operation fails with a business error

### Requirement: Config value by key and cache refresh
The system SHALL allow querying a config value by key (`GET /sys/config/configKey/{configKey}`) and refreshing the config cache (`POST /sys/config/refreshCache`). Create/update/delete MUST refresh the affected cache entry (or equivalent). Full refresh MUST reload all active keys.

#### Scenario: Get value by key
- **WHEN** an authorized user requests an existing `configKey`
- **THEN** the stored `configValue` is returned

#### Scenario: Refresh cache
- **WHEN** an authorized user calls refresh cache
- **THEN** subsequent key lookups reflect current database values

### Requirement: Config sync import export
The system SHALL provide sync Excel export (`POST /sys/config/export`) and import (`GET /sys/config/import/template`, `POST /sys/config/import` with `updateSupport`), following the OauthClient import/export contract (ids-first export, row cap, partial success with error detail file). Export endpoints MUST use `@IgnoreLogger` with `RESULT` so response bodies are not stored in oper logs.

#### Scenario: Export configs
- **WHEN** an authorized user posts export with optional ids or filters
- **THEN** an xlsx of config rows is downloaded

#### Scenario: Import with failures returns error file
- **WHEN** an import contains invalid rows
- **THEN** the response includes failure counts and an error file payload consistent with OauthClient import
