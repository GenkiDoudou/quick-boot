## Why

主工程尚无角色管理，登录后 `getInfo` 硬编码全权限、`getRouters` 写死客户端菜单，无法按角色控制菜单与按钮。参考 bak 的若依式角色能力，在现网分层下补齐角色-菜单-用户闭环，并使动态路由与权限来自真实数据。

## What Changes

- 扩展 `schema-sys.sql`：新增 `sys_role`、`sys_menu`、`sys_role_menu`、`sys_user_role` 及种子（超级管理员、系统管理目录、客户端管理与角色管理菜单/按钮）。
- 新增角色后端（entity/mapper/service/controller）：分页 CRUD、改状态、全量保存角色菜单、角色内用户授权/取消。
- 新增最小菜单读能力：角色菜单树（含已选 keys）；不做完整菜单管理后台。
- **BREAKING（行为）**：`GET /auth/me` 改为返回真实 `roles`/`permissions`；前端 `getInfo` 去掉硬编码全权限。
- **BREAKING（行为）**：`GET /getRouters` 改为按用户角色从 `sys_menu` 动态组装；移除 `ScaffoldCompatController` 硬编码路由列表。
- 新增前端 `views/system/role/index.vue`；改写 `api/system/role.js` 对齐 `/sys/role/*`。
- 本期不做：完整菜单 CRUD 页、dataScope、导入导出、用户侧独立 `auth-role` 页。

## Capabilities

### New Capabilities

- `sys-role`: 角色表与关联、角色管理 API、角色页（含菜单授权与用户授权）。
- `login-rbac`: 登录态权限与动态路由（`/auth/me` roles/permissions、`/getRouters` 菜单树）。

### Modified Capabilities

- （无）`openspec/specs/` 下无既有能力需改需求。

## Impact

- **后端**：`quickboot-system`（角色/菜单实体与接口）、`LoginController`/`AuthMeVo`、`ScaffoldCompatController`、`schema-sys.sql`。
- **前端**：`quick-ui` 角色页、`api/system/role.js`、`api/login.js` 的 `getInfo`。
- **依赖**：现有 Sa-Token 登录与 `sys_user`；`sys_user_role.user_id` 为 `VARCHAR(64)`。
- **设计来源**：`docs/superpowers/specs/2026-08-02-sys-role-management-design.md`。
