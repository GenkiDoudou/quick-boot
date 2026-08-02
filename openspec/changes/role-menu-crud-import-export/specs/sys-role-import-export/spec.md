## ADDED Requirements

### Requirement: Role sync Excel export
The system SHALL provide a synchronous Excel export for roles via `POST /sys/role/export` guarded by `system:role:export`. When the request body contains a non-empty `ids` list, the system MUST export only those role primary keys and ignore search filters. When `ids` is empty or absent, the system MUST apply the same filters as the role page query (`roleName`, `roleKey`, `status`). The export MUST be `.xlsx`, MUST NOT exceed 5000 data rows (over limit MUST fail with a business error), and MUST still return a header-only file when zero rows match.

#### Scenario: Export by selected ids
- **WHEN** an authorized user posts export with non-empty `ids`
- **THEN** the response is an xlsx containing only those roles

#### Scenario: Export by search filters
- **WHEN** an authorized user posts export without `ids` but with search fields
- **THEN** the response is an xlsx of roles matching those filters (capped at 5000)

### Requirement: Role sync Excel import
The system SHALL provide template download `GET /sys/role/import/template` and sync import `POST /sys/role/import` (multipart `file`, `updateSupport`) guarded by `system:role:import`. Import uniqueness MUST be based on `roleKey`. Missing `roleKey` rows MUST fail that row. When the role does not exist, the system MUST insert using the same explicit defaults as single-row add. When the role exists and `updateSupport` is false, that row MUST fail. When the role exists and `updateSupport` is true, the system MUST update writable fields and keep `roleId`. Import MUST NOT create or change user-role or role-menu bindings. Partial success MUST be allowed; when failures exist the response MUST include downloadable error detail (compatible with the frontend Excel upload result contract). Row count over 5000 MUST fail the whole request.

#### Scenario: Import new roles
- **WHEN** an authorized user uploads a valid xlsx with new `roleKey` values
- **THEN** those roles are created and the response reports success counts

#### Scenario: Duplicate without updateSupport
- **WHEN** a row's `roleKey` already exists and `updateSupport` is false
- **THEN** that row is counted as failed and other rows may still succeed

#### Scenario: Duplicate with updateSupport
- **WHEN** a row's `roleKey` already exists and `updateSupport` is true
- **THEN** that role is updated in place without changing its primary key
