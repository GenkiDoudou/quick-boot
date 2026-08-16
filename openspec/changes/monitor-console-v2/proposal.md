## Why

监控控制台仍缺排障所需的时间/结果筛选与详情 enrichment；行为「页→操作→页」能力随 clientTrack 下线后需用 Lite RUM 重建；运维需要跨操作/登录/慢 SQL 的统一检索入口。概览已决定交给 BI，应物理下线以免双入口。

## What Changes

- **BREAKING（产品）**：删除「监控概览」菜单与前后端实现（`overview` API/页面/服务）。
- 增强「请求链路」：时间区间、成功/失败筛选；右侧 Index 全字段 + Span `attrsJson` 展开；关联跳转（同 operationId、慢 SQL、操作日志等）。
- 新增「用户行为」：基于 `sys_rum_event`（pv/action）按用户/会话展示操作时间线；ingest 补写 `uin`（DDL + 登录态写入）。
- 新增「日志中心」：聚合操作日志、登录日志、慢 SQL 的合并列表与维度筛选（一期近似分页）；保留原明细菜单。
- Flyway：停用 overview 菜单；`sys_rum_event.uin`；用户行为 / 日志中心菜单与权限。

## Capabilities

### New Capabilities

- `monitor-overview-retire`: 下线监控概览菜单与实现，禁止残留路由/component
- `lite-trace-console`: 请求链路查询增强与详情 enrichment / 关联跳转
- `user-behavior`: 基于 Lite RUM 的用户/会话行为时间线与查询 API
- `log-hub`: 三源日志合并检索入口（oper / login / slow_sql）

### Modified Capabilities

- （无主库 `openspec/specs/` 存量；行为变更均以 New Capabilities 承载）

## Impact

- 后端：`quickboot-module-monitor`（删除 `internal/overview`；扩展 `litetrace` 查询与 ingest；新增 userBehavior / logHub）
- 前端：`quick-ui` 删除 overview；增强 `liteTrace`；新增 `userBehavior`、`logHub` 页与 API
- 数据库：Flyway V27+（菜单 + `uin` 列）；经 `system::api` 读操作/登录日志
- 不恢复 `sys_client_track`；不改 BI
