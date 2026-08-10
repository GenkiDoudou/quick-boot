## Why

现网已具备操作/登录日志、定时任务、在线用户等监控能力，但 **慢 SQL 落库与管理、前端行为埋点与轨迹、全链路聚合** 仍留在 `bak`，排障时无法还原「页面操作 → HTTP → SQL」链路。需按 Modulith 新域模板迁入独立 `module-monitor`，与已定稿产品设计对齐落地。

## What Changes

- 新建 Maven / Modulith 模块 **`quickboot-module-monitor`**（包根 `io.github.genkidoudou.monitor`，`api` / `internal`）。
- 迁入慢 SQL：Druid 采集事件、`sys_slow_sql` 管理 API、同步 Excel 导出；common 放采集模型与拦截器，app 装配 Druid Filter。
- 迁入前端监控：`sys_client_track` 上报/查询/轨迹；`quick-ui` 埋点插件默认开启 + 管理页（批次 / 事件链路 / 行为轨迹）。
- 迁入全链路 `traceChain`：聚合 clientTrack + operlog + slowSql；跨域经 `system.api` 只读 Facade，禁止引用 system `internal`。
- 修复操作日志采集：从请求头写入 `clientOperationId` / `clientId`（替换空串写死）。
- Flyway：两张业务表 + 监控菜单/按钮权限与管理员授权。
- **不迁**异步 exporttask；**不迁**积木报表本体（保留 `sql_source=JIMU` 配置位）。
- **BREAKING（相对 bak）**：包名改为 `io.github.genkidoudou.monitor.*`；能力落独立模块而非 bak 的 system 包；去掉 `SlowSqlBizExportHandler`。

权威产品设计：`docs/superpowers/specs/2026-08-08-slow-sql-and-frontend-monitor-migration-design.md`。

## Capabilities

### New Capabilities

- `maven-module-monitor`: `quickboot-module-monitor` 脚手架、Modulith 边界、`app` 依赖与基包注册。
- `monitor-slow-sql`: 慢 SQL 采集落库、分页/详情/导出/删除/清空与权限。
- `monitor-client-track`: 前端行为批次上报与管理端列表/轨迹/删除；埋点插件默认开启。
- `monitor-trace-chain`: 全链路图聚合（track + operlog + slowSql）。

### Modified Capabilities

- （无既有主 specs 能力需改写；operlog 请求头写入与 `OperLogMonitorQuery` 为 system 内部/api 扩展，不另立 delta。）

## Impact

- 后端：新建 `quickboot-module-monitor`；`common` 增加慢 SQL 采集支撑；`app` 装配 Druid Filter；`module-system` 增加 `api` Facade 并修正 operlog 头字段；父 POM / Modulith 基包扩展。
- 前端：`quick-ui` 增加 `src/monitor/**`、`views/monitor/{slowSql,clientTrack,traceChain}` 与对应 API。
- 库表：`sys_slow_sql`、`sys_client_track`；菜单权限字 `monitor:slowSql:*`、`monitor:clientTrack:*`、`monitor:traceChain:query`。
- 配置：`qc.monitor.slow-sql.*`；前端 `VITE_APP_MONITOR_*`。
