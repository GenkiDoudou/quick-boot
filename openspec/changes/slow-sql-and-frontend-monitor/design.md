## Context

权威产品设计见 `docs/superpowers/specs/2026-08-08-slow-sql-and-frontend-monitor-migration-design.md`。本文件为 OpenSpec 实现向设计。

现状：现网已完成 Modulith（`common` / `core` / `module-system` / `module-quartz` / `module-tool` / `app`），监控已有 operlog / logininfor / job / online 等；慢 SQL、前端行为监控、全链路仍在 `bak`。现网已用 Druid；`sys_oper_log` 已有 `client_operation_id` / `client_id` 列，但采集切面仍将 `clientOperationId` 写死为空串。异步导出中心不存在。

约束：独立 `module-monitor`；跨域仅 `system.api`；同步 Excel 导出；前端埋点默认开启；行为对齐 bak 全量。

## Goals / Non-Goals

**Goals:**

1. 新建 `quickboot-module-monitor` 并注册到父反应器与 `app`，Modulith `verify()` 通过。
2. 慢 SQL：common 采集支撑 + app Druid Filter + 落库/管理 API/同步导出/前端页。
3. 前端监控：上报与管理 API + 埋点插件 + 批次/事件链路/行为轨迹页。
4. 全链路：聚合 track + operlog（经 Facade）+ slowSql；operlog 头字段写入修复。
5. Flyway 建表与菜单权限。

**Non-Goals:**

- 异步 exporttask；把 operlog/job/online 迁出 system；迁入 Jimu 本体；monitor 直查 `sys_oper_log` 表。

## Decisions

### 1. 独立 Maven / Modulith 模块

- `quickboot-module-monitor` ↔ Application Module `monitor` 一对一；包根 `io.github.genkidoudou.monitor`。
- `allowedDependencies` 含 `system::api`。
- 备选：塞进 `module-system` → 否决（用户已选独立域）；直查 operlog 表 → 否决（破坏边界）。

### 2. 分层落点

```text
common: SlowSqlProperties / CaptureSupport / 事件 / MyBatis mapper_id 拦截器
app:    Druid SlowSqlFilter 装配
monitor.internal: slowsql / clienttrack / tracechain CRUD 与 PersistListener
system.api: OperLogMonitorQuery（+ 按需 MenuPathQuery）
```

### 3. 从 bak 迁入并改写

- HTTP / 权限字对齐 bak：`/monitor/slowSql`、`/monitor/clientTrack`、`/monitor/traceChain`。
- **删除** `SlowSqlBizExportHandler`；导出走 `ExcelUtils.exportExcel`。
- `/report` 需登录、不校验菜单权限；可 XSS ignore；不匿名放行。
- `OperLogPublishingAspect` 读取 `X-Client-Operation-Id` / `X-Client-Id`。

### 4. Flyway 与前端

- 下一可用 `V*`：合并终态 DDL 建 `sys_slow_sql`、`sys_client_track`；菜单挂现网监控目录；admin 授权；menu_id 避开已占用段。
- 前端迁 `src/monitor/**` 与管理页；`VITE_APP_MONITOR_ENABLED` 默认 true；监控页进 `excludePages`。

### 5. 配置

- `qc.monitor.slow-sql.*`（含 `ignore-sql-contains` 防自写递归、`jimu-uri-prefixes` 预留）。
- 异步落库默认开启（与 bak 一致）。

## Risks / Trade-offs

- [一次迁三块体量大] → tasks 分阶段：脚手架+Flyway → 慢 SQL → clientTrack+埋点 → Facade+operlog 头 → traceChain → 联调。
- [Modulith 跨域偷表] → 强制 Facade；CI `verify()`。
- [埋点默认开流量] → exclude 监控页；环境变量可关。
- [Druid 自写递归] → ignore 慢 SQL 表相关 SQL 片段。
- [与 Jimu 并行] → 保留 JIMU 枚举与 URI 前缀，不阻塞本期。

## Migration Plan

1. POM 脚手架 + package-info + app 依赖 + 基包。
2. Flyway 表与菜单。
3. common 采集 + app Filter + 慢 SQL 后端/前端。
4. clientTrack 后端 + 埋点 + 管理页。
5. system.api Facade + operlog 头修复。
6. traceChain + Modulith verify + 冒烟。

回滚：删模块与依赖、Flyway 文件（已执行环境清表）、前端文件；以 Git 为准。

## Open Questions

- Flyway 最终序号、菜单精确 ID：实现时核对现网后写入 tasks 勾选说明。
- `MenuPathQuery` 是否必须：实现时若轨迹展示仅依赖 path 字符串可先不做；需要菜单名再补 Facade。
