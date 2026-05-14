## Why

系统管理域已有最小 `sys_role` 与菜单侧「角色菜单树」读能力，但缺少独立的角色维护、数据权限、菜单勾选保存、用户分配与导出能力，无法满足 `原始需求/系统管理/角色管理-需求文档.md` 与已定稿设计 `docs/superpowers/specs/2026-05-14-role-management-design.md` 的验收口径。需在 `quickboot` + `quick-ui` 内补齐与 `SysConfigController` 等模块一致的接口风格与前端页面。

## What Changes

- 新增角色管理能力：分页查询（角色名称、`role_key`、状态、创建时间区间）、详情、新增、修改、逻辑删除、状态变更、数据权限（若依式 `data_scope` + 自定义部门 `sys_role_dept`）、菜单权限保存（写 `sys_role_menu`）、分配用户（已分配/未分配列表与批量授权/取消，维护 `sys_user_role`）、导出 `xlsx`。
- 数据库：`sys_role` 扩展 `data_scope`；新建 `sys_role_dept`；若尚无 Flyway 级 `sys_user` 则新增最小表及分页查询以支撑分配用户（若与用户管理变更合并则单次迁移避免重复）。
- 写操作统一 `POST` 子路径，读操作 `GET`（与 `AGENTS.md` 一致，**不对齐**原始需求文档中的 `PUT`/`DELETE` 字面契约）。
- 复用 `GET /system/menu/roleMenuTreeselect/{roleId}` 做菜单树与 `checkedKeys` 回显；角色域通过 `RoleService → MenuService` 单向依赖避免循环引用。
- 内置超级管理员 `role_id = 1`：禁止删除；禁止修改其菜单权限与数据权限；其余字段不强制锁定（以设计文档为准）。
- 前端：`quick-ui` 路由、菜单、API、列表与弹窗/抽屉（菜单树、数据权限、分配用户），遵循根目录 `DESIGN.md`。
- 明确不包含：行级 DataScope SQL 自动拼接切面、角色复制、多租户、与原始需求 §7 方法字面一致的 PUT/DELETE 对外 API。

## Capabilities

### New Capabilities

- `system-role-management`: 提供系统角色的查询、分页、详情、创建、更新、删除、状态切换、数据权限与自定义部门、菜单权限保存、用户分配及导出能力；含内置角色保护规则与权限点 `system:role:*`。

### Modified Capabilities

- （无）

## Impact

- 后端：`quickboot-web` 下新增 `io.github.genkidoudou.web.system.role` 包（Controller/Service/Mapper/Domain/DTO/Excel 行对象）、Flyway 迁移；扩展 `SysRole` 实体；与现有 `MenuService`、`SysRoleMenuMapper`、`SysUserRoleMapper` 协作。
- 前端：`quick-ui` 的 `router`、菜单、`src/api/system/role.js`、系统管理角色页面与 `v-hasPermi`。
- 数据库：`sys_role` 变更、`sys_role_dept` 新建、可选 `sys_user` 新建；与 `V5__sys_menu.sql` 种子角色 id=1 行为一致。
- 权限与安全：新增/挂载 `system:role:list`、`add`、`edit`、`remove`、`export`、`dataScope` 等标识。
