# 前后端统一链路追踪设计文档

> **版本说明（2026-05-30 修订）：** 明确 **traceId/span 归属后端（一 HTTP 请求一 trace）**；**前端一次用户操作** 使用独立 **`operationId`** 聚合可能触发的多个接口；二者通过请求头与落库字段关联，**禁止**用 Tab 会话级 traceId 横跨多个 HTTP 请求。

## 1. 背景与目标

`quick-ui` 已实现轻量前端行为监控（`src/monitor/` → `sys_client_track`），`quickboot` 已通过 **Micrometer Tracing + OpenTelemetry** 在 MDC 中写入 `traceId`/`spanId`，并同步到 `R.traceId`、`sys_oper_log.trace_id` 与日志 pattern。

**现状问题：**

1. 前端 `front_trace_id`（会话级自生成 ID）与后端 **无关联**。
2. 未区分 **「一次用户操作」** 与 **「一次 HTTP 请求」**，易误以为应共用一个 traceId。
3. 一次点击触发多个接口时，无法按 **同一业务操作** 聚合多条 oper_log。

**目标：**

1. **后端语义不变且写清：** 一次 HTTP 请求 = 一个 **traceId**；库/MQ/内部调用 = 该 trace 下的 **span**。
2. **前端新增 `operationId`：** 一次用户操作（点击/提交）= 一个 operationId；触发的 **N 个接口** 共享 operationId，各自拥有 **不同 traceId**。
3. **跨系统联查：** 用 **operationId** 串前端 monitor ↔ 多条 oper_log；用 **traceId** 钻取单次请求内的日志/OTLP（含 DB/MQ span）。
4. 可选：单次请求通过 **`traceparent`** 续链（**每请求独立 trace**，不跨请求共享 traceId）。

**已确认产品取向：**

| 项 | 决策 |
|----|------|
| 首要目标 | 排障：还原「用户做了什么 → 调了哪些接口 → 某次接口内 DB/MQ 慢/错」 |
| ID 分层 | **operationId（前端操作）** + **traceId/span（后端请求内）** |
| 多接口场景 | **同一 operationId，多个 traceId**（非一个 traceId 包打天下） |
| 存储 | MySQL：client_track + oper_log（含 operationId）；OTLP 存完整 Span 树（dev） |
| MQ/DB | span 挂在 **单次请求的 trace** 下；MQ 传播 traceparent（Phase 2） |
| 管理端 | Phase 1：按 operationId / traceId 分别查询；Phase 2：合并时间线 UI |

---

## 2. 三层 ID 模型（核心）

| ID | 归属 | 粒度 | 生成方 | 存储/传播 |
|----|------|------|--------|-----------|
| **operationId** | 前端业务 | **一次用户操作**（一次点击、一次表单提交） | 前端（点击/提交时） | Header `X-Client-Operation-Id`；`sys_client_track`；`sys_oper_log.client_operation_id` |
| **traceId** | 后端链路 | **一次 HTTP 请求** 及其同步下游（DB/MQ） | Micrometer（入站可续 `traceparent`） | MDC、`R.traceId`、`sys_oper_log.trace_id`、日志、OTLP |
| **spanId** | 后端链路 | trace 内一个节点（Controller、JDBC、MQ…） | Micrometer | MDC、OTLP；**不入** oper_log 主表 |

**关系示例：** 用户点「保存」触发 3 个 API

```text
operationId = op-7f3a...          ← 一次前端操作，固定

  POST /validate   →  traceId = t-111  →  spans: HTTP, SQL...
  POST /user       →  traceId = t-222  →  spans: HTTP, SQL, MQ...
  GET  /list       →  traceId = t-333  →  spans: HTTP, SQL...

oper_log: 3 行，client_operation_id 均为 op-7f3a，trace_id 分别为 t-111/t-222/t-333
client_track: 1 click + 3 api 事件，operationId 相同，api 事件各带 serverTraceId
```

**禁止的设计：**

- ❌ Tab 会话共用一个 traceId 覆盖所有 HTTP 请求
- ❌ 用 traceId 表示「前端一次点击」
- ❌ 期望「一次操作只有一条 oper_log」（一次操作可对应 **多行** oper_log）

---

## 3. 现状摘要

### 3.1 前端（`quick-ui`）

