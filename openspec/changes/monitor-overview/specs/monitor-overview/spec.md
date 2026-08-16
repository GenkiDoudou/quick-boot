## Purpose

Provides a first-class monitoring overview dashboard that aggregates login, behavior, request errors, slow SQL, and job failure metrics from existing tables for ops and engineering triage.

## ADDED Requirements

### Requirement: Overview summary API
系统 SHALL 提供需登录且校验权限字 `monitor:overview:query` 的 `GET /monitor/overview/summary`，按时间窗返回五组 KPI：用户与登录、访问与行为、请求与错误、慢 SQL、定时任务。指标口径 MUST 对齐 `docs/demo/system-monitor-dashboard-metrics.sql`（含状态值约定：`sys_logininfor.status` `'0'`/`'1'`；`sys_oper_log.status` 非 0 为错误；`sys_job_log.status` `'0'`/`'1'`）。

#### Scenario: Authorized summary for today
- **WHEN** 持有 `monitor:overview:query` 的用户请求 summary 且时间窗为今日
- **THEN** 返回总用户数、登录用户数、登录成功/失败次数、访问页数/会话数/活跃用户、热门页面 Top5、请求次数/错误次数、慢 SQL 次数与耗时摘要、任务成功/失败次数（及失败率若实现提供）

#### Scenario: Permission denied
- **WHEN** 未持有 `monitor:overview:query` 的已登录用户请求 summary
- **THEN** 系统拒绝该请求（鉴权失败），不返回聚合数据

### Requirement: Overview trends API
系统 SHALL 提供需同一权限的 `GET /monitor/overview/trends`，返回至少「登录成功/失败」与「请求/错误」两组分桶序列。今日与昨日 MUST 按小时分桶；本周、近 7 天、本月 MUST 按日分桶。时间比较 MUST 使用半开区间 `[start, end)`。

#### Scenario: Hourly buckets for today
- **WHEN** 用户请求 trends 且时间窗为今日
- **THEN** 返回的序列按小时分桶，覆盖该时间窗内有数据的桶（允许空窗无点）

#### Scenario: Daily buckets for last 7 days
- **WHEN** 用户请求 trends 且时间窗为近 7 天
- **THEN** 返回的序列按日分桶

### Requirement: Cross-module read boundaries
监控总览聚合 MUST NOT 让 `module-monitor` 直接访问 `module-system` 或 `module-quartz` 的持久化表/internal 类型。用户计数、登录日志聚合、操作日志聚合 MUST 经 `system::api`；定时任务日志聚合 MUST 经 `quartz::api`；`sys_client_track` 与 `sys_slow_sql` 可由 monitor 本模块查询。

#### Scenario: Modulith boundary respected
- **WHEN** 执行 Modulith 结构校验（如 `verify()`）
- **THEN** monitor 模块对 system/quartz 的依赖仅通过各自 `api` 命名接口，校验通过

### Requirement: Overview admin UI and menu
系统 SHALL 在管理端提供「态势总览」页面，调用 summary/trends API 展示五组指标与趋势，并提供下钻到既有监控子页（至少：操作日志、慢 SQL、前端监控、登录日志、定时任务日志）的入口。Flyway（或等价迁移）MUST 注册菜单与权限字 `monitor:overview:query`，并为管理员角色授权。

#### Scenario: Admin opens overview
- **WHEN** 管理员从监控菜单打开态势总览
- **THEN** 页面展示接真 KPI（非 mock），可切换约定时间窗并刷新数据

#### Scenario: Drill-down navigation
- **WHEN** 用户从总览中的慢 SQL 或请求错误入口下钻
- **THEN** 进入对应已有监控管理页
