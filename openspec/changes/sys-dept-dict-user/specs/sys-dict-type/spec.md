## ADDED Requirements

### Requirement: Dict type CRUD and uniqueness
The system SHALL manage dictionary types under `/sys/dict/type` with OauthClient-style page/add/update/remove endpoints. `dictType` MUST be unique. Deleting a type that still has dictionary data rows MUST be rejected.

#### Scenario: Create dict type
- **WHEN** an authorized user adds a type with a new `dictType`
- **THEN** the type is stored and its id is returned

#### Scenario: Reject delete type with data
- **WHEN** a type still has dict data rows and a delete is requested
- **THEN** the delete fails and the type remains

### Requirement: Dict type cache refresh
The system SHALL provide `POST /sys/dict/type/refresh` and `POST /sys/dict/type/refresh/{dictType}` guarded by `system:dict:refresh` to clear server-side dict caches.

#### Scenario: Refresh all
- **WHEN** an authorized user posts refresh without a type
- **THEN** all dict-type caches are cleared

### Requirement: Dict type sync import export
The system SHALL support sync Excel export/import for dict types with `updateSupport` keyed by `dictType`, matching OauthClient error-detail behavior.

#### Scenario: Import update existing type
- **WHEN** `updateSupport` is true and a row's `dictType` exists
- **THEN** that type is updated without changing its primary key
