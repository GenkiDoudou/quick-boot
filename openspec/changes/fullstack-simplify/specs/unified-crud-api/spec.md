## ADDED Requirements

### Requirement: Standard CRUD HTTP contract
The system SHALL expose standard management CRUD endpoints using POST for write and list operations: `POST {prefix}/page`, `GET {prefix}/{id}`, `POST {prefix}/add`, `POST {prefix}/update`, `POST {prefix}/remove`, optional `POST {prefix}/export`, `POST {prefix}/import`, `GET {prefix}/importTemplate`. List pagination MUST use `PageRequest<Vo>` in request body. Quartz job APIs MUST keep prefix `monitor/job` and `monitor/job-log`.

#### Scenario: Page via POST
- **WHEN** client posts to `/sys/config/page` with valid `PageRequest`
- **THEN** response is `R<PageInfo<SysConfigVo>>` with records and total

### Requirement: Deprecated GET list compatibility
For controllers currently exposing GET `/list`, the system SHALL add equivalent POST `/page` behavior first. Legacy GET `/list` MUST remain as `@Deprecated` alias for at least four weeks, returning the same data shape where feasible, with HTTP response header `Deprecation: true`.

#### Scenario: Job list migration
- **WHEN** client posts to `/monitor/job/page`
- **THEN** job rows are returned with pagination matching prior GET `/monitor/job/list` semantics

#### Scenario: Deprecated GET list still works during compat window
- **WHEN** client calls GET `/monitor/job/list` during compat period
- **THEN** response succeeds and includes `Deprecation: true` header

### Requirement: Frontend createCrudApi factory
quick-ui SHALL provide `createCrudApi(basePath, options)` in `src/api/_factory/createCrudApi.js` generating page/get/add/update/remove and optional export/import/template functions using unified `utils/request`. Migrated API modules MUST use the factory instead of duplicating remove-id normalization and CRUD boilerplate.

#### Scenario: Config API uses factory
- **WHEN** `api/system/config.js` is refactored to use `createCrudApi('/sys/config', { export: true })`
- **THEN** exported functions behave identically to prior hand-written implementations for page and remove
