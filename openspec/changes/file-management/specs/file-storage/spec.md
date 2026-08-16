## Purpose

Provides local file storage with classify-based upload validation, common upload, and inline preview for business clients.

## ADDED Requirements

### Requirement: Local storage upload by classify

The system MUST store uploaded files on local disk and MUST validate each upload against an enabled classify rule (extension whitelist and max size). The classify key MUST NOT contain `/`.

#### Scenario: Valid upload succeeds

- **WHEN** a client uploads a file to the common upload API with an enabled classify and the file matches that classify's extension and size limits
- **THEN** the system stores the file locally and returns a relative path (and optional view URL) without creating a `sys_file` management record

#### Scenario: Reject oversized or wrong extension

- **WHEN** the uploaded file exceeds `limit_size_bytes` or its extension is not allowed by `limit_ext` (or the built-in default whitelist when `limit_ext` is empty)
- **THEN** the system rejects the upload with a business warning and MUST NOT persist the object

#### Scenario: Reject missing or disabled classify

- **WHEN** the classify does not exist, is soft-deleted, or has `status=1`
- **THEN** the system rejects the upload with a business warning

### Requirement: Common classify query for upload clients

The system MUST expose APIs to list enabled classifies and to fetch a single classify rule for upload UI validation.

#### Scenario: List enabled classifies

- **WHEN** an authenticated client requests the classify list
- **THEN** the system returns only non-deleted classifies with `status=0`

#### Scenario: Get one classify

- **WHEN** an authenticated client requests a specific classify key that exists and is enabled
- **THEN** the system returns its limits including extension, size, count, compress flag, and anonymous flag

### Requirement: Common preview by relative path

The system MUST stream a stored object as an inline preview response when given a relative path, subject to configured security/anonymous rules.

#### Scenario: Preview existing object

- **WHEN** a client requests preview for an existing relative path and access is allowed
- **THEN** the system returns the file bytes with an inline content disposition

### Requirement: Compress images on upload with threshold

When classify `compress_enabled` is `1`, clients SHOULD compress jpg/jpeg/png/bmp before upload when the file size is at least `qc.file.compress.min-size-kb`. Server-side compression MUST run only when `qc.file.compress.enabled` is true, the classify flag is on, and the payload exceeds the same min-size threshold. Non-image files MUST remain unchanged. If compression fails or does not shrink the payload, the original bytes MUST be kept.

#### Scenario: Server compress disabled keeps bytes

- **WHEN** `qc.file.compress.enabled` is false and a large JPEG is uploaded under a classify with `compress_enabled=1`
- **THEN** the stored object bytes match the uploaded content (no server compression)

#### Scenario: Server compress skips below min size

- **WHEN** server compress is enabled but the file is smaller than `min-size-kb`
- **THEN** the stored object bytes match the uploaded content

#### Scenario: Server compress shrinks large jpeg when enabled

- **WHEN** server compress is enabled, classify compress is on, and a large JPEG exceeds min size
- **THEN** the stored object byte length is smaller than the original upload

#### Scenario: Classify API exposes compress params for frontend

- **WHEN** a client fetches a classify rule
- **THEN** the response includes compressMinSizeKb, compressQuality, and compressMaxEdge for client-side compression
