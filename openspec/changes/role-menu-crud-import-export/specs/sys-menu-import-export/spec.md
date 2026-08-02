## ADDED Requirements

### Requirement: Menu sync Excel export
The system SHALL provide a synchronous Excel export for menus via `POST /system/menu/export` guarded by `system:menu:export`. When the request body contains a non-empty `ids` list, the system MUST export only those menu primary keys. Otherwise it MUST filter by `menuName` and/or `status` like the list API. Rows MUST be flat (one menu per row, no nested `children` column). The export MUST be `.xlsx`, MUST enforce a 5000-row data cap with business error on overflow, and MUST return header-only xlsx for zero matches.

#### Scenario: Export selected menus
- **WHEN** an authorized user posts export with non-empty `ids`
- **THEN** the response is an xlsx containing only those menus as flat rows

#### Scenario: Export filtered menus
- **WHEN** an authorized user posts export with `menuName` or `status` and without `ids`
- **THEN** the response is an xlsx of matching menus (capped at 5000)

### Requirement: Menu sync Excel import
The system SHALL provide `GET /system/menu/import/template` and `POST /system/menu/import` (multipart `file`, `updateSupport`) guarded by `system:menu:import`. When a row includes `menuId` and the id exists, uniqueness is by `menuId`. Otherwise uniqueness MUST be `(parentId, menuName, menuType)` under the same parent. Insert/update field defaults and button-type field clearing MUST match single-row add/update. `updateSupport=false` with an existing match MUST fail that row; `true` MUST update and keep the primary key. Import MUST NOT modify role-menu bindings. Parent menus referenced by `parentId` MUST already exist (except `0`); otherwise the row fails. Partial success and error-detail file rules MUST match the role/oauth-client import contract. Over 5000 rows MUST fail the whole request.

#### Scenario: Import new menu under existing parent
- **WHEN** an authorized user imports a row with `parentId` that exists (or `0`) and no colliding sibling key
- **THEN** the menu is created

#### Scenario: Update existing menu by id
- **WHEN** a row carries an existing `menuId` and `updateSupport` is true
- **THEN** that menu is updated without changing its primary key

#### Scenario: Missing parent
- **WHEN** a row references a non-zero `parentId` that does not exist
- **THEN** that row fails and is included in the error detail
