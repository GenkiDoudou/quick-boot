## Context

- 已定稿业务与产品约束见 `docs/superpowers/specs/2026-05-14-menu-management-design.md`；proposal 已界定 **BREAKING**（`getRouters` 由空列表变为按库输出）。
- 后端可参考 `sys-dept` 的树加载、剪枝、`treeselect` 映射与 `POST /update`、`POST /remove` 风格；`AuthController.getRouters` 当前返回 `List.of()`。
- 前端 `quick-ui` 已存在若依式动态路由装配（`permission.js`、`getRouters`），菜单管理页与 `api/system/menu.js` 尚待补齐。

## Goals / Non-Goals

**Goals:**

- `MenuService`（方案一）内聚：管理端树、筛选剪枝、`treeselect`、`roleMenuTreeselect`、CRUD、**路由 VO 构建**供 `getRouters` 调用；类型与字段联动校验与需求一致。
- Flyway 提供 `sys_menu`、`sys_role`、`sys_role_menu` 及种子，使 **`roleMenuTreeselect` 对真实 `roleId` 可测**。
- 与 `AGENTS.md` 一致：对外 REST 使用 `@Tag`/`@Operation`/Validation；写操作用 **POST**；业务失败用项目自定义异常。

**Non-Goals:**

- 本迭代删除菜单时 **不** 校验 `sys_role_menu` 是否仍引用（已定稿延后）。
- 不在此变更完成完整「用户-角色」分配 UI（仅保证角色菜单树接口与表结构；用户绑定角色可在后续用户管理变更中衔接）。

## Decisions

| 决策 | 说明 | 理由 |
|------|------|------|
| 模块边界 | **单 `MenuService`**，路由组装以私有方法或同包 `*Assembler` 类承载 | 与方案一一致，避免过早拆服务 |
| 顶级父级 | `parent_id = -1` | 与 `sys_dept` 一致 |
| `getRouters` 归属 | 路径仍在 **`AuthController`**，逻辑委托 **`MenuService`**（或 package-private 门面） | 减少安全入口分散；单测可测 Menu 侧 |
| 路由 JSON 形状 | 对齐现有 **`filterAsyncRouter`** 所需字段（`path`、`component`、`meta`、`children`、`hidden`、`redirect` 等） | 避免前端大规模改造 |
| 主键策略 | 与现有 `sys_*` 迁移一致（雪花或分段 BIGINT 种子）；Flyway 中显式插入避免 H2/MySQL 冲突 | 与 `V2__sys_dept` 风格一致 |
| 权限与会话 | 登录后权限列表来自 **用户角色关联菜单** 上的 `perms`（按钮/菜单节点）；若当前无用户-角色表，可采用 **单用户默认绑定内置管理员角色** 的临时策略并在 `Open Questions` 跟踪移除条件 | 否则 `getRouters` 无法与真实用户联动 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 动态路由与常量路由双源冲突 | 种子菜单路径与 `router/index.js` 常量路由去重；设计文档约定「菜单 path 唯一」 |
| Ruoyi 字段名与当前实体命名不一致 | VO 层显式映射 camelCase；联调对照前端 `SidebarItem` |
| 用户-角色未完备导致 getRouters 全空或全量 | 开发期默认角色绑定策略写清；验收用种子用户或文档步骤 |

## Migration Plan

1. 部署执行 Flyway 新版本：新增三表 + 种子菜单/角色/关联。
2. 已有环境：无旧菜单表则直接新增；若曾手工建表需团队手工对齐 DDL。
3. 回滚：非空表时生产回滚需评估；开发环境按团队 Flyway 规范处理。

## Open Questions

- **已收口**：采用 **`sys_user_role` 种子数据**（`user_id=1` 绑定 `role_id=1`）与占位登录 `StpUtil.login(1L)` 对齐，供 `getRouters` / `getInfo` 联调；接入真实用户体系后以用户管理模块数据替换。
