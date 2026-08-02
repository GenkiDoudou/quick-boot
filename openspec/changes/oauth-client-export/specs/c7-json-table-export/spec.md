## ADDED Requirements

### Requirement: C7ExcelDownload sync blob download

The frontend packages SHALL provide `C7ExcelDownload` that invokes a `downloadFn`, accepts a `Blob` or `{ data, headers }`, triggers a browser download, and surfaces JSON-error blobs as user-visible error messages.

#### Scenario: Successful blob download
- **WHEN** `downloadFn` resolves to a valid file Blob (optionally with `Content-Disposition` headers)
- **THEN** the browser downloads a file using the resolved or default file name

#### Scenario: JSON error blob
- **WHEN** `downloadFn` resolves to a JSON error Blob
- **THEN** the component shows the business error message and does not save an xlsx file

### Requirement: C7JsonTable built-in exportFunction

`C7JsonTable` SHALL render an export control when `exportFunction` is provided (unless `showExportButton` disables it), honor `exportButtonPermi`, and call `exportFunction` with a snapshot of current search params plus selected row ids when any rows are selected.

#### Scenario: Export with selection
- **WHEN** the user has selected one or more rows and clicks Export
- **THEN** `exportFunction` receives a snapshot that includes those row primary keys under `ids`

#### Scenario: Export without selection
- **WHEN** no rows are selected and the user clicks Export
- **THEN** `exportFunction` receives the current search-param snapshot without a non-empty `ids` list

#### Scenario: Permission hides export
- **WHEN** `exportButtonPermi` is set and the current user lacks those permissions
- **THEN** the export button is not shown

### Requirement: OAuth client page wires export

The OAuth client management page SHALL pass an `export-function` that posts the snapshot to `/sys/oauthclient/export` as JSON with `responseType: 'blob'`, and SHALL set `export-button-permi` to `system:oauthClient:export`.

#### Scenario: Page export click
- **WHEN** an authorized user clicks Export on the OAuth client page
- **THEN** the browser downloads `oauth-client.xlsx` (or the server-provided file name) for the selected or filtered clients
