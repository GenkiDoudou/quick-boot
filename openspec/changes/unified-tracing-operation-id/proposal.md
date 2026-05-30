## Why

`quick-ui` 已实现轻量前端行为监控（`src/monitor/` → `sys_client_track`），`quickboot` 已通过 Micrometer Tracing 在 MDC、`R.traceId` 与 `sys_oper_log.trace_id` 中记录**单次 HTTP 请求**的链路 id。但前端 `front_trace_id`（会话级自生成 ID）与后端**无关联**，且未区分「一次用户操作」与「一次 HTTP 请求」，导致一次点击触发多个接口时无法按同一业务操作聚合多条 oper_log，排障路径断裂。现需在**不改变后端 traceId/span 语义**的前提下，引入前端 **operationId** 打通 monitor ↔ oper_log 联查。

## What Changes

- **三层 ID 模型落地**：`operationId`（前端一次操作）+ `traceId`/`spanId`（后端一次请求及其内部节点）；一次操作触发 N 个 API → **同一 operationId、N 个不同 traceId**。
- **Flyway**：`sys_oper_log` 新增 `client_operation_id`；`sys_client_track` 新增 `operation_id`；`trace_id` 列语义废弃或过渡为存首个 API 的 `serverTraceId`。
- **后端**：读取 Header `X-Client-Operation-Id`，写入 MDC 与 oper_log 落库；CORS Allow-Headers 补充新头；**不**将 operationId 写入 `TraceIds.current()`。
- **前端**：新增 `operationContext.js`；axios 注入 `X-Client-Operation-Id`；monitor 事件携带 `operationId` 与 API 事件的 `serverTraceId`；废弃 `front_trace_id`。
- **管理端**：前端监控列表/详情展示 `operationId`；操作日志增加 `client_operation_id` 搜索与列展示。
- **可选（Phase 1）**：`requestTrace.js` 每请求独立 `traceparent`（策略 B）；Phase 2（OTLP 时间线、MQ 传播）**不在本期**。
- **非 BREAKING**：新增列与 Header；旧 `sys_client_track.trace_id` 数据无法按 operationId 联查，不回填。

## Capabilities

### New Capabilities

- `monitor-client-track-tracing`：前端行为监控上报、落库模型（`operation_id`、`events_json` 内 `operationId`/`serverTraceId`）、管理端列表/详情与按 operationId 联查的可验收需求。
- `client-operation-id`：前端 `operationContext`、axios Header 注入、monitor 字段与 `front_trace_id` 废弃；与后端 `X-Client-Operation-Id` 传播约定。

### Modified Capabilities

- `common-tracing`：补充 `clientOperationId` MDC 键约定（与 `traceId` 分离）；明确 operationId **不得**替代 W3C trace。
- `monitor-operlog`（变更内 delta，主 spec 尚未归档时以 change delta 为准）：`sys_oper_log` 新增 `client_operation_id` 落库与列表筛选；一次操作可对应多行 oper_log。

## Impact

- **后端**：`quickboot-common`（Filter/MDC）、`quickboot-web`（oper_log 实体/Mapper/Service、clientTrack 模块、Flyway `Vn+1`/`Vn+2`）、CORS 配置。
- **前端**：`quick-ui/src/monitor/`、`operationContext.js`（新）、`utils/request.js`、`views/monitor/clientTrack`、`views/monitor/operlog`（或等价操作日志页）。
- **依赖**：复用现有 Micrometer Tracing、`TraceIds`、`R.traceId`；Client HMAC 签名**不包含**新 Header。
- **真源文档**：`docs/superpowers/specs/2026-05-30-unified-tracing-design.md`。
