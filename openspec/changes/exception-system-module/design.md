## Context

当前后端缺少统一异常分层与错误码治理，导致不同模块在异常抛出、HTTP 状态选择、错误消息国际化与日志记录上行为不一致。仓库已具备统一响应 `R`、国际化工具 `I18nUtil`、`MessageSource` 与 MDC `traceId` 机制，因此本次设计以“复用既有基础设施、补齐异常治理层”为约束。

约束条件：
- 按现有分层，`quickboot-common` 提供通用能力，`quickboot-web` 承载 Web 入口处理。
- 与现有契约兼容：失败响应仍为 `R.error(code, message)`。
- 客户端安全约束：不得暴露堆栈与敏感内部信息。

## Goals / Non-Goals

**Goals:**
- 建立统一异常层级：`BaseException`、`WarningException`、`ErrorException`。
- 建立错误码分段规范与首批常量集合（`1xxxx/2xxxx/3xxxx/4xxxx`）。
- 在应用层实现全局异常处理，统一 i18n 解析、HTTP 映射与 `R` 响应。
- 统一日志与排障口径，确保 `traceId` 可关联。

**Non-Goals:**
- 不在本次变更中重构所有历史业务抛异常代码。
- 不新增外部依赖，不替换现有 `R` 与 i18n 机制。
- 不调整网关或前端的错误处理策略，仅保证后端响应契约稳定。

## Decisions

### 决策 1：异常能力放在 `quickboot-common`，全局处理放在 `quickboot-web`
- 选择：`common` 仅放模型与规范，`web` 放 `@RestControllerAdvice`。
- 原因：与仓库分层一致，避免 `common` 绑定 Web 框架细节，提升复用性。
- 备选：在 `common` 自动装配全局处理器。
- 放弃原因：会加重 `common` 边界，且在多应用场景下覆盖策略复杂。

### 决策 2：HTTP 状态采用“异常类型映射表 + 兜底”
- 选择：维护“异常类型 -> HTTP状态 + 默认业务码”映射；`ErrorException/Throwable` 兜底 `500`。
- 原因：可扩展且可治理，能够精确支持 401/403/429 等语义状态。
- 备选：只按异常基类二分（Warning=400, Error=500）。
- 放弃原因：无法满足安全拦截类细分状态语义。

### 决策 3：消息解析优先 i18n，按三段回退
- 选择：`I18nUtil.getMessage(code,args,msg)` -> `msg` -> 固定兜底文案。
- 原因：兼顾国际化一致性与线上健壮性，避免空消息。
- 备选：未命中直接返回错误码。
- 放弃原因：用户可读性差，不利于统一体验。

### 决策 4：严重异常统一 500，响应体保留业务码
- 选择：严重异常 `HTTP 500 + R.error(4xxxx, message)`。
- 原因：符合 HTTP 语义并兼容现有业务码治理。
- 备选：HTTP 始终 200 仅靠业务码。
- 放弃原因：不利于网关告警、APM 统计与运维可观测。

## Risks / Trade-offs

- [风险] 历史模块抛出非规范异常，短期内行为不一致。
  - Mitigation：提供 `Throwable` 兜底映射，后续按模块渐进替换为 `BaseException` 派生类。

- [风险] 错误码重复或段位污染。
  - Mitigation：实现前扫描现有码位，预留区段并在常量层集中管理。

- [风险] 多语言文案缺失导致回退频繁。
  - Mitigation：补充 `messages*.properties` 并增加 key 完整性检查。

- [权衡] 使用异常类型映射表提升清晰度，但增加一处维护点。
  - Mitigation：以集中配置/常量化方式管理并添加测试覆盖。

## Migration Plan

1. 在 `quickboot-common` 新增异常模型与错误码常量，不改现有对外接口。
2. 在 `quickboot-web` 接入全局异常处理器，启用映射表与 i18n 回退逻辑。
3. 补充 i18n 文案 key（按 `String.valueOf(code)`）。
4. 增加异常处理测试（业务异常、严重异常、未捕获异常、安全映射）。
5. 灰度验证日志与告警，确认 `traceId` 关联正常后全量启用。

回滚策略：
- 若出现兼容性问题，可临时回滚 `@RestControllerAdvice` 注册并保留 `common` 新增类型不启用；不会影响数据库或协议结构性迁移。

## Open Questions

- 现有系统内错误码是否存在冲突段位，需要在实施前做一次全量扫描确认。
- 安全拦截异常类型清单是否已有统一定义，若无需在实现阶段补一层适配映射。
