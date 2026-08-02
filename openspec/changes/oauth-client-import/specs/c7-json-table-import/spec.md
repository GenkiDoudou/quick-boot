## ADDED Requirements

### Requirement: Slim C7ExcelUpload sync import UI

The frontend packages SHALL provide a slim `C7ExcelUpload` that supports file pick/drop, optional “update existing” checkbox, template download via `C7ExcelDownload`, sync result summary, and local download of `errorFileBase64` failure details, and MUST NOT depend on the import/export center or async task APIs.

#### Scenario: Successful sync import
- **WHEN** the user selects a valid file and confirms import
- **THEN** `uploadFn` is invoked with strategy `overwrite` or `ignore` and the UI shows total/success/fail counts

#### Scenario: Download failure details
- **WHEN** the sync result has failCount > 0 and an error file payload
- **THEN** the user can download the failure workbook locally

### Requirement: C7JsonTable built-in import

`C7JsonTable` SHALL render an import control when import is enabled, honor `importButtonPermi`, open a dialog embedding `C7ExcelUpload`, wire `importFunction` and `importTemplateDownloadFn`, refresh the list after successful import, and emit `import-success`.

#### Scenario: Import button permission
- **WHEN** `importButtonPermi` is set and the user lacks those permissions
- **THEN** the import button is not shown

#### Scenario: Import refreshes list
- **WHEN** a sync import completes successfully (including partial success with some failures)
- **THEN** the table refreshes its data list

### Requirement: OAuth client page wires import

The OAuth client management page SHALL enable import with `system:oauthClient:import`, provide multipart `importOauthClient` and template download functions, and place import alongside the existing export toolbar actions.

#### Scenario: Page import click
- **WHEN** an authorized user opens Import on the OAuth client page and uploads a valid workbook
- **THEN** the backend import API is called and the list reflects inserted or updated clients without revealing secrets in the UI response
