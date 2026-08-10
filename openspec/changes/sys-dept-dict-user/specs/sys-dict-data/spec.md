## ADDED Requirements

### Requirement: Dict data CRUD under type
The system SHALL manage dictionary data under `/sys/dict/data` with OauthClient-style CRUD. Uniqueness MUST be `(dictType, dictValue)`. Rows belong to an existing dict type.

#### Scenario: Add dict data
- **WHEN** an authorized user adds a label/value under an existing `dictType`
- **THEN** the row is stored

#### Scenario: Duplicate value rejected
- **WHEN** a second row with the same `dictType` and `dictValue` is added without update semantics
- **THEN** the operation fails

### Requirement: Query enabled data by type for useDict
The system SHALL expose `GET /sys/dict/data/type/{dictType}` returning enabled items for frontend `useDict` / `getDicts`.

#### Scenario: Load options by type
- **WHEN** a client requests data for `sys_normal_disable`
- **THEN** enabled label/value pairs for that type are returned

### Requirement: Dict data sync import export
The system SHALL support sync Excel export/import for dict data with `updateSupport`, scoped by type when provided, with the same failure-detail contract as OauthClient.

#### Scenario: Export by dictType filter
- **WHEN** export is requested with a `dictType` filter
- **THEN** only matching data rows are included in the xlsx
