## ADDED Requirements

### Requirement: sys_client_track operation_id 列

系统 MUST 通过 Flyway 为 `sys_client_track` 新增可索引列 **`operation_id`**（VARCHAR(64)，NOT NULL DEFAULT ''），作为前端操作与 oper_log 联查的**主关联键**。现有 `trace_id` 列 MAY 保留用于过渡（存批次内首个 API 的 serverTraceId 或留空），但 MUST NOT 再表示 `front_trace_id` 会话语义。

#### Scenario: 迁移后可写入 operation_id

- **WHEN** Flyway 迁移已执行且客户端上报含 `operationId`
- **THEN** 插入 `sys_client_track` 时 `operation_id` MUST 持久化该值

---

### Requirement: 监控事件字段 operationId 与 serverTraceId

前端 monitor MUST 在事件 payload 中携带：**click/关键交互** — `operationId`（click 时可通过 `beginOperation` 生成）；**api_slow / api_error** — `operationId`（若有活跃 operation）与 **`serverTraceId`**（等于当次 API 响应 `R.traceId`，即该 HTTP 请求的后端 traceId）。同一 operation 触发的多个 API 事件的 `serverTraceId` MUST 允许互不相同。

#### Scenario: 多 API serverTraceId 不同

- **WHEN** 一次 operation 触发两个业务 API 且均返回 `R.traceId`
- **THEN** 两个 api 类型监控事件的 `serverTraceId` MUST 互不相同（在正常后端 trace 行为下）

#### Scenario: api 事件含 operationId

- **WHEN** 在活跃 operation 内发生 api_slow 事件
- **THEN** 该事件 MUST 含与当前 axios Header 一致的 `operationId`

---

### Requirement: 上报批次 operationId

`POST /monitor/clientTrack/report` 的请求体 MUST 支持批次级 **`operationId`** 字段（取批次内主 operationId 或首个非空值）。后端 MUST 将其写入 `sys_client_track.operation_id`。`events_json` 内元素 MUST 保留 per-event 的 `operationId` 与 `serverTraceId`（API 事件）。

#### Scenario: 上报落库 operation_id

- **WHEN** 客户端上报 `{ operationId: "op-x", events: [...] }`
- **THEN** 对应 `sys_client_track` 行的 `operation_id` MUST 为 `op-x`

---

### Requirement: 前端监控管理端展示

`quick-ui` 前端监控列表/详情页 MUST 展示 **`operationId`**（列或详情字段）。详情中 API 类事件 MUST 展示 **`serverTraceId`**，并 MUST 支持复制以便跳转 oper_log 或日志 grep。

#### Scenario: 列表可见 operationId

- **WHEN** 管理员打开前端监控列表且存在含 operationId 的记录
- **THEN** 列表 MUST 展示 `operation_id` 列或等价字段

---

### Requirement: 联查路径 operationId

系统 MUST 支持排障路径：给定 `operationId`，管理员 MUST 能从前端监控 events 看到该操作触发的 API 列表（含各 `serverTraceId`），并在操作日志中通过 **`client_operation_id`** 筛选出同一 operation 触发的**多条** oper_log（各行 `trace_id` 可不同）。

#### Scenario: operationId 对应多条 oper_log

- **WHEN** 一次带 operationId 的用户操作触发两个 Controller 请求
- **THEN** `sys_oper_log` MUST 存在两行且 `client_operation_id` 相同、`trace_id` 不同
