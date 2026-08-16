## Purpose

Delivers quick-ui admin pages and C7 upload/preview components that consume file classify and file management APIs.

## ADDED Requirements

### Requirement: C7Upload uses classify rules

`C7Upload` MUST require a classify, MUST load that classify's rules for client-side hints/validation, MUST default to common upload API, and MUST allow an `uploadFn` override for management uploads.

#### Scenario: Missing classify disables upload

- **WHEN** classify is empty or the rule fails to load
- **THEN** the upload control is not ready for upload and shows a clear tip

#### Scenario: Management page overrides uploadFn

- **WHEN** the file management dialog uses `C7Upload` with a system upload function
- **THEN** files are submitted to the management upload API instead of the common upload API

### Requirement: C7Preview media behaviors

`C7Preview` MUST support previewing image and video URLs in-dialog/in-component and MUST open other file types in a new window.

#### Scenario: Image preview

- **WHEN** preview URLs include images
- **THEN** the user can open an image preview without leaving the page context

#### Scenario: Non-media opens new window

- **WHEN** the user previews a non-image, non-video URL
- **THEN** the client opens the URL in a new browsing context

### Requirement: File classify admin page

quick-ui MUST provide a system page to list and maintain classifies with create/edit/delete, showing extension, size, count, compress switch, anonymous, and status. Edit MUST disable changing the classify key. Compress UI MUST indicate configuration-only (no compression yet).

#### Scenario: Edit disables classify key

- **WHEN** an admin opens edit on an existing classify
- **THEN** the classify key field is read-only or disabled

#### Scenario: Compress switch describes server compression

- **WHEN** an admin views the classify form compress switch
- **THEN** the UI indicates that enabling compress runs server-side image compression for supported formats

### Requirement: File management admin page

quick-ui MUST provide a system page to list managed files, upload with classify selection, preview, download, and delete, using the management APIs and permissions.

#### Scenario: Upload then appear in list

- **WHEN** an admin uploads a file from the file management page successfully
- **THEN** the table refreshes and shows the new file with name, classify, size, ext, uploader, and time

#### Scenario: Preview download delete actions

- **WHEN** an admin uses row actions on a listed file
- **THEN** preview, download, and delete behave according to management API semantics and permission codes
