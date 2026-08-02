## ADDED Requirements

### Requirement: /auth/me 返回真实角色与权限

`GET /auth/me` MUST 在已登录前提下返回当前用户标识与昵称，以及 `roles`（启用且未删除角色的 `role_key` 列表）与 `permissions`（经角色菜单聚合的非空 `perms` 去重集合）。当用户拥有 `role_key=admin` 时，`permissions` MUST 为包含 `*:*:*` 的全权限表示。`userId` MUST 与 `sys_user.user_id` 类型一致（String）。

#### Scenario: 超级管理员 me 接口

- **WHEN** 已绑定 admin 角色的用户调用 `GET /auth/me`
- **THEN** 响应中 `roles` 含 `admin`，且 `permissions` 含 `*:*:*`

#### Scenario: 无角色用户

- **WHEN** 已登录但无任何启用角色的用户调用 `GET /auth/me`
- **THEN** `roles` 为空列表（或等价空集合），`permissions` 不含 `*:*:*`（除非另有明确产品规则）

### Requirement: 前端 getInfo 使用后端权限数据

前端 `getInfo` MUST 基于 `/auth/me`（或后端提供的等价结构）填充 `user`/`roles`/`permissions`，MUST NOT 再写死 `ROLE_DEFAULT` 与 `*:*:*`。若后端 roles 为空，可保留 store 既有兜底策略，但 MUST NOT 在 API 适配层无条件注入全权限。

#### Scenario: getInfo 不再硬编码全权限

- **WHEN** 前端执行 getInfo 且后端返回非 admin 的有限 permissions
- **THEN** store 中 permissions 与后端返回一致，不为无条件 `*:*:*`

### Requirement: /getRouters 按角色动态生成

`GET /getRouters` MUST 根据当前用户角色查询启用未删的目录/菜单（`menu_type` 为 M/C），组装与现网 `permission.js` 兼容的若依形态路由树。拥有 `admin` 角色时 MUST 返回全部启用未删的 M/C 菜单。MUST NOT 再使用仅含客户端管理的硬编码静态列表作为唯一数据源。种子菜单 MUST 使 admin 用户至少能看到客户端管理与角色管理。

#### Scenario: admin 看到客户端与角色菜单

- **WHEN** admin 用户调用 `GET /getRouters`
- **THEN** 路由树中包含指向 `system/oauthClient/index` 与 `system/role/index` 的菜单项

#### Scenario: 受限角色仅见授权菜单

- **WHEN** 仅授权客户端管理菜单的非 admin 用户调用 `GET /getRouters`
- **THEN** 路由树包含客户端管理且不包含角色管理菜单
