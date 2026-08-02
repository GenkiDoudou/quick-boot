## Why

角色与菜单管理的 CRUD 风格、路径约定与当前已落地的 `SysOauthClient` 不完全一致，且缺少对称的同步 Excel 导入/导出。希望按 OauthClient 同一套契约补齐，便于运维批量维护权限数据，并降低后续模块再对齐成本。

## What Changes

- 按当前 `SysOauthClient` 对齐 **SysRole / SysMenu** 的新增/查询/修改/删除写法：显式字段赋值与默认值、主键保护、Controller 路径与返回值习惯（Role 已基本对齐；Menu 重点统一 `add`/`remove` 等路径与批量删除能力）。
- 为角色、菜单增加**同步导出**：勾选优先，否则按当前查询条件；EasyExcel `.xlsx`。
- 为角色、菜单增加**同步导入**：模板下载 + multipart 上传；可选「更新已存在」；失败明细文件（与 OauthClient 导入一致）。
- 菜单导入以业务键识别（建议 `parentId` + `menuName` + `menuType`，或导出行含 `menuId` 时按 id 更新）；导入**不**自动维护角色-菜单授权关系。
- 角色导入以 `roleKey` 判重；导入**不**带用户授权/菜单授权。
- Flyway 增加 `system:role:export` / `system:role:import`、`system:menu:export` / `system:menu:import` 按钮权限。
- 前端：角色页（`C7JsonTable`）挂载导入导出；菜单页（非 JsonTable）增加等价导入导出入口。

## Capabilities

### New Capabilities

- `sys-role-import-export`: 角色同步 Excel 导入/导出契约、判重与失败明细。
- `sys-menu-import-export`: 菜单同步 Excel 导入/导出契约、树字段处理与失败明细。
- `sys-menu-crud-align`: 菜单管理 CRUD 路径/返回值/显式赋值与 OauthClient/Role 对齐。

### Modified Capabilities

- （无；`sys-role-management` 等既有能力若存在仅实现层对齐，本变更以新增导入导出 + 菜单 CRUD 对齐为主。）

## Impact

- 后端：`SysRoleController` / `SysRoleServiceImpl`、`SysMenuController` / `SysMenuServiceImpl`、VO/ImportRow、Flyway 权限脚本。
- 前端：`quick-ui/src/api/system/role.js`、`menu.js`；`views/system/role/index.vue`、`views/system/menu/index.vue`。
- 依赖：复用现有 `ExcelUtils` / `C7ExcelDownload` / `C7ExcelUpload`（角色）；菜单页可直接调用 API + 组件或等价按钮。
- **BREAKING（菜单）**：若将 `POST /system/menu` 改为 `POST /system/menu/add`、删除改为与 Role 一致的 `GET remove/{id}` + `POST /remove`，需同步改前端 API；建议一次性改齐。
