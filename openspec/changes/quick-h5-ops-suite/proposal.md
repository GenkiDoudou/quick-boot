## Why

quick-h5 已具备用户/角色/部门与工作台菜单权限，但参数、文件、字典、客户端及监控类（任务、日志、在线、慢 SQL）仍只能在 PC 操作。运维人员需要在移动端完成常用查询与轻量写操作，与现有 H5 CRUD 体验一致。

## What Changes

- H5 系统域：参数设置、字典（类型+数据）、OAuth 客户端、文件分类、文件管理（含上传选分类、预览/下载/删除）
- H5 监控域：定时任务（启停/执行一次，无 Cron 编辑）、调度日志、登录日志、操作日志、在线用户、慢 SQL
- 统一复用：`Qb*` 组件、`usePagedList`（或 GET 分页适配）、`hasPermi`、`toastErr`；直连现有后端 API，不新增 H5 BFF
- Flyway：在「移动端工作台」下挂系统/监控菜单 C 节点（`path` 以 `/pages/` 开头），按钮 F 复用 PC 已有 perms；绑定 admin
- `pages.json` 注册各页；不做导入导出；定时任务不提供移动端整单新增/Cron 编辑
- **非 BREAKING**：PC 管理端与现有 API 契约不变

## Capabilities

### New Capabilities

- `quick-h5-ops-system`: H5 系统运维页（参数、字典、客户端、文件分类、文件）及对应菜单种子
- `quick-h5-ops-monitor`: H5 监控运维页（定时任务、调度/登录/操作日志、在线用户、慢 SQL）及对应菜单种子

### Modified Capabilities

- （无）不修改已归档主 specs 需求正文；增量集中在上述新 capability

## Impact

- 前端：`quick-h5`（`api/system/*`、`api/monitor/*`、`pages/system/*`、`pages/monitor/*`、`pages.json`）
- 后端：原则上无新接口；Flyway 仅增 H5 菜单与角色绑定
- 依赖设计：`docs/superpowers/specs/2026-08-16-quick-h5-ops-suite-design.md`
- 前置：`quick-h5-menu-perm`（工作台 + hasPermi）已落地
