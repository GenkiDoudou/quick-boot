## ADDED Requirements

### Requirement: Menu CRUD paths align with OauthClient/Role
The menu management API under `/system/menu` SHALL expose write operations consistent with `SysOauthClient` / `SysRole` conventions: create via `POST /system/menu/add` returning the new `menuId` as string data; update via `POST /system/menu/update`; single delete via `GET /system/menu/remove/{menuId}`; batch delete via `POST /system/menu/remove` with a JSON array of ids. Existing tree list, treeselect, roleMenuTreeselect, detail, and updateSort endpoints MUST remain available. Delete MUST still reject menus that have children.

#### Scenario: Create menu via add path
- **WHEN** an authorized user posts a valid body to `/system/menu/add`
- **THEN** the menu is persisted and the response data is the new menu id string

#### Scenario: Batch remove leaf menus
- **WHEN** an authorized user posts a list of leaf menu ids to `/system/menu/remove`
- **THEN** those menus are deleted

#### Scenario: Remove menu with children rejected
- **WHEN** an authorized user attempts to delete a menu that still has children
- **THEN** the operation fails with the existing has-children business error

### Requirement: Menu service explicit field assignment
On add and update, the menu service MUST explicitly assign and default writable fields (name/type/parent/order/flags/status and button-type clearing) rather than relying solely on unchecked bean copy for those controls, matching the explicit-assignment style used by `SysRoleServiceImpl` / `SysOauthClientServiceImpl`.

#### Scenario: Add button menu clears route fields
- **WHEN** a user adds a menu with `menuType=F`
- **THEN** path/component/route/icon/query and frame/cache/visible flags are normalized as in current single-add rules
