## 1. 数据库迁移

- [x] 1.1 新增 Flyway：为 `sys_role` 追加 `data_scope`（CHAR(1) NOT NULL DEFAULT '1'），与 `docs/superpowers/specs/2026-05-14-role-management-design.md` 一致。
- [x] 1.2 新增 Flyway：创建 `sys_role_dept(role_id, dept_id)` 联合主键。
- [x] 1.3 评估并落地 `sys_user`：若仓库尚无 Flyway 级用户表，则新增最小表（`user_id`、账号、昵称、部门、状态、`del_flag`、审计字段等，对齐 `sdd/数据库设计规范.md`）；若已有则跳过建表仅接 Mapper。

## 2. 后端

- [x] 2.1 在 `quickboot-web` 下新增 `io.github.genkidoudou.web.system.role` 包：扩展/调整 `SysRole` 实体（含 `dataScope`）、`SysRoleDept` 实体及 Mapper、`SysRoleQueryBo`/`SysRoleBo`/`SysRoleVo`、Excel 导出行 DTO。
- [x] 2.2 实现 `GET /system/role/list`：分页、`PageInfo`、筛选（角色名、`role_key`、状态、创建时间区间）。
- [x] 2.3 实现 `GET /system/role/{roleId}` 详情；不存在或已删除时业务异常 + 中文提示。
- [x] 2.4 实现 `POST /system/role/create` 与 `POST /system/role/update`：Jakarta Validation（Add/Update）、`role_key` 全局唯一（排除自身）、审计字段。
- [x] 2.5 实现 `POST /system/role/remove`：批量逻辑删除；拒绝 `role_id=1`；若仍存在 `sys_user_role` 引用则拒绝并提示。
- [x] 2.6 实现 `POST /system/role/changeStatus`：`roleId` + `status` 校验与更新。
- [x] 2.7 实现 `POST /system/role/dataScope`：`data_scope` 枚举校验、`deptIds` 与 `sys_role_dept` 先删后插；非 `2` 时清空部门行；拒绝 `role_id=1`。
- [x] 2.8 实现 `POST /system/role/menu`：`menuIds` 全量替换 `sys_role_menu`；事务；拒绝 `role_id=1`。确认与 `MenuServiceImpl.roleMenuTreeselect` 读回显一致（不引入循环依赖：`RoleService` → `MenuService` 或仅用 Mapper）。
- [x] 2.9 实现 `GET /system/role/authUser/allocatedList` 与 `unallocatedList`：分页、筛选、`roleId` 必传；基于 `sys_user` 与 `sys_user_role` 查询。
- [x] 2.10 实现 `POST /system/role/authUser/selectAll`、`cancel`、`cancelAll`：维护 `sys_user_role`，校验用户与角色存在性。
- [x] 2.11 实现 `POST /system/role/export`：`xlsx` 流，复用 `quickboot-common` Excel 工具，列与列表主要字段一致。
- [x] 2.12 `SysRoleController`：`@Tag`、`@Operation`、`@Parameter`；禁止 `IllegalArgumentException` 业务信号；JavaDoc 齐全。
- [x] 2.13 Spring Security / 权限配置：`system:role:list|add|edit|remove|export|dataScope` 与接口方法绑定；菜单 SQL 或配置同步（若项目从库加载菜单则提供种子脚本）。

## 3. 前端

- [x] 3.1 新增 `quick-ui/src/api/system/role.js`，封装列表、详情、创建、更新、删除、状态、数据权限、菜单保存、分配用户、导出等方法（路径与后端 POST 子路径一致）。
- [x] 3.2 新增路由与菜单项 `/system/role`，接入 `v-hasPermi`。
- [x] 3.3 实现列表页：查询表单、表格、`C7JsonTable`（或项目统一表格）、状态开关二次确认、操作列（编辑、数据权限、分配用户、删除）、导出按钮。
- [x] 3.4 实现新增/编辑弹窗或抽屉：表单校验（名称、`role_key`、排序必填）；实现前阅读 `DESIGN.md`。
- [x] 3.5 实现菜单权限弹窗：调用 `GET /system/menu/roleMenuTreeselect/{roleId}` 展示树与 `checkedKeys`；提交 `POST /system/role/menu`；树组件全选/全不选/父子联动配置写清注释。
- [x] 3.6 实现数据权限弹窗：`data_scope` 单选；自定义时部门树多选；提交 `POST /system/role/dataScope`。
- [x] 3.7 实现分配用户 UI：已分配/未分配分页表与批量授权/取消；默认导出组件/页面补充 JSDoc。

## 4. 验证

- [x] 4.1 后端：单测或集成测覆盖 `role_key` 唯一、`role_id=1` 删除/菜单/数据权限拒绝、数据权限切换清理 `sys_role_dept`、绑定用户时禁止删角色。
- [x] 4.2 前端：`pnpm build:prod` 通过。
- [x] 4.3 联调：菜单树保存后回显、数据权限切换、分配用户流转、导出文件可打开。