| 组件 | 行为 |
|------|------|
| `createUserMonitor.js` | `sessionStorage.front_trace_id`，与后端无关 |
| `bindRequestMonitor.js` | `api_slow` / `api_error`，未读 `R.traceId` |
| `sys_client_track.trace_id` | 存前端随机会话 ID，语义将废弃 |

### 3.2 后端（`quickboot`）

| 组件 | 行为 |
|------|------|
| Micrometer OTEL | 入站 HTTP 创建 trace；JDBC/MQ 可为 child span |
| `sys_oper_log.trace_id` | 每次 Controller 请求一条，trace_id = 当次请求 |
| MQ | 尚无 |

---

## 4. 已确认决策摘要

| 项 | 决策 |
|----|------|
| 前端操作 ID | 字段名 **`operationId`**；HTTP 头 **`X-Client-Operation-Id`** |
| operationId 生成 | 用户 **click**（带 `data-track` 或关键按钮）或 **显式 `beginOperation()`** 时生成 UUID；同一次操作触发的 axios **共用** |
| traceId | **每 HTTP 请求一个**；由后端生成，或前端 **每请求** 传新 `traceparent`（二选一，见 §6.2） |
| traceparent | **仅作用于当前请求**；不跨请求复用 traceId |
| oper_log | 保留 `trace_id`；**新增** `client_operation_id` |
| client_track | **新增** `operation_id`（或复用列改名）；`trace_id` 列 **废弃或改存主 API 的 serverTraceId（可选）** |
| 联查主键 | monitor ↔ oper_log：**operationId**；oper_log ↔ 日志/OTLP：**traceId** |
| Client HMAC | 新 header **不参与** body 签名 |
| CORS | Allow-Headers 含 `X-Client-Operation-Id`、`traceparent`、`x-trace-id` |

---

## 5. 架构与数据流

```text
[用户点击「保存」]
       │
       ├─ operationId = op-7f3a  （operationContext.begin）
       │
       ├─ axios #1  headers: X-Client-Operation-Id=op-7f3a
       │              [可选] traceparent(t-111, span-s1)
       │            ──► 后端 trace t-111 → oper_log #1
       │            ◄──  R.traceId = t-111
       │            monitor: api 事件 serverTraceId=t-111
       │
       ├─ axios #2  同 operationId，新 trace t-222 ...
       └─ axios #3  同 operationId，新 trace t-333 ...

[单次请求 t-222 内部]
       SERVER span
         ├─ JDBC span
         └─ MQ span (Phase 2)
```

---

## 6. 前端设计

### 6.1 模块 `operationContext.js`（新增，核心）

**职责：** 管理「当前用户操作」的 operationId，与后端 trace 解耦。

| 方法 | 说明 |
|------|------|
| `beginOperation(reason?)` | 生成 `operationId`（UUID），设为 **当前活跃 operation** |
| `getOperationId()` | 返回当前活跃 operationId，无则 `null` |
| `endOperation()` | 清除活跃 operation（可选：操作完成后） |
| `runInOperation(fn)` | 包装：`begin` → 执行 → `end` |

**触发 `beginOperation` 的策略（Phase 1）：**

1. **显式：** 关键按钮 `@click` 前调用，或 `data-track` 点击时 monitor 内自动 `begin`。
2. **隐式兜底：** 若 axios 发出时无活跃 operationId，**不自动生成**（避免噪声）；仅 API 事件无 operationId，联查降级为时间+用户。

**持久化：** 默认 **内存** 即可；不要求 sessionStorage（一次操作生命周期短）。若 SPA 长流程（向导）可手动保持 operationId 至步骤结束。

### 6.2 模块 `requestTrace.js`（可选，与 operation 分离）

**职责：** **单次 HTTP 请求** 的 W3C 头（若采用「前端每请求生成 traceparent」策略）。

| 方法 | 说明 |
|------|------|
| `nextRequestTraceHeaders()` | 每调用一次：**新** traceId + 新 spanId → `traceparent`、`x-trace-id` |

**策略二选一（实现计划定稿）：**

| 策略 | 行为 | 适用 |
|------|------|------|
| **A. 后端生成 trace** | axios **不传** traceparent | 简单；`R.traceId` 纯服务端 |
| **B. 每请求传 traceparent** | 每 axios 调用 `nextRequestTraceHeaders()` | 与现有「header 续链」习惯一致；**每请求新 traceId** |

