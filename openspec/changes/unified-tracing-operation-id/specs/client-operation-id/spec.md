## ADDED Requirements

### Requirement: 前端 operationContext 模块

`quick-ui` MUST 提供 `operationContext` 模块（路径以实现为准，如 `src/monitor/operationContext.js`），管理当前活跃的用户操作 ID，与后端 trace 解耦。模块 MUST 提供：`beginOperation(reason?)` 生成 UUID 并设为当前活跃 operation；`getOperationId()` 返回当前 ID 或 `null`；`endOperation()` 清除活跃 operation；`runInOperation(fn)` 包装 begin/执行/end。

#### Scenario: begin 后 get 返回同一 ID

- **WHEN** 调用 `beginOperation()` 后立即调用 `getOperationId()`
- **THEN** 两次调用返回相同的非空 UUID 字符串

#### Scenario: end 后 get 返回 null

- **WHEN** 调用 `beginOperation()` 后调用 `endOperation()`，再调用 `getOperationId()`
- **THEN** `getOperationId()` 返回 `null`

---

### Requirement: operationId 触发策略

系统 MUST 在以下时机调用 `beginOperation`：（1）monitor 捕获带 `data-track` 的 click 事件时；（2）业务代码显式调用 `beginOperation`。当 axios 发出请求时若无活跃 operationId，系统 MUST NOT 自动生成 operationId。

#### Scenario: data-track 点击产生 operationId

- **WHEN** 用户点击带 `data-track` 属性的元素
- **THEN** 该次 click 监控事件 MUST 携带非空 `operationId`

#### Scenario: 无 begin 的 axios 不带 Header

- **WHEN** 未调用 `beginOperation` 且 axios 发起业务 API 请求
- **THEN** 请求 MUST NOT 携带 `X-Client-Operation-Id` Header

---

### Requirement: axios 注入 X-Client-Operation-Id

`quick-ui` 的 axios 请求拦截器 MUST 在存在活跃 operationId 时，向每个业务 API 请求注入 Header `X-Client-Operation-Id`，值为 `operationContext.getOperationId()`。同一活跃 operation 触发的多个并行或串行 API MUST 携带相同的 Header 值。

#### Scenario: 同 operation 多 API Header 相同

- **WHEN** `beginOperation()` 后连续发起两次 axios 业务请求
- **THEN** 两次请求的 `X-Client-Operation-Id` Header 值 MUST 相同

#### Scenario: 不同 operation Header 不同

- **WHEN** 两次独立的 `beginOperation()` 各触发一次 axios 请求
- **THEN** 两次请求的 `X-Client-Operation-Id` MUST 互不相同

---

### Requirement: 废弃 front_trace_id

系统 MUST 停止写入 `sessionStorage.front_trace_id`（或等价会话级「前端 traceId」）。monitor 与上报 payload MUST 使用 `operationId` 作为前端操作关联键，MUST NOT 将会话级 ID 表述为 traceId 或与后端 trace 等同。

#### Scenario: 新会话无 front_trace_id

- **WHEN** 用户清空 sessionStorage 后打开应用并触发监控
- **THEN** `sessionStorage` MUST NOT 存在新写入的 `front_trace_id` 键（或等价键）

---

### Requirement: 后端读取 X-Client-Operation-Id

后端 MUST 通过 Filter 或等价机制读取请求 Header `X-Client-Operation-Id`：trim 后长度 MUST 不超过 64；非法或空白 MUST 丢弃。合法值 MUST 写入 MDC 键 `clientOperationId`（常量名以实现为准），且 MUST NOT 写入 `TraceIds.current()` 或替代 Micrometer trace 上下文。

#### Scenario: 合法 Header 进 MDC

- **WHEN** 请求携带 `X-Client-Operation-Id: op-abc123`
- **THEN** 请求处理线程 MDC MUST 含 `clientOperationId=op-abc123`

#### Scenario: 超长 Header 丢弃

- **WHEN** 请求携带超过 64 字符的 `X-Client-Operation-Id`
- **THEN** MDC MUST NOT 设置 `clientOperationId`；oper_log.client_operation_id MUST 为空

---

### Requirement: CORS 与 HMAC 边界

CORS 配置的 Allow-Headers MUST 包含 `X-Client-Operation-Id`。Client HMAC 签名计算 MUST NOT 将 `X-Client-Operation-Id` 纳入 signed body 或等价签名材料。

#### Scenario: 预检允许 operation Header

- **WHEN** 浏览器对携带 `X-Client-Operation-Id` 的跨域请求发起 OPTIONS 预检
- **THEN** 响应 Access-Control-Allow-Headers MUST 包含 `X-Client-Operation-Id`
