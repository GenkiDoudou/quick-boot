## ADDED Requirements

### Requirement: Sync export endpoint without client secret

The system SHALL provide `POST /sys/oauthclient/export` that returns an `.xlsx` workbook of OAuth clients and MUST NOT include `clientSecret` in any column or cell.

#### Scenario: Successful export by search
- **WHEN** an authorized caller posts export with search fields and without non-empty `ids`
- **THEN** the response is an Excel file whose rows match the same filter rules as the page query and contain no secret values

#### Scenario: Export by selected ids
- **WHEN** an authorized caller posts export with a non-empty `ids` array
- **THEN** the workbook contains only those primary-key rows and search fields are ignored

#### Scenario: Empty result still downloads headers
- **WHEN** the query or ids yield zero rows
- **THEN** the system still returns a valid `.xlsx` that includes header columns only

#### Scenario: Row limit exceeded
- **WHEN** matching rows exceed 5000
- **THEN** the system returns a business-error JSON body (not an xlsx) indicating the limit was exceeded

### Requirement: Export permission

The system SHALL require permission `system:oauthClient:export` for the export endpoint, and RBAC seed data SHALL include a corresponding button permission under the OAuth client menu.

#### Scenario: Unauthorized export
- **WHEN** a caller without `system:oauthClient:export` invokes the export endpoint
- **THEN** the request is denied by authorization

### Requirement: Excel column set

The export workbook SHALL include columns for `id`, `clientId`, `clientName`, `apiPathPatterns`, `tokenTimeout`, `checkCaptcha`, `remark`, and `createTime` when available on the entity, and MUST NOT include a secret column.

#### Scenario: Column audit
- **WHEN** a successful export file is opened
- **THEN** the listed columns are present and no `clientSecret` / secret-labeled column exists
