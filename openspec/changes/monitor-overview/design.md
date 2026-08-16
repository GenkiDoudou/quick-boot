## Context

See proposal.md — Why。权威产品范围见 `docs/superpowers/specs/2026-08-14-monitoring-system-redesign-design.md` §4①期与 §6；指标口径见 `docs/demo/system-monitor-dashboard-metrics.sql`。

现状：`module-monitor` 已有 clientTrack / slowSql / traceChain；`system.api` 仅有按 operationId/traceId 的 `OperLogMonitorQuery`，无时间窗聚合；`SysUserQueryFacade` 无活跃用户计数；登录日志与定时任务日志无跨模块只读 Facade。`monitor` 模块 `allowedDependencies` 目前仅为 `system :: api`（任务日志在 `module-quartz`）。

约束：①期不上 RUM/告警新表；不扫 `events_json` 做 KPI；Modulith 禁止 monitor 直查 system/quartz 表。

## Goals / Non-Goals

**Goals:**

1. 提供接真的 overview summary / trends API，口径对齐 demo SQL。
2. 经 Facade 跨域只读：system（user / logininfor / operlog）+ quartz（job_log）；本模块直查 client_track / slow_sql。
3. quick-ui 正式「态势总览」页 + 权限菜单，可下钻既有子页。

**Non-Goals:**

- Web Vitals / Issue / 告警 / H5；投屏深色壳一比一移植（默认管理端浅色 KPI）；WebSocket 实时推送。

## Decisions

### 1. 聚合落在 `monitor.internal.overview`

- Controller：`/monitor/overview/summary`、`/monitor/overview/trends`；权限 `monitor:overview:query`。
- Service 并发查多源后组装 DTO（可用 `CompletableFuture` / 顺序查询，实现自定；须控制总超时合理）。
- 备选：把聚合放在 `app` → 否决（监控域应自洽，且破坏「子页同属 monitor」叙事）。

### 2. 跨域 Facade 拆分

| 数据 | 归属 | 做法 |
|------|------|------|
| 总用户数（`del_flag='0'`） | system | 扩展 `SysUserQueryFacade` 或新增 `UserStatsQuery#countActiveUsers()` |
| 登录用户/成功失败/登录趋势 | system | 新增 `LoginInfoMonitorQuery`（时间窗聚合 + 分桶） |
| 请求/错误数与趋势 | system | 扩展 `OperLogMonitorQuery` 增加时间窗 count / bucket 聚合 |
| 任务成功失败/最近失败 | quartz | 新增 `JobLogMonitorQuery`；**monitor `allowedDependencies` 增加 `quartz :: api`** |
| 访问页/会话/活跃用户/热门页 | monitor | 本模块 Mapper 聚合 `sys_client_track` |
| 慢 SQL 计数/均耗时/Top | monitor | 本模块已有 slowSql 能力上聚合 |

备选：全部经 system 转发 job → 否决（job 不在 system，硬塞破坏边界）。

### 3. 时间窗约定

Query 参数：`range=today|yesterday|week|last7d|month`（或显式 `startTime`/`endTime`，二选一以 `range` 为主）。

分桶：`today` / `yesterday` → 按小时；`week` / `last7d` / `month` → 按日。与 demo SQL 注释一致。

区间半开：`create_time/oper_time/login_time >= start AND < end`。

### 4. 前端交互

- 新页：`views/monitor/overview/index.vue`（或等价）；API `api/monitor/overview.js`。
- 布局：管理端浅色 KPI 卡 + 分组（用户登录 / 访问行为 / 请求错误 / 慢 SQL / 任务）；趋势图用现有 ECharts 习惯。
- 下钻：KPI 链到 `/monitor/operlog`、`/monitor/slowSql`、`/monitor/clientTrack`、`/monitor/job-log`、`/monitor/logininfor` 等既有路由（路径以实现时菜单 component 为准）。
- 菜单：挂在「监控」目录下靠前；Flyway 插入 menu + `monitor:overview:query` + admin 授权。

### 5. 性能

- ①期直接 SQL 聚合即可；热门页 LIMIT 5；趋势分桶结果行数有限。
- 不为 overview 引入缓存层（可列为后续）；禁止 `SELECT events_json`。

## Risks / Trade-offs

- [跨模块 Facade 变多] → 每个 Facade 只暴露聚合结果 DTO，不暴露实体；Modulith verify 必过。
- [大表全表时间窗扫描] → 依赖现有 `create_time` / `oper_time` / `login_time` 索引；验收注明大数据量下可后续加汇总表。
- [allowedDependencies 扩 quartz] → 在 package-info 显式声明；design/tasks 写清，避免漏改。
- [深色投屏 vs 管理端] → 默认浅色；需要投屏时可另开 change 套深色壳复用同一 API。

## Migration Plan

1. system.api / quartz.api 聚合接口 + 实现。
2. monitor overview service/controller；更新 `allowedDependencies`。
3. Flyway 菜单权限。
4. quick-ui 总览页与路由。
5. 对照 `system-monitor-dashboard-metrics.sql` 手工/脚本验收时间窗口径。

回滚：删 overview 代码与菜单 Flyway（已执行环境删菜单行）、恢复 package-info 依赖；以 Git 为准。

## Open Questions

- 菜单 `menu_id` 与 component 路径：实现时对照现网 `sys_menu` 占用段后写入 Flyway（不阻塞规格）。
- `range` 枚举英文 key 与前端文案映射：实现时与 demo 中文标签对齐即可。
