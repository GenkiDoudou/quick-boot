# 异常体系模块设计（quickboot-common/common/exception）

## 1. 目标与范围

### 1.1 目标
本设计用于在项目内建立统一异常体系，确保：
- 业务侧抛出异常语义一致（区分可预期与严重错误）。
- 客户端响应格式一致（`R<Void>` + 稳定业务码）。
- 服务端可观测性完整（日志、`traceId`、异常分类）。
- 错误消息具备国际化能力（基于错误码解析）。

### 1.2 范围
- 本次只覆盖异常模型、错误码规范、全局异常映射机制。
- 不覆盖具体业务模块的异常抛出改造细节。
- 不改变既有统一响应对象 `R` 与 i18n 基础设施 `I18nUtil` 的既有契约。

### 1.3 分层边界
- `quickboot-common`：仅提供异常能力（异常基类、子类、错误码常量、可选辅助工具）。
- `quickboot-web`（或应用模块）：提供 `@RestControllerAdvice`，负责 HTTP 状态与 `R` 响应组装。

## 2. 设计原则
- 语义优先：异常类型表达意图，避免散落式 `try/catch`。
- 兼容优先：复用现有 `R`、`I18nUtil`、`traceId` 机制。
- 安全优先：客户端不暴露内部堆栈与敏感细节。
- 扩展优先：错误码分段清晰，映射表可按异常类型扩展。

## 3. 核心模型设计

### 3.1 BaseException
`BaseException extends RuntimeException`

字段约定：
- `Integer code`：业务错误码。
- `String msg`：默认文案（i18n 未命中时回退）。
- `Object[] args`：i18n 占位参数。

构造器最小集合：
- `(Integer code, String msg)`
- `(Integer code, String msg, Object[] args)`
- `(Integer code, String msg, Throwable cause)`
- `(Integer code, String msg, Object[] args, Throwable cause)`

约束：
- `code` 不能为空；为空时在构造阶段降级为内部错误码。
- `msg` 允许为空，但最终响应阶段必须有兜底文案。

### 3.2 WarningException
`WarningException extends BaseException`

语义：
- 表示可预期异常（业务校验失败、权限拒绝、认证失败、限流、幂等冲突、来源拦截等）。
- 默认映射 4xx/429（由映射表决定）。

### 3.3 ErrorException
`ErrorException extends BaseException`

语义：
- 表示严重错误（系统内部故障、关键依赖失败）。
- 默认映射 `HTTP 500`，建议使用 `4xxxx` 业务码。

## 4. 错误码规范

### 4.1 分段规则
- `1xxxx`：通用错误
- `2xxxx`：业务错误
- `3xxxx`：安全相关错误
- `4xxxx`：系统错误

### 4.2 组织形式
采用“单一总类 + 内部分组常量”模式，例如：
- `ErrorCodes.Common`
- `ErrorCodes.Biz`
- `ErrorCodes.Security`
- `ErrorCodes.System`

### 4.3 最小常量集（首批）
- 通用：参数非法、请求格式错误。
- 业务：状态不允许、幂等重复。
- 安全：认证失败、权限不足、来源拦截、限流。
- 系统：内部错误、关键依赖不可用。

> 注：具体数值在实现阶段与现有代码库占用情况对齐，避免冲突。

## 5. 全局异常处理设计

### 5.1 处理职责
全局异常处理器放在 `quickboot-web`（或应用模块），职责包括：
- 异常分类识别。
- 消息国际化解析与回退。
- HTTP 状态确定。
- 统一响应 `R.error(code, message)` 输出。
- 结构化日志记录。

### 5.2 消息解析规则
对 `BaseException` 派生异常：
1. 使用 `String.valueOf(code)` 作为 i18n key。
2. 先尝试 `I18nUtil.getMessage(codeKey, args, msg)`。
3. 若未命中且 `msg` 为空，回退统一兜底文案（如“系统繁忙，请稍后再试”）。

### 5.3 HTTP 状态映射规则
采用“异常类型 -> HTTP 状态 + 默认业务码”映射表。

推荐默认映射：
- `WarningException`：默认 `400`。
- 认证失败类：`401`。
- 权限拒绝类：`403`。
- 限流/频控类：`429`。
- `ErrorException`：`500`。
- 其他未捕获 `Throwable`：`500` + 内部错误码。

说明：
- 若异常实例已携带明确业务码，则优先使用异常中的 `code`。
- 未携带业务码时按映射表默认业务码兜底。

### 5.4 响应契约
- 响应体统一为 `R<Void>`。
- 失败响应统一 `R.error(code, message)`。
- `traceId` 按现有 MDC 机制透出，便于客户端报障与服务端日志关联。

## 6. 日志与安全策略
- `WarningException` 记录 `warn` 级别日志。
- `ErrorException` 与未捕获异常记录 `error` 级别日志并保留完整堆栈。
- 客户端响应禁止返回堆栈、SQL、密钥、内部路径等敏感内容。
- 同一错误码在多语言下应表达同一语义，避免跨语言语义漂移。

## 7. 数据流（请求到响应）
1. Controller/Service 抛出 `WarningException` 或 `ErrorException`。
2. 全局异常处理器捕获并提取 `code/msg/args`。
3. 根据 `code` 调用 i18n 解析 message。
4. 根据异常类型映射 HTTP 状态。
5. 返回 `R.error(code, message)`，附带 `traceId`。
6. 服务端记录结构化日志（含异常类别、错误码、traceId、堆栈）。

## 8. 测试与验收

### 8.1 核心用例
- 抛出 `WarningException(code=2xxxx)`：返回 `R.error(2xxxx, message)`，message 可命中 i18n。
- i18n 未命中：回退 `msg`；`msg` 为空再回退统一兜底文案。
- 抛出 `ErrorException(4xxxx)`：返回 `HTTP 500 + R.error(4xxxx, message)`。
- 抛出未捕获 `RuntimeException`：返回 `HTTP 500 + 内部错误码`，不泄露堆栈。
- 安全拦截异常：正确映射 401/403/429。
- 响应含 `traceId`，可在日志中检索到同值链路。

### 8.2 回归关注
- 不影响既有 `R` 成功响应结构。
- 不改变现有 i18n 基础能力与 key 约定。
- 不引入对业务层无感知的 breaking change。

## 9. 实施清单（非代码）
- 在 `quickboot-common` 新增异常模型与错误码常量。
- 在 `quickboot-web` 新增/调整全局异常处理器与映射表。
- 补充 `i18n/messages*.properties` 对应错误码文案。
- 增加异常处理测试样例（单元/集成按现有测试基线选择）。

## 10. 风险与决策
- 风险：历史业务异常抛出不规范导致映射不一致。
- 应对：先兼容兜底，再逐步替换为 `BaseException` 派生异常。

- 风险：错误码冲突。
- 应对：实现前扫描现存错误码，建立保留区段。

- 风险：多语言文案缺失。
- 应对：在测试与发布检查中加入 i18n key 完整性校验。

## 11. 结论
本设计采用“`common` 提供异常能力、`web` 提供全局处理”的分层方案，满足统一响应、国际化、HTTP 语义与安全可观测要求，并可在不破坏现有契约前提下渐进落地。
