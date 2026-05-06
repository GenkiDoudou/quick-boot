## ADDED Requirements

### Requirement: 通过 TraceIds 读取当前 traceId

系统 SHALL 在 `quickboot-common` 提供 `TraceIds`（`final` 工具类），并 SHALL 提供 `String current()`：从 SLF4J `MDC` 读取键 `traceId`（与 `TraceIds.MDC_KEY` 常量一致）；当值为 `null`、空白或仅空白字符时，方法 MUST 返回 `null`，否则 MUST 返回去除首尾空白后的字符串。系统 SHALL NOT 在本能力范围内引入另一套与 `TraceIds` 并行的公开类名（例如 `TraceUtil`）作为规范要求的读取入口。

#### Scenario: MDC 已设置非空 traceId

- **WHEN** 当前线程 `MDC.put("traceId", "abc123")`
- **THEN** `TraceIds.current()` 返回 `"abc123"`

#### Scenario: MDC 未设置或为空

- **WHEN** 当前线程未设置 `traceId` 或值为 `""` / `"   "`
- **THEN** `TraceIds.current()` 返回 `null`

### Requirement: R 与 TraceIds 协作

系统 SHALL 在构造 `R` 实例（`build` 或等价私有工厂路径）时，将 `traceId` 字段赋值为 `TraceIds.current()` 在该时刻的返回值。系统 SHALL NOT 在 `quickboot-common` 的该路径上创建 OpenTelemetry/Micrometer span 或向 MDC 写入 traceId 以替代框架行为。

#### Scenario: 与日志同一 traceId

- **WHEN** 某 Web 请求处理线程上 MDC 已含与 Micrometer Tracing 一致的 `traceId`，且 Controller 返回 `R.ok()`（或任意经 `build` 的 `R`）
- **THEN** 响应体中 `traceId` 字段（若序列化策略保留非空值）与该请求同线程日志 pattern 中输出的 `traceId` 一致

### Requirement: 日志与 MDC 键约定

应用 SHALL 在使用的 Logback 配置中，将 `%X{traceId}`（及可选 `%X{spanId}`）纳入日志 pattern，且 Micrometer Tracing 对 MDC 的填充键名 MUST 与 `TraceIds.MDC_KEY` 一致，或文档中明确映射关系（本仓库以 `traceId` / `spanId` 为准）。

#### Scenario: 日志行可关联 trace

- **WHEN** 处于已启用 Micrometer Tracing 与 MDC 桥接的运行环境，且某请求产生 INFO 日志一行
- **THEN** 该日志行包含依据 `%X{traceId}` 输出的 trace 标识（若该请求存在 tracing 上下文）

### Requirement: 框架负责 span 与上下文传播

系统 SHALL 依赖 Spring Boot 3 与 Micrometer Tracing（及选用的 OpenTelemetry bridge）在 HTTP 服务端自动创建或延续 trace：无上游传播上下文时创建根 span；当请求携带标准 W3C 传播头（如 `traceparent`）时，MUST 延续对应 trace。本能力 SHALL NOT 要求业务或 `quickboot-common` 在 Filter/Interceptor 中手工创建 span 以满足上述行为。

#### Scenario: 标准头延续 trace

- **WHEN** 客户端请求携带合法的 W3C `traceparent` 头，且服务端已启用 Micrometer Tracing
- **THEN** 服务端为该请求记录的 span 属于该头所标识的同一 trace（与 OTel/Micrometer 默认行为一致，不在本 spec 中重写算法）

### Requirement: 运维可配置采样与 OTLP 导出

部署 SHALL 可通过 Spring Boot 配置项 `management.tracing.sampling.probability` 调整采样率；SHALL 可通过 `management.otlp.tracing.endpoint`（或当前 Spring Boot 版本文档推荐的等价键）配置 OTLP 导出端点，以便将链路数据发送至外部可观测后端（可选启用）。

#### Scenario: 配置项存在且可被覆盖

- **WHEN** 运维在 `application.yml` 中设置 `management.tracing.sampling.probability` 与 `management.otlp.tracing.endpoint`
- **THEN** 应用启动后 tracing 采样与 OTLP 终点行为遵循 Spring Boot/Micrometer 对该配置的解释（不在本 spec 中重复实现细节）
