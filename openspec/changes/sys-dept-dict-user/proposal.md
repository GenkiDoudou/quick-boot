## Why

系统管理缺少部门、字典（类型/字典项）与用户管理能力：前端仅有部分 dict API 桩，后端无对应模块；bak 页面依赖旧异步导入中心，与当前已落地的 `SysOauthClient` 契约不一致。需要按统一风格补齐管理端，支撑组织、字典标签与账号运维。

## What Changes

- 新增精简**部门管理**（表、CRUD、树下拉、同步导入导出；有用户绑定则拒绝删除）。
- 新增**字典类型 / 字典项**管理（CRUD、缓存刷新、按类型查询供 `useDict`、同步导入导出）。
- 新增**用户管理**（CRUD、同步导入导出、分配角色、重置密码、启停；不含个人中心）。
- Flyway：`sys_dept` / `sys_dict_type` / `sys_dict_data`、菜单权限、种子数据；必要时统一 `sys_user.user_id` 类型。
- 前端：从 `bak/quick-ui` 迁移并改造为现 `C7JsonTable` + 同步导入导出；API 对齐 `sys/*`。
- **BREAKING（相对 bak）**：不兼容 bak 旧路径与异步导入中心字段。

参考设计：`docs/superpowers/specs/2026-08-03-sys-dept-dict-user-design.md`。

## Capabilities

### New Capabilities

- `sys-dept`: 部门树/CRUD/导入导出与删除约束。
- `sys-dict-type`: 字典类型 CRUD、刷新缓存、导入导出。
- `sys-dict-data`: 字典项 CRUD、按类型查询、导入导出。
- `sys-user-mgmt`: 用户管理 CRUD、导入导出、分配角色、重置密码。

### Modified Capabilities

- （无独立既有 delta；登录用 `SysUser` 查询能力保持，本变更扩展管理端。）

## Impact

- 后端：`quickboot-system` 新增 Dept/Dict/User 管理 Controller/Service/VO；Flyway 迁移。
- 前端：`quick-ui` views/api/system（dept、dict、user）；`utils/dict.js` 路径对齐。
- 依赖：复用 `ExcelUtils`、`C7ExcelUpload`/`C7ExcelDownload`、现有密码编码器与角色关联表。
