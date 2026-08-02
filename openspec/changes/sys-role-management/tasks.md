## 1. 数据库

- [x] 1.1 扩展 `schema-sys.sql`：创建 `sys_role`、`sys_menu`、`sys_role_menu`、`sys_user_role`（含唯一约束与 CHAR(1) 状态字段注释）
- [x] 1.2 写入种子：admin 角色、系统管理目录、客户端管理/角色管理菜单与按钮、role_menu 与管理员 user_role 绑定

## 2. 后端实体与权限查询

- [x] 2.1 新增 Entity/Mapper：`SysRole`、`SysMenu`、`SysRoleMenu`、`SysUserRole`
- [x] 2.2 实现权限查询服务：用户 roleKeys、permissions、可见菜单、若依 Router 树组装；admin 全权限/全菜单策略

## 3. 角色管理 API

- [x] 3.1 实现角色 Service/Controller：`page/get/add/update/remove/changeStatus`（含 role_id=1 与有绑定不可删、`role_key` 唯一）
- [x] 3.2 实现 `menu` 全量保存与 `menuTree`
- [x] 3.3 实现 `authUser` 已分配/未分配分页与 grant/cancel/cancelAll

## 4. 登录 RBAC

- [x] 4.1 扩展 `/auth/me`（`AuthMeVo` userId 用 String）返回 roles/permissions
- [x] 4.2 改造 `/getRouters` 为动态菜单树，移除硬编码唯一数据源

## 5. 前端

- [x] 5.1 改写 `api/system/role.js` 对齐 `/sys/role/*`；清理本期不用的 dataScope/import 调用
- [x] 5.2 改写 `api/login.js` 的 `getInfo`，去掉硬编码全权限
- [x] 5.3 新增 `views/system/role/index.vue`（C7JsonTable：CRUD、菜单授权、用户授权）

## 6. 验证

- [x] 6.1 应用 schema 后登录 admin：`/auth/me` 含 admin 与 `*:*:*`；`/getRouters` 含客户端管理与角色管理
- [x] 6.2 角色页完成创建、改菜单、授权用户；删除有绑定角色与 role_id=1 失败
- [x] 6.3 受限角色用户 routers/permissions 仅含授权范围
