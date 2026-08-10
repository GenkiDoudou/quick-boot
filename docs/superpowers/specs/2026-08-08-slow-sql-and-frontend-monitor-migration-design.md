# 慢 SQL + 前端监控 + 全链路迁移设计

日期：2026-08-08  
状态：已定稿（OpenSpec change：`slow-sql-and-frontend-monitor`）  
来源：`bak/quickboot`（`common/monitor/slowsql`、`system/slowsql`、`monitor/clienttrack`、`monitor/tracechain`、app Druid Filter）+ `bak/quick-ui`（`src/monitor/**`、`views/monitor/{slowSql,clientTrack,traceChain}`）  
对齐：`2026-08-08-spring-modulith-maven-layering-design.md` 新域模板；策略同 quartz / tool 独立模块先例。

## 1. 背景与目标

现网已迁入操作/登录日志、定时任务、在线用户与代码生成等能力；**慢 SQL 落库与管理、前端行为监控（含埋点）、全链路聚合**仍留在 `bak`。

目标：

1. 新建独立 Maven / Modulith 模块 `quickboot-module-monitor`，承载慢 SQL、clientTrack、traceChain 的业务实现。
2. 行为对齐 bak 已实现全量：采集落库、管理 API、同步 Excel 导出、前端埋点默认开启、管理页（批次 / 事件链路 / 行为轨迹 / 全链路）。
3. 全链路通过 `module-system` 的 `api` 只读查询操作日志，禁止跨模块引用 `internal`。
4. 修复现网操作日志采集中 `clientOperationId` 写死空串的问题，保证与前端 `X-Client-Operation-Id` 可关联。

非目标：

- 异步导出中心（`SlowSqlBizExportHandler` / exporttask）。
- 将 operlog / job / online 迁出 `module-system`。
- 迁入积木报表本体（可保留 `sql_source=JIMU` 与 URI 前缀配置，与 Jimu 迁移设计兼容）。
- 合并 `common` / `core`；不把监控 CRUD 塞回 `module-system`。

## 2. 已确认决策

| 项 | 选择 |
|----|------|
| 范围 | 慢 SQL + 前端监控（含埋点）+ 全链路 traceChain |
| 交付节奏 | 同一 OpenSpec change 一起交付 |
| 完整度 | 对齐 bak 已实现全量 |
| Maven / Modulith | 新建 `quickboot-module-monitor` |
| 实现路径 | 方案 1：独立 monitor + 跨域仅用 `system.api` |
| 慢 SQL 导出 | 同步 `ExcelUtils.exportExcel` |
| 前端埋点 | 迁入且默认开启（可配置关闭） |
| 包根 | `io.github.genkidoudou.monitor` |

## 3. 架构与边界

```text
quickboot-app
  ├── Druid SlowSqlFilter 装配（数据源侧）
  ├── quickboot-module-system   （operlog；新增 api Facade）
  ├── quickboot-module-monitor  ← 新建
  ├── quickboot-module-quartz / tool …
  └── core → common
```

依赖方向：`app → module-* → core → common`；`module-monitor` 仅依赖 `module-system` 的 **`api`**。

| 项 | 约定 |
|----|------|
| Artifact / 目录 | `quickboot-module-monitor`；父 POM `<modules>` 注册；`quickboot-app` 增加依赖 |
| Modulith | `@ApplicationModule(displayName = "monitor")`；开放 `api`；`allowedDependencies` 含 `system::api` |
| 基包注册 | `ApplicationModuleSourceFactory#getModuleBasePackages` 追加 `io.github.genkidoudou.monitor` |
| common | 慢 SQL 采集事件模型、`SlowSqlProperties`、`SlowSqlCaptureSupport`、MyBatis mapper_id 拦截器（**无业务表**） |
| app | 注册 Druid Filter；不写业务 Controller |
| Controller | 留在 `monitor.internal` |

### 3.1 包结构

```text
io.github.genkidoudou.monitor/
  package-info.java                 @ApplicationModule
  api/
    package-info.java               @NamedInterface("api")
    （本期可空或仅占位；对外契约按需再加）
  internal/
    slowsql/                        controller · service · entity · mapper · persist · dto
    clienttrack/                    controller · service · entity · mapper · support · dto
    tracechain/                     controller · service · dto
    config/
```

### 3.2 system.api 扩展

- `OperLogMonitorQuery`：按 `clientOperationId` 集合 / 时间窗只读查询，结果上限对齐 bak（如 200）。
- 若轨迹/全链路需要菜单 path→名称：增加 `MenuPathQuery`（或复用已有菜单只读 api）。
- **禁止** monitor 直接依赖 system 的 entity / mapper / internal 包。

## 4. API / 数据 / 配置

### 4.1 HTTP API（行为对齐 bak）

**慢 SQL** `/monitor/slowSql`（建议 `@IgnoreLogger(ALL)`）

| 方法 | 路径 | 权限 |
|------|------|------|
| GET | `/list`、`/{slowId}` | `monitor:slowSql:query` |
| POST | `/export` | `monitor:slowSql:export` |
| POST | `/remove`、`/clean` | `monitor:slowSql:remove` |

**前端监控** `/monitor/clientTrack`