两种策略下 **`X-Client-Operation-Id` 必传**（有活跃 operation 时）。

### 6.3 axios 集成（`request.js`）

```javascript
const opId = operationContext.getOperationId()
if (opId) {
  config.headers['X-Client-Operation-Id'] = opId
}
// 策略 B 时：
const { traceparent, 'x-trace-id': traceId } = requestTrace.nextRequestTraceHeaders()
config.headers['traceparent'] = traceparent
config.headers['x-trace-id'] = traceId
```

### 6.4 monitor 集成

| 事件类型 | 字段 |
|----------|------|
| `click` / 关键交互 | `operationId`（click 时可 `beginOperation`） |
| `route_*` | `operationId`（若有活跃 operation） |
| `api_slow` / `api_error` | `operationId` + **`serverTraceId`**（= `response.data.traceId`，即 **当次请求** 的后端 traceId） |
| 废弃 | `front_trace_id` / 会话级「前端 traceId」 |

**DEV 校验：** 策略 B 时，单次请求 `serverTraceId` 应与当次 `x-trace-id` 一致；**不应**要求多次 API 的 serverTraceId 彼此相同。

### 6.5 监控上报（`report.js`）

- 批次 payload 增加 **`operationId`**（取批次内事件的主 operationId 或首个非空值）。
- 上报请求本身可不带 operationId，或带当前活跃 operationId；**不**要求与业务 API 共 traceId。

---

## 7. 后端设计

### 7.1 traceId / span（保持 Micrometer 语义）

- **一次 HTTP 入站** → 一个 trace（`TraceIds.current()`）。
- **JDBC / MQ** → 同一 traceId 下的 child span（Phase 1 启用 JDBC observation）。
- **`sys_oper_log`**：一行 ≈ 一次 Controller 调用；`trace_id` = 当次请求 traceId。

### 7.2 `X-Client-Operation-Id` 接入（新增）

**`ClientOperationFilter` 或 oper_log 切面增强：**

1. 读取 Header `X-Client-Operation-Id`（trim，最大长度 64，非法丢弃）。
2. 写入 MDC 键 `clientOperationId`（便于日志 grep）。
3. **`OperLogCapturePayload` / `SysOperLog`** 增加 `clientOperationId` 落库。

**不入 traceId：** operationId **不**写入 `TraceIds.current()`，**不**替代 W3C trace。

### 7.3 traceparent 入站（策略 B 时）

与现设计一致：Micrometer 解析 **当次请求** 的 `traceparent`；无 header 则服务端新建 trace。**禁止**跨请求共享同一 traceparent 的 traceId 作为「前端操作 ID」。

### 7.4 DB / MQ Span

| 节点 | 关系 |
|------|------|
| JDBC | 挂在 **当前 HTTP 请求** 的 traceId 下 |
| MQ 生产 | Header 带 **当前请求** 的 traceparent；consumer 续链（Phase 2） |
| operationId | 可选写入 MQ message header **副本**，便于消费侧 oper_log 关联；**不**替代 traceId |

---

## 8. 数据模型变更

### 8.1 `sys_oper_log`（Flyway 新增列）

| 列 | 类型 | 说明 |
|----|------|------|
| **`client_operation_id`** | VARCHAR(64) NULL | 来自 `X-Client-Operation-Id`；索引 |
| `trace_id` | 不变 | **单次请求** 的后端 traceId |

**联查：**

```sql
-- 一次前端操作触发的所有后端接口
SELECT * FROM sys_oper_log WHERE client_operation_id = ? ORDER BY oper_time;

-- 某次接口内部的完整链路（日志/OTLP）
SELECT * FROM sys_oper_log WHERE trace_id = ?;
```

### 8.2 `sys_client_track`（Flyway）

| 列 | 变更 |
|----|------|
| **`operation_id`** | VARCHAR(64) NOT NULL DEFAULT ''，索引；**主关联键** |
| `trace_id` | **废弃语义**；Phase 1 可保留列存「批次内首个 API 的 serverTraceId」便于快捷跳转，或迁移后删除 |
| `events_json` | 元素含 `operationId`、`serverTraceId`（API 事件） |

