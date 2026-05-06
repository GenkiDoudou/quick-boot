## Why

《链路追踪模块》原始需求与仓库实现已基本一致，但文档仍出现 `TraceUtil` 等表述，与现有 `TraceIds` 约定不一致；同时缺少成文的 **span 由框架创建 / 上游通过 W3C 传播延续 trace** 的边界说明，不利于评审与后续接入。需要在 OpenSpec 中固化能力要求与文档对齐，避免双 API 与职责误解。

## What Changes

- 新增能力规范 **`common-tracing`**：约定从 MDC 读取 `traceId` 的公开 API、与 `R` 及 logback 的协作、以及「不在 common 内创建 span」的边界。
- 将 `原始需求/后端/链路追踪模块.md` 与上述约定对齐（统一为 `TraceIds`，补充传播与采样相关说明）。
- **不改** `R` / `TraceIds` 的对外行为（若实现阶段仅有注释或文档补充，视为非破坏性）。

## Capabilities

### New Capabilities

- `common-tracing`: quickboot-common 与 Web 工程中链路标识（traceId/spanId）与 MDC、统一响应、日志 pattern、Micrometer Tracing/OTLP 运维配置之间的契约；明确框架负责 span 生命周期与上下文传播，common 仅提供读取与文档化约定。

### Modified Capabilities

- （无）不在本变更中修改 `common-response-paging` 等已有 spec 的正文要求；`R.traceId` 行为保持与设计文档既有「从 MDC 读取」一致。

## Impact

- 文档：`openspec/specs` 或变更内 delta（以 tasks 为准）、`原始需求/后端/链路追踪模块.md`。
- 代码：`quickboot-common` 中 `TraceIds`、`R` 可能仅补充 JavaDoc 交叉引用（若 tasks 要求）。
- 配置：沿用既有 `management.tracing`、`management.otlp.tracing`、`logback-spring.xml`；本变更以说明为主，不强制改配置默认值。
