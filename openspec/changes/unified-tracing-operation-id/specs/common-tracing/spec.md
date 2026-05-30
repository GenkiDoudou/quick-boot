## ADDED Requirements

### Requirement: clientOperationId MDC 键

系统 MUST 支持独立于 `traceId` 的 MDC 键 **`clientOperationId`**（或文档约定的等价常量），用于承载来自 HTTP Header `X-Client-Operation-Id` 的前端操作 ID。该键 MUST 由 Web Filter（或等价）在请求入口写入、请求结束时清除，且 MUST NOT 被 Micrometer Tracing 用作 W3C trace 标识。

#### Scenario: clientOperationId 与 traceId 并存

- **WHEN** 请求同时存在 Micrometer `traceId` 与合法 `X-Client-Operation-Id`
- **THEN** MDC MUST 同时含 `traceId` 与 `clientOperationId`，且二者值 MUST 不同（除非偶然碰撞，不作为规范要求）

#### Scenario: 请求结束清除 clientOperationId

- **WHEN** HTTP 请求处理完成
- **THEN** 线程 MDC MUST NOT 残留上一请求的 `clientOperationId`

---

### Requirement: operationId 不得替代 trace

系统 MUST NOT 将 `clientOperationId` / `operationId` 写入 `TraceIds.current()` 返回值、W3C `traceparent` 的 trace 段、或作为 `sys_oper_log.trace_id` 的替代来源。`trace_id` MUST 继续表示**单次 HTTP 请求**的后端 trace，与 `common-tracing` 既有 `TraceIds` 语义一致。

#### Scenario: oper_log trace_id 仍为请求 trace

- **WHEN** 请求携带 `X-Client-Operation-Id` 且 Micrometer 为当次请求分配 traceId
- **THEN** 入库 `sys_oper_log.trace_id` MUST 等于 `TraceIds.current()`，MUST NOT 等于 operationId
