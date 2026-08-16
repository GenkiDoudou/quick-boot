## Why

监控相关原始数据（登录、操作日志、前端埋点、慢 SQL、定时任务）已在库内可查，但运维/研发仍缺少一张**正式的态势总览**：现有能力分散在各子页，`docs/demo` 大屏仅为 mock。体系重设计将「大盘接真 + 正式路由」定为①期，先提供可下钻的统一入口，再演进 RUM 与告警。

权威产品设计：`docs/superpowers/specs/2026-08-14-monitoring-system-redesign-design.md`（①期）及指标口径 `docs/demo/system-monitor-dashboard-metrics.sql`、`docs/superpowers/specs/2026-08-11-system-monitor-dashboard-design.md`。

## What Changes

- 在 `quickboot-module-monitor` 新增 **监控态势总览** API：`GET /monitor/overview/summary`、`GET /monitor/overview/trends`（时间窗：今日 / 昨日 / 本周 / 近7天 / 本月）。
- 指标分组对齐既有 demo SQL：用户与登录、访问与行为、请求与错误、慢 SQL、定时任务；本模块直查 `sys_client_track` / `sys_slow_sql`，其余经 `system.api` 只读 Facade，禁止依赖 system `internal`。
- `quick-ui` 新增「态势总览」页面与监控菜单项；权限字 `monitor:overview:query`；卡片可下钻到既有 monitor 子页（带时间窗 query 可选）。
- Flyway：菜单 / 按钮权限与管理员授权（无新业务宽表；①期不上 RUM / 告警表）。
- **不含**：Web Vitals / Issue、告警规则、quick-h5 SDK、采集协议大改、Sourcemap。

## Capabilities

### New Capabilities

- `monitor-overview`: 态势总览聚合 API、权限与 quick-ui 总览页（接真数据、时间窗、下钻入口）。

### Modified Capabilities

- （无；主 `openspec/specs` 尚无已归档的 overview 能力需改写。）

## Impact

- 后端：`module-monitor` 增加 `overview` 包（controller / service / dto）；`module-system` 按需扩展 `api` 只读查询（operlog / logininfor / user 计数 / job_log 失败数等）。
- 前端：`quick-ui` 增加 `views/monitor/overview`（或等价路径）、`api/monitor/overview.js`、路由与菜单。
- 库表：无①期新业务表；仅菜单权限数据变更。
- 依赖：复用现有表与 `system-monitor-dashboard-metrics.sql` 口径；视觉默认管理端浅色 KPI 布局（指标分组与 demo 一致，不强制投屏深色壳）。
