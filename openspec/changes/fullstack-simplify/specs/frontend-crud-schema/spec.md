## ADDED Requirements

### Requirement: Schema-driven CRUD pages Tier A
quick-ui SHALL support schema-driven C7JsonTable pages for Tier A modules: config, dict type, dict data, fileClassify, online, logininfor, operlog, deployRecord, slowSql, job-log. Each page MUST reduce to approximately ≤55 lines of Vue SFC excluding imports, using shared composable or wrapper where applicable.

#### Scenario: Config page uses schema
- **WHEN** `views/system/config/index.vue` is migrated
- **THEN** table columns and search fields are driven from schema or shared composable and page remains functionally equivalent for CRUD/export

### Requirement: Unified datetime column formatting
List pages displaying `createTime`, `loginTime`, or `operTime` MUST use shared `formatDateTime` (dayjs-based) via column formatter or slot helper, replacing ad-hoc `parseTime` imports in monitor pages.

#### Scenario: Logininfor time column
- **WHEN** logininfor list renders after migration
- **THEN** access time displays `yyyy-MM-dd HH:mm:ss` consistent with backend Vo string

### Requirement: Dynamic routes migration to sys_menu
The three hidden routes currently in `router/index.js` dynamicRoutes (dict-data by type, user-auth role, gen edit) MUST be representable via Flyway-added `sys_menu` rows with hidden meta, allowing removal of local `dynamicRoutes` and `filterDynamicRoutes` after verification.

#### Scenario: Hidden gen edit route from menu
- **WHEN** super admin loads menus after Flyway migration
- **THEN** tool gen edit path is reachable without hardcoded frontend dynamicRoutes entry
