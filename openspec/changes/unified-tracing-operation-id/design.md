## Context

`quick-ui` 已实现轻量前端行为监控（`src/monitor/` → `POST /monitor/clientTrack/report` → `sys_client_track`），使用 `sessionStorage.front_trace_id` 作为会话级 ID，**与后端 Micrometer trace 无关联**。`quickboot` 已通过 Micrometer Tracing 在 MDC、`R.traceId`、`sys_oper_log.trace_id` 记录**单次 HTTP 请求**的 traceId；操作日志宽切面已落地（change `add-oper-log`）。

**真源设计**：`docs/superpowers/specs/2026-05-30-unified-tracing-design.md`（2026-05-30 修订）明确：**traceId/span 归属后端单请求**；**operationId 归属前端单次用户操作**；一次操作触发 N 个 API → 同一 operationId、N 个不同 traceId。

**现状差距**：`sys_client_track` 无 `operation_id`；`sys_oper_log` 无 `client_operation_id`；前端无 `operationContext`；axios 未传 `X-Client-Operation-Id`。

## Goals / Non-Goals

**Goals:**

- 落地三层 ID 模型：`operationId`（前端操作）+ `traceId`/`spanId`（后端请求内）。
- Flyway 增列；后端 Filter/切面读取 Header 落库；前端 `operationContext` + axios + monitor 字段迁移。
- 管理端：前端监控与操作日志均支持按 operationId 维度展示/搜索；API 事件展示 `serverTraceId`（= 当次 `R.traceId`）。
- Phase 1 验收：一次 click 触发 ≥2 API → 同一 operationId、不同 traceId、多条 oper_log。

**Non-Goals:**

- Phase 2：OTLP/Jaeger 时间线 UI、MQ traceparent 传播、`sys_trace_span` 表。
- 历史 `front_trace_id` 数据回填 operationId。
- Tab 会话级共用 traceId（明确禁止）。
- 修改 Micrometer 根 span 创建算法或全局采样策略。

## Decisions

| 决策 | 选项 | 理由 |
|------|------|------|
| 前端操作 ID 字段 | **`operationId`** / Header **`X-Client-Operation-Id`** | 与设计文档 §4 一致；与 W3C `traceId` 语义分离 |
| operationId 生成时机 | **click（`data-track`）或显式 `beginOperation()`**；axios 无活跃 operation 时不自动生成 | 避免轮询/后台 API 噪声 |
| operationId 存储 | **内存**（`operationContext`）；不写入 sessionStorage | 一次操作生命周期短 |
| traceId 策略（Phase 1） | **策略 A：后端生成 trace**，axios 不传 traceparent | 实现简单；与现有 `R.traceId` 纯服务端一致；策略 B 留作可选任务 |
| 后端接入点 | **`ClientOperationFilter`**（或等价 OncePerRequestFilter）读 Header → MDC `clientOperationId`；oper_log 切面/监听器从 MDC 写入 `client_operation_id` | 与 `TraceIds` 解耦；Filter 顺序在 tracing 之后、业务之前 |
| operationId 校验 | trim，最大 64 字符，非法字符丢弃，不落库 | 防注入与超长 |
| `sys_client_track.trace_id` | **保留列**，Phase 1 可存批次内首个 API 的 `serverTraceId` 或留空；**废弃 front_trace_id 语义** | 避免破坏性 DROP；新联查主键为 `operation_id` |
| Client HMAC | **`X-Client-Operation-Id` 不参与** body 签名 | 与设计 §4 一致 |
| CORS | Allow-Headers 增加 `X-Client-Operation-Id`（及预留 `traceparent`、`x-trace-id`） | 预检通过 |
| 模块放置 | Filter/MDC 常量 → **`quickboot-common`**；clientTrack/operlog 实体与 API → **`quickboot-system`** / **`quickboot-web`** | 对齐现有 monitor 包结构 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 未覆盖的点击无 operationId | 关键按钮 `data-track` + 文档约定；联查降级为时间+用户 |
| 并行 click 边界模糊 | 每次 click 新 operationId；`beginOperation` 覆盖上一活跃 operation |
| oper_log 行数随多接口增加 | 预期行为；按 `client_operation_id` 聚合查询 |
| 与现有 monitor 上报字段不兼容 | Flyway 增列；后端 DTO 同时接受 `operationId`；旧 `traceId` 字段过渡 |
| Flyway 版本冲突 | 实现前取 `db/migration` 最大 `V*` 顺延 |

## Migration Plan

1. **Flyway**：`ALTER sys_oper_log ADD client_operation_id` + 索引；`ALTER sys_client_track ADD operation_id` + 索引（版本号顺延）。
2. **后端部署**：Filter + oper_log 落库 + clientTrack DTO/实体更新。
3. **前端部署**：`operationContext`、monitor、request.js 同批发布；移除 `front_trace_id` 写入。
4. **回滚**：保留迁移文件；新列可空，回滚代码后旧客户端仍可用（仅无 operationId 联查）。
5. **历史数据**：旧 `sys_client_track.trace_id` 无法与 oper_log 按 operationId 联查，不回填。

## Open Questions

- （无）策略 B（`requestTrace.js` 每请求 traceparent）列为可选任务，默认不启用。
