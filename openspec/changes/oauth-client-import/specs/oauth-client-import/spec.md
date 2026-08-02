## ADDED Requirements

### Requirement: Import template without client secret

The system SHALL provide `GET /sys/oauthclient/import/template` that returns an `.xlsx` template whose columns do not include `clientSecret`, `id`, or `createTime`.

#### Scenario: Download template
- **WHEN** an authorized caller requests the import template
- **THEN** the response is an Excel workbook with importable headers and no secret column

### Requirement: Sync multipart import with optional update

The system SHALL provide `POST /sys/oauthclient/import` accepting multipart `file` and `updateSupport`, process at most 5000 rows synchronously, and MUST NOT return any `clientSecret` in the response body.

#### Scenario: Insert new clients
- **WHEN** rows contain new `clientId` values
- **THEN** the system inserts those clients with server-generated secrets and counts them as successes

#### Scenario: Duplicate without update support
- **WHEN** a row's `clientId` already exists and `updateSupport` is false
- **THEN** that row is counted as a failure and other rows continue

#### Scenario: Duplicate with update support
- **WHEN** a row's `clientId` already exists and `updateSupport` is true
- **THEN** writable fields are updated and the existing secret and primary key are preserved

#### Scenario: Partial success with error file
- **WHEN** some rows fail validation or business rules
- **THEN** the response includes success/fail counts and, if failCount > 0, an error workbook payload for download

#### Scenario: Row limit exceeded
- **WHEN** the file contains more than 5000 data rows
- **THEN** the entire import is rejected with a business-error JSON body

### Requirement: Import permission

The system SHALL require `system:oauthClient:import` for template and import endpoints, and Flyway seed SHALL add menu id 2008 under the OAuth client menu for the admin role.

#### Scenario: Unauthorized import
- **WHEN** a caller without `system:oauthClient:import` invokes import or template
- **THEN** the request is denied by authorization
