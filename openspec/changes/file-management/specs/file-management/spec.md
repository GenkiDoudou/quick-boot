## Purpose

Provides an admin file registry for files uploaded through the file management page, including list, preview, download, and delete.

## ADDED Requirements

### Requirement: Management upload registers sys_file

Uploads through the management upload API MUST store the file via local storage and MUST insert a `sys_file` row with original name, extension, size, content type, classify, relative path, uploader identity, and upload time. Common `/file/upload` MUST NOT create `sys_file` rows.

#### Scenario: Management upload creates registry row

- **WHEN** an admin uploads via `/system/file/upload/{classify}` successfully
- **THEN** a `sys_file` row exists for the returned relative path

#### Scenario: Common upload does not register

- **WHEN** a client uploads via `/file/upload/{classify}` successfully
- **THEN** no new `sys_file` row is created for that relative path

#### Scenario: Registry failure rolls back object

- **WHEN** storage write succeeds but inserting `sys_file` fails
- **THEN** the upload fails to the client and the system MUST attempt to delete the stored object

### Requirement: Paginated file list

Authorized admins MUST be able to page and filter managed files by original name, uploader name, classify, and upload time. Soft-deleted files MUST be excluded from the default list.

#### Scenario: List shows registered files only

- **WHEN** an admin opens the file list after both common and management uploads
- **THEN** only management-registered non-deleted files appear

### Requirement: Preview and download

Admins MUST be able to preview a managed file by relative path stream and download by `fileId` using the original filename in Content-Disposition.

#### Scenario: Download uses original name

- **WHEN** an admin downloads a file by `fileId`
- **THEN** the response is an attachment whose filename matches `original_name`

#### Scenario: Preview streams bytes

- **WHEN** an admin requests the management view URL for a stored relative path
- **THEN** the system returns inline file bytes for that object

### Requirement: Soft delete removes storage object

Deleting managed files MUST soft-delete `sys_file` rows and MUST delete the corresponding local storage objects. Missing storage objects MUST still allow successful soft-delete.

#### Scenario: Delete removes list visibility and object

- **WHEN** an admin deletes one or more file IDs
- **THEN** those rows are soft-deleted (absent from default list) and their local objects are removed when present

#### Scenario: Missing object still succeeds

- **WHEN** an admin deletes a file whose storage object is already missing
- **THEN** the soft-delete still succeeds
