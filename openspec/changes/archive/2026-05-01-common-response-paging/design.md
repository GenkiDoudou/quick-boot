## Context

原始需求见 `原始需求/后端/通用响应与分页模块.md`。工程为 Spring Boot 3，`quickboot-common` 定位为通用 DTO、常量与横切能力的载体；日志已使用 `%X{traceId}`（见 `quickboot-web` 下 `logback-spring.xml`），与 Micrometer OpenTelemetry tracing 共存。约定：网关与前端统一以 **HTTP 200 + JSON body** 判定业务成败，`body.code` 承载语义；分页 DTO **仅作为 Controller API 契约**，持久层使用 MyBatis-Plus `Page` 时在 Service 内完成转换。

## Goals / Non-Goals

**Goals:**

- 提供 `R<T>`：字段 `code`、`msg`、`data`、`traceId`、`timestamp`；工厂方法 `ok` / `error` 系列；`isSuccess()` 当且仅当 `code == 200`，`isError()` 为其余情况。
- 提供 `PageRequest<T>`：`current`（默认 1）、`size`（默认 10、**最小值 1**）、`param`；`getOffset()` 为 `(current - 1) * size`。
- 提供 `PageInfo<T>`：`current`、`size`、`records`、`total`、`pages`、`ext`（可选 Map）；`pages` 按 `(total + size - 1) / size` 计算（在 `size >= 1` 约束下安全）。
- 提供常用 HTTP 语义向 **业务码常量**（如 200、400、401、403、404、500、503），供响应体与后续异常模块引用。
- `traceId`：从 MDC 或当前链路追踪上下文中读取；`timestamp` 为毫秒时间戳且在每次构建响应时非空。

**Non-Goals:**

- 不在本模块实现全局异常到 `R` 的映射、业务错误码枚举或 `ControllerAdvice`（由异常模块承担）。
- 不规定 MyBatis-Plus `Page` 与 `PageInfo` 的强制转换工具类是否公开（若实现阶段提供 `fromIPage` 之类辅助方法，属实现细节，以 tasks 为准）。
- 不规定 `size` 上限（防刷由网关或业务校验另定）；本设计仅固定 **默认 10、下限 1**。
- 不扩展 `isSuccess` 到 201/204 等非 200 成功码（已明确不需要）。

## Decisions

1. **HTTP 与 body.code**  
   - **做法**：业务 API 对外 **始终返回 HTTP 200**（Success 状态组中的 200），由客户端解析 `code`。  
   - **备选**：HTTP 状态与 body 对齐 — 与网关/前端既有「统一 JSON」策略冲突，不采纳。

2. **分页分层**  
   - **做法**：`PageRequest` / `PageInfo` 仅出现在 Controller（及 OpenAPI 文档）；进入 Service 后转换为 `com.baomidou.mybatisplus.extension.plugins.pagination.Page`（或从 `IPage` 回填 `PageInfo`）。  
   - **备选**：全栈统一使用 MP 类型 — 污染 API 契约与 OpenAPI，不采纳。

3. **traceId 来源**  
   - **做法**：优先与现有日志一致，从 **SLF4J MDC** 的 `traceId` 键读取；若未来存在仅 OTel Context 而无 MDC 的场景，实现可退化为 `Tracer`/`Baggage`（本阶段以保持与 logback 一致为主）。  
   - **备选**：仅依赖 Micrometer `Tracer` — 需确认与当前 MDC 填充链路一致，避免重复逻辑；实现阶段二选一或组合读取并文档化优先级。

4. **校验与非法分页**  
   - **做法**：对 `size` 施加 **最小值 1**（Bean Validation `@Min(1)` 或等价）；`current` 默认 1，实现中可对 `<1` 做规范化或校验失败策略与项目统一参数错误码对齐（由异常模块消费校验结果时仍返回 HTTP 200）。  
   - **备选**：静默修正非法值 — 易导致前后端理解不一致，不推荐除非全项目统一。

5. **常量类形态**  
   - **做法**：独立 `HttpCodes`（或同类名）(`public static final int`)，与既有 common 常量风格一致即可。  
   - **备选**：枚举 — 对纯整型业务码冗余，暂不优先。

## Risks / Trade-offs

- **[Risk]** 仅 HTTP 200 时，网关/APM 默认按 HTTP 误判「全成功」。  
  → **Mitigation**：运维与监控改为依据 JSON `code` 或业务指标；文档明确约定。
- **[Risk]** MDC 在异步线程未传播导致 `traceId` 为空。  
  → **Mitigation**：第一版验收范围为 **典型 Web 请求线程**；异步场景依赖 TaskDecorator 或与异常模块后续的上下文传播对齐。
- **[Risk]** `jackson default-property-inclusion: non_null` 下空 `traceId` 可能被省略，与「字段存在」的客户端假设不一致。  
  → **Mitigation**：实现时若需要「键恒在」，可对 `traceId` 使用 `@JsonInclude(ALWAYS)` 或显式空字符串策略，并在 spec/tasks 中单列。
- **[Trade-off]** `R` 与 Spring `ResponseEntity` 组合：若全局仍设 HTTP 200，通常直接返回 `R` 类型即可。

## Migration Plan

1. 合并后在 `quickboot-common` 增加类与常量，不写 Web Advice。  
2. 选取一个演示或现有接口改为返回 `R<T>`（可选，由 tasks 决定），前端/网关联调字段名。  
3. 回滚：移除新增类且无调用方则无行为变化；若有调用方则还原 Controller 签名。

## Open Questions

- `traceId` 在序列化 JSON 中空值时是输出 `null` 还是省略字段：待实现时与用户代理定（影响 OpenAPI 示例）。
- 是否在 common 中提供 `PageInfo.from(IPage<T>)` 静态工厂：建议提供以降低重复，非强制写入 spec 时可放入 tasks。