### 8.3 历史数据

- 旧 `sys_client_track.trace_id`（front_trace_id）与 oper_log **无法**按 operationId 联查；不回填。

---

## 9. 管理端与联查体验

### 9.1 Phase 1

| 页面 | 能力 |
|------|------|
| **前端监控** | 列表/详情展示 **operationId**；详情 events 中 API 行展示 **serverTraceId**（可点击复制） |
| **操作日志** | 搜索条件增加 **client_operation_id**；列表展示该列 |
| **联查路径 A** | operationId → 前端 events + 多条 oper_log |
| **联查路径 B** | 某条 api 的 serverTraceId → 单条 oper_log + 日志 grep +（Phase 2 OTLP） |

### 9.2 Phase 2

- **操作时间线页**：以 operationId 为根，展示 click → API₁(t-111) → API₂(t-222) → 各 trace 下 DB/MQ span（OTLP 或 `sys_trace_span`）。
- Jaeger 外链：`search?traceId=t-222`（**按单次请求** 钻取，非 operationId）。

---

## 10. 错误处理与边界

| 场景 | 行为 |
|------|------|
| 无 operationId 的 API | oper_log.client_operation_id 为空；仍正常记 trace_id |
| 一次 click 触发并行 3 API | 同一 operationId，3 个 traceId，3 条 oper_log |
| 自动轮询/后台 API | 不带 operationId（除非显式 begin） |
| 非法 operationId header | 后端丢弃，不落库 |
| 策略 A vs B | 均不影响 operationId 模型 |
| HMAC / CORS | 新 header 按 §4 处理 |

---

## 11. 测试与验收标准

### 11.1 手工验收（Phase 1）

1. 点击带 `data-track` 的按钮，触发 **≥2 个** API。
2. 前端 monitor 批次：**同一 operationId**；≥2 个 api 事件的 **serverTraceId 互不相同**。
3. `sys_oper_log`：**≥2 行**，**client_operation_id 相同**，**trace_id 互不相同**。
4. 每条 oper_log 的 trace_id 与对应 API 响应 `R.traceId`、日志 `traceId=` **一致**。
5. 操作日志页按 **client_operation_id** 可一次查出上述多行。
6. 按其中任一 **trace_id** 可唯一定位单条 oper_log 及日志行。

### 11.2 自动化（可选）

- 前端：`operationContext.begin` + mock 两次 axios，断言 header operationId 相同、trace header（若策略 B）traceId 不同。
- 后端：MockMvc 同 operationId 两次请求，断言两条 oper_log 相同 client_operation_id、不同 trace_id。

---

## 12. 迁移与实现顺序

1. Flyway：`sys_oper_log.client_operation_id`、`sys_client_track.operation_id`。
2. 后端：`ClientOperationFilter` + oper_log 落库。
3. 前端：`operationContext.js` → `request.js` header → monitor 字段 → client_track 上报。
4. 管理端：两表查询条件与列展示。
5. （可选）策略 B：`requestTrace.js` + traceparent。
6. Phase 2：OTLP 时间线、MQ 传播。

**废弃：** `front_trace_id`、会话级 traceId、spec 初版「Tab 共 traceId」叙述。

---

## 13. 风险与缓解

| 风险 | 缓解 |
|------|------|
| operationId 未覆盖所有点击 | 关键路径 `data-track` + 文档约定；逐步扩大 begin 触发点 |
| 并行请求 operation 边界模糊 | 每次 click 新 operationId；避免全局单例跨 click |
| oper_log 行数变多（多接口） | 预期行为；按 operationId 聚合展示 |
| 与初版 monitor 字段不兼容 | Flyway + 前端兼容读取；旧 trace_id 列保留过渡 |

---

## 14. 文档与后续

- 实现计划：`docs/superpowers/plans/2026-05-30-unified-tracing.md`（待 writing-plans 产出）。
- 排障短指南：先 **operationId** 看「做了什么、调了哪些接口」，再 **traceId** 看「某接口内 SQL/MQ/堆栈」。

---

**Spec 自检（2026-05-30 修订）：** traceId/span 归属后端单请求；operationId 归属前端单次操作；多接口 = 同 operationId + 多 traceId；与 operlog/`TraceIds` 语义一致；无 Tab 级 traceId 横跨请求。
