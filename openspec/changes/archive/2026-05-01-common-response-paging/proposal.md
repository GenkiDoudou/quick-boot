## Why

后端接口需要统一 JSON 返回结构与分页契约，便于网关、前端与日志/排障链路一致消费；当前 `quickboot-common` 尚缺标准化的 `R<T>`、分页入参/出参与 HTTP 语义码常量，业务侧易重复实现且与链路追踪字段难以对齐。

## What Changes

- 在 `quickboot-common` 提供统一响应体 `R<T>`：`code`、`msg`、`data`、`traceId`、`timestamp`，以及 `ok` / `error` 工厂方法与 `isSuccess` / `isError` 判断（成功约定为 `code == 200`）。
- 提供仅用于 Controller 层的分页 API 契约：`PageRequest<T>`（`current`、`size`、`param`、`getOffset()`）与 `PageInfo<T>`（`current`、`size`、`records`、`total`、`pages`、`ext`）；Service/Mapper 内部再转换为 MyBatis-Plus `Page`。
- 提供全局 HTTP 语义向的业务码常量（如 200/400/401/403/404/500/503 等），供响应与后续异常模块协同引用。
- **约定**：业务接口 HTTP 状态码始终为 200，成败以响应体 `code` 为准；分页 `size` 默认 10、下限 1；`traceId` 从 MDC/链路追踪获取；本变更**不包含**业务错误码枚举与全局异常到响应的映射（由异常模块承担）。

## Capabilities

### New Capabilities

- `common-response-paging`：`quickboot-common` 侧统一响应 `R<T>`、分页契约 `PageRequest`/`PageInfo`、HTTP 语义码常量及与链路 `traceId`、时间戳字段的约定与验收要求。

### Modified Capabilities

- （无）仓库 `openspec/specs/` 下暂无同名能力规范需增量修改既有需求条目。

## Impact

- **代码**：在 `quickboot-common` 新增响应与分页相关包（DTO/常量、`R` 工厂与 `traceId` 读取方式等）；Web 层 Controller 逐步采纳 `R<T>` 与分页契约后可减少重复封装。
- **依赖**：沿用现有链路追踪与日志 MDC（如 `micrometer-tracing`、`logback` 中 `%X{traceId}` 等约定），**不强制**为本能力新增运行时依赖类型；若读取 `traceId` 仅需 slf4j MDC / 已有的 trace 门面，应保持轻量。
- **API**：对外 JSON 契约新增/统一字段含义；网关与前端按「HTTP 200 + body.code」解析时需与本文档对齐。
- **边界**：不包含异常处理器、不包含业务错误码表；MyBatis-Plus 仅作为分页实现侧转换目标，分页 DTO 不侵入 Mapper 签名（由业务自主选择转换）。
