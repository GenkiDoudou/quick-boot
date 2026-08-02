## Context

主工程已有 `SysUser`、`SysOauthClient` 与 Sa-Token 登录；`getInfo`（前端适配 `/auth/me`）硬编码 roles/permissions；`/getRouters` 硬编码客户端菜单。bak 具备完整若依式角色管理，但包名与 API 风格与现网不一致。

产品设计见：`docs/superpowers/specs/2026-08-02-sys-role-management-design.md`。本文件落实技术决策供 tasks 拆解。

约束：UTF-8 无 BOM；DB/实体禁止 boolean 是否语义；API 优先 `@PostMapping`；`user_id` 为 `VARCHAR(64)`。

## Goals / Non-Goals

**Goals:**

- `sys_role` / `sys_menu` / `sys_role_menu` / `sys_user_role` + 种子。
- 角色 CRUD、改状态、全量菜单授权、角色内用户授权。
- `/auth/me` 返回真实 roles/permissions；`/getRouters` 按角色动态组装。
- 前端角色列表页（含菜单树与用户授权弹窗）；`getInfo` 去硬编码。

**Non-Goals:**

- 完整菜单管理后台、dataScope/部门、导入导出、用户侧 `auth-role` 独立页。
- 本期不强制 `@SaCheckPermission`（与 OauthClient 现状对齐）。

## Decisions

### D1：API 前缀与风格对齐 OauthClient

- **选择**：`/sys/role/page|add|update|remove|...`，POST 为主。
- **替代**：照搬 bak `/system/role` + GET list。
- **理由**：与现网控制器一致，降低前端新页学习成本；同步改写残留 `role.js`。

### D2：DDL 落点

- **选择**：扩展 `schema-sys.sql`。
- **替代**：新建 Flyway 目录。
- **理由**：当前 web 模块以 schema-sys 为权威本地表脚本。

### D3：权限查询集中服务

- **选择**：`ISysPermissionService`（或等价）统一：用户 roleKeys、perms、可见菜单树、若依 Router VO 组装；供 RoleController、LoginController、getRouters 复用。
- **理由**：避免 me/routers/角色菜单三处复制 SQL。

### D4：admin 策略

- **选择**：`role_key=admin` → permissions=`*:*:*`；routers 返回全部启用未删的 M/C 菜单。
- **替代**：admin 仍按 role_menu 过滤。
- **理由**：与 bak 超管体验一致，种子绑定后即可运维。

### D5：userId 类型

- **选择**：`AuthMeVo.userId` 与关联表统一为 **String**，对齐 `sys_user.user_id`。
- **理由**：消除现网 Long/String 混用风险。

### D6：前端页面

- **选择**：仅 `views/system/role/index.vue`，交互对齐 `oauthClient/index.vue`（C7JsonTable）。
- **理由**：用户确认范围；用户授权在角色页弹窗完成。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 去掉硬编码全权限后旧账号无角色导致空白菜单 | 种子将 admin 角色绑定脚手架管理员用户；文档说明需跑 schema |
| status 语义与 bak 若依「0正常1停用」混淆 | 实现前对照 `SysUser`/`SysOauthClient` 注释写死并在实体 JavaDoc 标明 |
| getRouters 字段与 permission.js 不兼容 | 对照现硬编码结构与 bak 路由字段做黄金样例断言 |
| 仅扩 schema、已有库未重建 | README/任务中注明需执行增量 SQL 或重建 H2/本地库 |

## Migration Plan

1. 合并 DDL 与后端、前端代码。  
2. 本地应用 schema 增量并重启；用管理员登录验证 routers 含角色管理。  
3. 回滚：还原 `/auth/me`/`getRouters` 硬编码行为并移除新表（开发环境可重建库）。

## Open Questions

- 无（产品设计已关闭范围；`status` 取值以实现时对照现网实体注释为准）。
