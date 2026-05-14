## Why

系统管理域缺少与定稿设计一致的「用户」全生命周期能力：后端尚无统一 `/system/user` 契约与 `SysUserController`，前端 `api/system/user.js` 仍混用 `/sys/user/*` 与 `/system/user/*`，菜单种子未覆盖用户管理权限点。需在 `docs/superpowers/specs/2026-05-14-user-management-design.md` 指导下落地，以便与角色、部门等模块协同完成权限与人员治理。

## What Changes

- 新增用户域 REST：分页列表、详情、创建、更新、批量删除、改状态、重置密码、分配角色（读/写）、导入/导入模板/导入失败明细下载、导出（全部为设计文档 §5 定稿路径与方法）。
- Flyway：用户管理菜单及按钮（`system:user:*`），并为超级管理员角色绑定；菜单主键与现有 `sys_menu` 无冲突。
- 前端：`quick-ui` 用户列表、新增/编辑弹窗、独立子路由「分配角色」页；`api/system/user.js` 统一改为 `/system/user/*`；无岗位字段；新增/编辑须至少选择一个角色（前后端校验）。
- 内置用户 `user_id=1`：禁止删除、禁止改用户名、禁止停用；其余字段按设计可编辑。
- 导入支持 `updateSupport` 按 `user_name` 更新已存在用户；失败结果支持短时 `errorKey` + `GET /system/user/importError` 下载失败 xlsx。
- **BREAKING**：依赖旧 `/sys/user/*` 或原始需求 §7 `PUT`/`DELETE` 字面路径的调用方须迁移到本文档契约。

## Capabilities

### New Capabilities

- `system-user`：系统用户主数据与生命周期（查询筛选、CRUD、状态、重置密码）、用户-角色绑定（用户侧入口）、Excel 导入导出与模板、权限点与内置用户治理规则。

### Modified Capabilities

- （无）`openspec/specs/` 下尚无已发布的「系统用户」或「系统角色」主规格；与 `sys_user_role` 相关的角色侧接口行为以设计文档「写入收敛」为实现约束，不单独列为已存在规格的增量变更。

## Impact

- **后端**：新建 `io.github.genkidoudou.web.system.user` 包；可能轻触 `SysRoleServiceImpl` 或抽取共享写入逻辑以收敛 `sys_user_role` 写入。
- **前端**：`quick-ui/src/api/system/user.js`、`views/system/user/*`、动态路由/菜单配置与角色种子数据。
- **数据库**：主要复用 `sys_user`、`sys_user_role`、`sys_dept`、`sys_role`；新增迁移仅菜单与绑定。
- **依赖**：`quickboot-common` Excel、现有 Sa-Token 权限、部门树接口、登录密码编码策略。
