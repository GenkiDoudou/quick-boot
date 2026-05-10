## Why

当前项目缺少统一的异常分层与错误码治理，导致业务模块在 Controller/Service 层存在分散的异常处理逻辑，响应语义与 HTTP 状态不稳定，且国际化错误消息落地不一致。现在推进该变更可统一错误契约、降低重复处理成本，并提升问题排查效率。

## What Changes

- 在 `quickboot-common` 增加统一异常模型：`BaseException`、`WarningException`、`ErrorException`，规范 `code/msg/args/cause` 语义。
- 建立错误码分段规范（`1xxxx/2xxxx/3xxxx/4xxxx`）及首批常量集合，覆盖通用、业务、安全、系统场景。
- 在应用层（`quickboot-web`）新增统一全局异常处理，输出 `R.error(code, message)`，并按异常类型映射 HTTP 状态。
- 统一消息解析策略：优先 i18n（`I18nUtil`），未命中回退默认文案，再回退系统兜底文案。
- 明确日志与安全策略：客户端不泄露堆栈，服务端保留堆栈并关联 `traceId`。

## Capabilities

### New Capabilities
- `exception-system`: 统一异常模型、错误码规范、全局异常映射与 i18n 消息回退机制。

### Modified Capabilities
- 无。

## Impact

- 后端模块：`quickboot-common`（异常与错误码能力）、`quickboot-web`（全局异常处理与映射策略）。
- API 行为：失败响应语义更一致；严重错误统一 `HTTP 500`；安全拦截类可映射到 401/403/429。
- 依赖与契约：复用既有 `R`、`I18nUtil`、`MessageSource` 与 MDC `traceId` 机制，无新增外部依赖。
- 运维排障：日志结构与错误码体系统一后，跨端定位与告警规则配置更稳定。
