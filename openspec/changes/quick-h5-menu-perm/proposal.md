## Why

quick-h5 工作台菜单目前写死在 mock 中，与角色无关；登录未落库 `permissions`，业务页按钮对所有登录用户可见。需与 PC 同一套 `sys_menu` + 角色授权对齐，实现菜单按角色下发、按钮按 `perms` 显隐。

## What Changes

- H5：`user` store 持久化 `roles` / `permissions`；登录与启动有 token 时拉取 `/auth/me`
- H5：新增 `hasPermi` / `hasRole`（对齐 PC `*:*:*` 规则）；用户/角色/部门页操作按钮按权限隐藏
- H5：工作台改为请求后台菜单组装结果，不再以 mock 为权威
- 后端：新增 `GET /sys/menu/h5Workbench`，按当前用户可见菜单过滤 H5 约定节点并组装分组
- 数据：可选 Flyway/SQL 初始化「移动端工作台」目录与用户/部门/角色入口（`path` 以 `/pages/` 开头）；按钮 F 节点优先复用现有 `system:*` perms
- **非 BREAKING**：PC `/getRouters` 与菜单管理保持不变

## Capabilities

### New Capabilities

- `quick-h5-menu-perm`: H5 工作台菜单按角色下发；会话权限落库；按钮级 `hasPermi` 显隐；后端 h5Workbench 接口与菜单约定

### Modified Capabilities

- （无）本期不修改已归档主 specs 中的既有 capability 需求正文；行为增量集中在新 capability

## Impact

- 前端：`quick-h5`（store、permission 工具、workbench、system 业务页、`api/system/menu.ts`）
- 后端：`quickboot-module-system`（Menu Controller/Service 或 Permission 组装）
- 数据：`sys_menu` / `sys_role_menu`（初始化节点与角色勾选）
- 依赖设计文档：`docs/superpowers/specs/2026-08-16-quick-h5-menu-perm-design.md`
