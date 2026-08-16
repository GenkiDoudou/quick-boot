## Purpose

Lets administrators configure upload classifies (extensions, size, count, compress switch, anonymous, status) that drive upload validation.

## ADDED Requirements

### Requirement: Classify CRUD

The system MUST allow authorized administrators to create, update, list, query, and soft-delete file classifies in `sys_file_classify`. The classify key MUST be unique among non-deleted rows and MUST NOT contain `/`. After create, the classify key MUST NOT be changeable.

#### Scenario: Create classify

- **WHEN** an admin with `system:fileClassify:add` creates a classify with a unique key and valid limits
- **THEN** the classify is persisted and becomes available to upload validation after cache refresh/invalidation

#### Scenario: Update cannot change key

- **WHEN** an admin updates an existing classify
- **THEN** the system applies changes to display name, limits, compress/anonymous/status fields and MUST reject attempts to change the classify key

#### Scenario: Duplicate key rejected

- **WHEN** an admin creates a classify whose key already exists on a non-deleted row
- **THEN** the system rejects the create with a business warning

### Requirement: Refuse delete when files reference classify

The system MUST refuse soft-deleting a classify when non-deleted `sys_file` rows still reference that classify key.

#### Scenario: Delete blocked by references

- **WHEN** an admin attempts to remove a classify that is still referenced by non-deleted managed files
- **THEN** the system rejects the delete and leaves the classify unchanged

#### Scenario: Delete allowed without references

- **WHEN** an admin removes a classify with no non-deleted `sys_file` references
- **THEN** the classify is soft-deleted and MUST NOT be usable for new uploads

### Requirement: Seed default classify

The system MUST ship a seed classify `default` (enabled, 10MB, limit_count=1, compress off, anonymous off) so uploads can work out of the box.

#### Scenario: Default classify present after migration

- **WHEN** database migrations for this change are applied
- **THEN** a non-deleted `default` classify row exists with the seed limits above

### Requirement: Boolean fields as CHAR(1)

Persisted classify flags (`compress_enabled`, `anonymous`, `status`, `del_flag`) MUST use `CHAR(1)` with `0`/`1` semantics and MUST NOT use boolean/TINYINT(1) columns.

#### Scenario: Compress flag stored as 0 or 1

- **WHEN** an admin sets compress enabled
- **THEN** the stored value is the character `1` (and disabled is `0`)
