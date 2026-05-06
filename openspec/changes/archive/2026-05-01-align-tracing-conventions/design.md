## Context

原始需求见 `原始需求/后端/链路追踪模块.md`。工程为 Spring Boot 3，`quickboot-web` 已配置 `management.tracing`、`management.otlp.tracing` 与 `logback-spring.xml` 中的 `%X{traceId}` / `%X{spanId}`；`quickboot-common` 已提供 `TraceIds`（MDC 键 `traceId`）及 `R` 构建时写入 `traceId`。此前讨论明确：**只保留一套读取 API（`TraceIds`）**；span 由 **Micrometer/Spring 在 Web 请求观测路径上创建**，若请求携带 **W3C `traceparent` 等标准传播头**，则延续已有 trace，而非在 common 中手工起 span。

## Goals / Non-Goals

**Goals:**

- 文档与规范与代码一致：公开读取入口统一为 `TraceIds`（或与其等价的单一封装），原始需求不再要求单独的 `TraceUtil` 命名。
- 在能力规范中写清：`R.traceId` 与日志中的 trace 输出均依赖 **同一 MDC 键** 及 Micrometer Tracing 对 MDC 的桥接。
- 说明运维侧采样与 OTLP endpoint 的配置键（与 Spring Boot 文档一致），作为「可选导出」验收指引。

**Non-Goals:**

- 不在 `quickboot-common` 内实现 `Tracer` 注入、手工 `Span` 创建或自定义 Propagator（除非未来单独变更）。
- 不解决全项目异步线程池 MDC 传播（可作为后续变更；本设计仅点明风险）。
- 不强制修改 Jackson 对空 `traceId` 的序列化策略（保持现状，与既有 `common-response-paging` 设计一致）。

## Decisions

1. **单一 API 名称**  
   - **做法**：以 `io.github.genkidoudou.common.api.TraceIds` 为规范中的唯一读取入口；原始需求文档改为引用该类与方法 `current()`，不引入并行的 `TraceUtil.getTraceId()`。  
   - **备选**：增加 `TraceUtil` 委托到 `TraceIds` — 增加噪声与导入分叉，不采纳。

2. **trace 生命周期**  
   - **做法**：依赖 Spring Boot + Micrometer Tracing 自动配置；无上游上下文时创建服务端根 span，有 `traceparent` 时延续 trace。common 层 **SHALL NOT** 创建 span。  
   - **备选**：在 Filter 中手写 traceId — 与 OTel/Micrometer 双轨，易不一致，不采纳。

3. **MDC 与 logback**  
   - **做法**：继续约定 MDC 键 `traceId`（及日志中的 `spanId`）与 `TraceIds.MDC_KEY`、logback pattern 对齐；验收以「同请求线程内日志行与 `R` JSON 中 traceId 一致」为主。  
   - **备选**：改名 MDC 键 — 破坏现有日志与配置，不采纳。

4. **规范落地位置**  
   - **做法**：本变更在 `openspec/changes/align-tracing-conventions/specs/common-tracing/spec.md` 新增 delta；实现以更新原始需求 + 可选 JavaDoc 为主。归档后可再由 `openspec-sync` 合入 `openspec/specs/`（若项目流程要求）。  
   - **备选**：仅改原始需求不写 spec — 与 OpenSpec 工作流失配，不采纳。

## Risks / Trade-offs

- **[Risk]** 采样率低于 1 时，部分请求可能无 tracing 上下文，导致 MDC 中无 `traceId`，`R.traceId` 为空或被省略。  
  → **Mitigation**：在文档与 spec 中写清验收前提（典型为开发/联调 100% 采样）；生产依赖运维配置说明。

- **[Risk]** 仅传自定义头不传 W3C 传播头时，Micrometer 无法自动接续前端 trace。  
  → **Mitigation**：在 design/spec 中注明「接续 trace 需标准传播头」；自定义头属扩展变更。

- **[Trade-off]** 异步与线程池场景下 MDC 可能断裂；本变更不扩展代码修复，仅在风险中记录。

## Migration Plan

1. 合并 proposal/design/spec/tasks 对应实现（文档 + 可选注释）。  
2. 评审 `原始需求/后端/链路追踪模块.md` 与 spec 一致性。  
3. 回滚：还原文档与注释即可，无行为变更预期。

## Open Questions

- 归档后是否立即执行 `openspec sync` 将 `common-tracing` 合入主 `openspec/specs/`，由发布节奏决定（本变更 tasks 以 delta 文件为准）。
