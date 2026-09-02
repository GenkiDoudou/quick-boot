## ADDED Requirements

### Requirement: Codegen emits Vo-only backend stack
`quickboot-module-tool` generators MUST produce Entity, Mapper, `ISysXxxService`, `SysXxxServiceImpl extends CrudServiceImpl`, Vo-only Controller, and optional ImportRow following `quickboot-system-codegen` Skill. Generated Controllers MUST NOT expose Entity in signatures.

#### Scenario: Generate new table CRUD
- **WHEN** operator generates code for a new `sys_*` table via Gen tool
- **THEN** output includes Vo-only Controller and CrudServiceImpl-based service with I-prefixed interface

### Requirement: Codegen emits frontend API and schema page
Codegen MUST optionally emit `quick-ui/src/api/{domain}/{biz}.js` using `createCrudApi` and `views/{domain}/{biz}/index.vue` driven by schema JSON (columns, form, permPrefix, dict types).

#### Scenario: Generated frontend builds
- **WHEN** codegen output is written to quick-ui paths
- **THEN** `pnpm build` succeeds without manual edits for Tier-1 simple tables

### Requirement: Codegen emits Flyway menu and permissions
For new management modules, codegen MUST emit Flyway INSERT scripts for `sys_menu` and button permissions consistent with `permPrefix` in schema, using ADD-only migrations.

#### Scenario: New module menu visible
- **WHEN** generated Flyway script runs on fresh DB
- **THEN** super admin role sees new menu entry with correct permission strings
