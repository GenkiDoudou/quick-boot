## Context

仓库为 Spring Boot 3 多模块后端（`quickboot-web`）与 Vue 3 + Vite 前端（`quick-ui`）。`V5__sys_menu.sql` 已创建 `sys_role`、`sys_role_menu`、`sys_user_role`；`SysRole` 实体与 `MenuServiceImpl.roleMenuTreeselect` 支撑登录与菜单树读。系统管理域已有 `SysConfigController` 风格的 `POST` 子路径写操作与 `PageInfo` 分页契约。

定稿业务设计见 `docs/superpowers/specs/2026-05-14-role-management-design.md`；OpenSpec 动机见同目录 `proposal.md`。本文件将上述内容收敛为实施前技术决策。

## Goals / Non-Goals

**Goals:**

- 交付角色维护全闭环：分页筛选、详情、创建、更新、逻辑删除、状态变更、数据权限（含 `sys_role_dept`）、菜单勾选保存、用户分配、Excel 导出。
- Flyway 扩展 `sys_role.data_scope`、新建 `sys_role_dept`；在尚无 `sys_user` 表时提供最小用户表与分页查询以支撑分配用户验收。
- 菜单树读复用 `GET /system/menu/roleMenuTreeselect/{roleId}`；写菜单关联由 `system.role` 包内服务事务维护 `sys_role_menu`，依赖方向为 **RoleService → MenuService**（或仅 Mapper），禁止循环依赖。
- 内置角色 `role_id = 1`：禁止删除；禁止修改其数据权限与菜单权限。

**Non-Goals:**

- 行级 DataScope 在 SQL 中的自动拼接切面（仅持久化角色侧配置与 UI）。
- 角色复制、继承、多租户；对外暴露 `PUT`/`DELETE` 作为写语义主路径。

## Decisions

### 决策1：独立包 `io.github.genkidoudou.web.system.role`

- **选择**：新建 `role` 子包承载 Controller/Service/Mapper/DTO，与 `menu` 包解耦。
- **原因**：职责清晰，避免 `menu` 包继续膨胀。
- **备选**：仅在 `menu` 包追加 `SysRoleController`（交付更快但长期可读性差，否决）。

### 决策2：接口形状对齐 `SysConfigController`

- **选择**：`GET /system/role/list`、`GET /system/role/{roleId}`；`POST /system/role/create`、`/update`、`/remove`、`/changeStatus`、`/dataScope`、`/menu`、`/export`；分配用户 `GET .../allocatedList`、`GET .../unallocatedList` 与 `POST .../authUser/*`。
- **原因**：符合 `AGENTS.md` 与现有系统管理模块习惯。
- **备选**：与原始需求一致的 `PUT`/`DELETE`（与仓库约束冲突，否决）。

### 决策3：`data_scope` 与 `sys_role_dept`

- **选择**：`data_scope` 取值为 `1` 全部、`2` 自定义、`3` 本部门、`4` 本部门及以下、`5` 仅本人（CHAR(1)）；自定义部门存 `(role_id, dept_id)`；当 `data_scope != '2'` 时删除该角色全部 `sys_role_dept` 行。
- **原因**：与若依生态一致，表结构可测可索引。
- **备选**：部门 ID 逗号拼接存 `sys_role` 字段（难维护，否决）。

### 决策4：菜单保存策略

- **选择**：`POST /system/role/menu` 接收 `menuIds`，在单事务内对该 `role_id` 执行**先删后插**（或等价全量替换）写入 `sys_role_menu`。
- **原因**：幂等、实现简单、与「全量勾选结果」前端模型匹配。
- **备选**：差异比对增量更新（复杂度高，非首版必要）。

### 决策5：删除与用户的约束

- **选择**：若角色仍被 `sys_user_role` 引用，则 **MUST** 拒绝删除并返回明确中文业务错误。
- **原因**：避免孤儿授权与数据不一致。
- **备选**：级联删除关联（破坏审计与误操作风险，否决作默认）。

### 决策6：前端与导出

- **选择**：`quick-ui` 新建页面，布局与色板遵循根目录 `DESIGN.md`；导出使用 `quickboot-common` 既有 Excel 工具，与参数/字典等导出一致。
- **原因**：仓库统一体验与依赖。

## Risks / Trade-offs

- **[风险]** 引入 `sys_user` 最小表与用户管理后续全量模型冲突 → **[缓解]** 字段命名与类型对齐 `sdd/数据库设计规范.md`；若并行 PR 已建表则合并迁移、删除重复 DDL。
- **[风险]** `MenuService` 与 `RoleService` 循环依赖 → **[缓解]** 菜单树读保留在 `MenuService`；角色写菜单仅依赖 Mapper 或单向注入 `MenuService` 中的只读/写辅助方法（实现时以编译通过为准）。
- **[风险]** 父子联动树配置与 Element Plus 版本差异 → **[缓解]** 在 Vue 组件内固定 `check-strictly` 等配置并中文注释验收口径。

## Migration Plan

1. Flyway：追加 `sys_role.data_scope`；创建 `sys_role_dept`；按需创建 `sys_user` 最小表（遵守当前最大 `V*` 版本号递增）。
2. 部署后端：注册 `SysRoleController` 与安全权限标识 `system:role:*`。
3. 部署前端：路由、菜单、API 与页面。
4. **回滚**：回退应用；若需回滚 DDL，准备 `DROP TABLE sys_role_dept`、列回滚及逆向脚本（生产慎用）。

## Open Questions

- （无）实现阶段若全局菜单权限字符串与 `list`/`query` 命名不一致，在合并前与 `sql` 菜单脚本对齐并勘误 delta spec。