| 方法 | 路径 | 权限 |
|------|------|------|
| POST | `/report` | 需登录，**不**校验菜单权限 |
| GET | `/list`、`/timeline`、`/timeline/page` | `monitor:clientTrack:list` |
| POST | `/remove`、`/clean` | `monitor:clientTrack:remove` |

**全链路** `/monitor/traceChain`

| 方法 | 路径 | 权限 |
|------|------|------|
| GET | `/graph` | `monitor:traceChain:query` |

分页：管理列表对齐现网 C7 / 已修监控页（扁平 query 与 `{current,size,param}` 映射）。

### 4.2 Flyway

- `sys_slow_sql`：自 bak 定稿列一次建表（含 `sql_source`、`sql_type`、`mapper_id`、`sql_text`、`cost_time`、`trace_id`、`client_operation_id`、`client_id`、请求上下文等及索引）。
- `sys_client_track`：合并 bak 终态列（`operation_id`、`trigger_action`、`session_id`、`page_visit_id`、`browser_visit_id`、`page_path`、`events_json` 等及索引）。
- 菜单种子（挂现网监控目录）：慢 SQL、前端监控、事件链路、行为轨迹、全链路 + 按钮权限；admin 角色授权。
- **不改**既有 `sys_oper_log` 表结构（已有 `client_operation_id` / `client_id`）；只修采集写入逻辑。

### 4.3 采集与配置

- `qc.monitor.slow-sql.*`：`capture-enabled`、`threshold-ms`、`log-enabled`、`async-enabled`、`export-max-rows`、`max-sql-length`、`ignore-sql-contains`、`jimu-uri-prefixes`。
- Druid Filter（app）超阈值发事件 → monitor 内异步/同步 PersistListener 落库；`ignore-sql-contains` 须覆盖 `sys_slow_sql` 自写入。
- `OperLogPublishingAspect`：从请求头写入 `clientOperationId`（`X-Client-Operation-Id`）与 `clientId`（`X-Client-Id`），替换当前空串写死。
- `/monitor/clientTrack/report`：需登录；可对 XSS 忽略该路径（body 含事件 JSON）；**不**匿名放行。

## 5. 前端（quick-ui）

### 5.1 埋点（默认开启）

- 迁入 `src/monitor/**`（plugin、report、requestTrace、config 等）及上报所依赖的工具（如 `clientSign`，若现网缺失）。
- 应用入口注册 `userMonitorPlugin`；`request` 拦截器挂钩 `X-Client-Operation-Id` / 相关 trace 头。
- `VITE_APP_MONITOR_ENABLED` 默认 `true`；`INTERVAL` / `IDLE_MS` / `EXCLUDE_PAGES` 等与 bak 对齐；监控管理页路径加入 `excludePages`。

### 5.2 管理页

| 页面 | 说明 |
|------|------|
| `views/monitor/slowSql` | 列表 / 详情 / 导出 / 删除 / 清空 |
| `views/monitor/clientTrack` | `index`、`events`、`timeline` 及批次详情等组件 |
| `views/monitor/traceChain` | Network 全链路图 |
| `api/monitor/{slowSql,clientTrack,traceChain}.js` | 对齐现网 `request` 与权限指令 |

改造：分页、权限、样式对齐已迁监控页；不引入新 UI 框架。

## 6. 验收标准

1. 超阈值 SQL 写入 `sys_slow_sql`；列表筛选、详情、导出、删除、清空与权限字可用。  
2. 登录后前端自动上报；管理端批次 / 事件链路 / 行为轨迹可查；删除 / 清空可用。  
3. 有前端操作时，操作日志 `client_operation_id` 非空；`GET /monitor/traceChain/graph` 能聚合 track + operlog + slowSql。  
4. `VITE_APP_MONITOR_ENABLED=false` 或 `qc.monitor.slow-sql.capture-enabled=false` 后不再产生对应新数据。  
5. Modulith `verify()` 通过；monitor 不引用 system `internal`。  
6. 登录与既有 operlog / job / online 等不回归。

## 7. 风险与缓解

| 风险 | 缓解 |
|------|------|
| 一次迁三块体量大 | tasks 分：脚手架+Flyway → 慢 SQL → clientTrack+埋点 → operlog 头修复 + Facade → traceChain → 联调 |
| Modulith 跨域偷表 | 强制走 `system.api` |
| 埋点默认开带来流量/库膨胀 | exclude 监控页；配置可关；异步落库 |
| Druid 自写递归 | `ignore-sql-contains` 含慢 SQL 表相关片段 |
| 与 Jimu 迁入并行 | 保留 JIMU 来源枚举与 URI 前缀，不阻塞本期 |

## 8. 实现顺序建议

1. `module-monitor` 脚手架 + Flyway（表 + 菜单）  
2. common 采集 + app Druid Filter + 慢 SQL CRUD / 导出 / 前端页  
3. clientTrack 后端 + 埋点插件 + 三页管理端  
4. operlog 请求头写入 + `OperLogMonitorQuery`（及菜单 path 查询若需要）  
5. traceChain + 联调验收清单  

## 9. 实现路径说明（方案 1）

相对「采集沉 common 过厚」或「monitor 直查 `sys_oper_log`」：本方案用独立域模块 + 显式 Facade，与 quartz/tool 先例及 Modulith 一对一域边界一致，便于以后整模块外提。
