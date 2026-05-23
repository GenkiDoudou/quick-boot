## Why

主工程缺少与《操作日志需求文档》一致的可审计能力：无法在后台按条件检索、追溯单次请求的链路 id，也无法对日志做生命周期管理（删除、清空、导出）。在已有登录日志等监控能力的基础上补齐操作日志，可满足安全与运维排障诉求。

## What Changes

- 新增表 `sys_oper_log`（若依语义字段 + `trace_id`）及 Flyway 迁移；可选菜单/字典种子。
- 新增**宽切面**环绕 Web 映射方法，在 `finally` 中发布应用事件；事件载荷携带 **`traceId`（与 `TraceIds`/MDC 同源）**、参数、返回值、异常、耗时等。
- 新增 **`@IgnoreLogger`**（`ALL`/`PARAMS`/`RESULT`）及可选 **`@OperLogMeta`**；配置化 **URL 前缀排除**（actuator、swagger、静态资源等）。
- 新增同步 **`@EventListener`** 将事件脱敏后写入 `sys_oper_log`；**写库失败不阻断**业务请求。
- 新增监控 REST：`GET /monitor/operlog/list`、`GET /monitor/operlog/{operId}`、`POST /monitor/operlog/remove`、`POST /monitor/operlog/clean`、`POST /monitor/operlog/export`（删除/清空为 POST，与 `AGENTS.md` 约定一致）；权限 `monitor:operlog:query|remove|export`。
- `quick-ui`：操作日志列表/详情/删/清空/导出及路由、API、权限指令。
- **非 BREAKING**：均为新增表与新增接口；不改变现有对外契约（除新增切面写库带来的性能开销，可通过排除与忽略注解控制）。

## Capabilities

### New Capabilities

- `monitor-operlog`：覆盖操作日志**采集落库**（切面、事件、监听器、脱敏、`trace_id`）、**管理端 API**、**导出行数上限**、**前端页面与菜单权限** 的可验收需求。

### Modified Capabilities

- （无）本期不修改 `openspec/specs/` 下既有规范的 REQUIREMENTS 文本；实现阶段**复用** `common-field-desensitization`、`common-tracing` 等已有能力，不在主 spec 中重新定义其行为。

## Impact

- **后端**：`quickboot-web`（Flyway、`monitor/operlog` 包）、`quickboot-common`（切面/注解/事件/属性，具体包名以实现阶段为准，避免循环依赖）。
- **前端**：`quick-ui` 下 `api/monitor/operlog`、视图与路由。
- **依赖**：Sa-Token、MyBatis-Plus、Flyway；与 Micrometer MDC / `TraceIds` 对齐；导出与项目现有 Excel 方案一致。
- **真源文档**：`docs/superpowers/specs/2026-05-16-operlog-design.md`、`docs/superpowers/plans/2026-05-16-operlog.md`。
