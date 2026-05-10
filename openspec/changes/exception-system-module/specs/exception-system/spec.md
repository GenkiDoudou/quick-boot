## ADDED Requirements

### Requirement: 统一异常模型与字段契约
系统 MUST 提供统一异常基类 `BaseException`，并提供 `WarningException`、`ErrorException` 两类派生异常。`BaseException` SHALL 包含 `code`、`msg`、`args` 字段，并支持携带 `cause` 的构造方式。

#### Scenario: 抛出可预期异常
- **WHEN** 业务代码抛出 `WarningException` 且携带业务错误码与默认文案
- **THEN** 异常对象可被统一处理器读取到 `code/msg/args` 并进入标准响应流程

#### Scenario: 抛出严重异常
- **WHEN** 系统代码抛出 `ErrorException` 且携带 `cause`
- **THEN** 异常对象保留原始异常链路，供服务端日志完整记录

### Requirement: 统一错误码分段规范
系统 MUST 使用分段错误码规范：`1xxxx`（通用）、`2xxxx`（业务）、`3xxxx`（安全）、`4xxxx`（系统）。系统 SHALL 提供首批常量以覆盖内部错误、参数校验、认证失败、权限拒绝、幂等冲突、限流与来源拦截等场景。

#### Scenario: 业务异常使用业务段错误码
- **WHEN** 业务校验失败并抛出异常
- **THEN** 该异常使用 `2xxxx` 段错误码，不得混用 `4xxxx` 系统段

#### Scenario: 未分类系统故障使用系统段错误码
- **WHEN** 出现未预期运行时故障
- **THEN** 全局处理器返回 `4xxxx` 系统段内部错误码作为兜底

### Requirement: 全局异常处理输出统一响应契约
系统 MUST 在应用层提供全局异常处理能力，并将异常统一映射为 `R.error(code, message)`。对严重异常与未捕获异常，系统 SHALL 返回 `HTTP 500`；对安全拦截类异常，系统 SHALL 可映射至 401/403/429 等状态。

#### Scenario: WarningException 响应映射
- **WHEN** 捕获到 `WarningException`
- **THEN** 返回 `R.error(code, message)`，HTTP 状态按异常类型映射表确定

#### Scenario: 未捕获异常兜底
- **WHEN** 捕获到非 `BaseException` 的未处理异常
- **THEN** 返回 `HTTP 500` 与统一内部错误码，且响应体不包含堆栈细节

### Requirement: 错误消息国际化与回退策略
系统 MUST 按 `String.valueOf(code)` 作为 i18n key 解析错误消息，调用 `I18nUtil` 时传入 `args` 与默认 `msg`。当 i18n 未命中时，系统 SHALL 回退到 `msg`；若 `msg` 为空，系统 SHALL 回退到统一兜底文案。

#### Scenario: i18n 命中
- **WHEN** 存在对应错误码的国际化文案
- **THEN** 响应 message 使用国际化文案，并完成占位参数替换

#### Scenario: i18n 与默认文案均缺失
- **WHEN** 未找到 i18n key 且异常 `msg` 为空
- **THEN** 响应 message 使用统一兜底文案，避免返回空消息

### Requirement: 安全日志与可观测性约束
系统 MUST 记录异常日志并包含 `traceId`、错误码与异常类型。`WarningException` SHALL 使用 `warn` 级别日志；`ErrorException` 与未捕获异常 SHALL 使用 `error` 级别并记录完整堆栈。系统 MUST NOT 向客户端暴露堆栈、SQL、密钥或内部路径等敏感信息。

#### Scenario: 客户端与日志链路关联
- **WHEN** 客户端收到异常响应
- **THEN** 响应中包含可用于日志检索的 `traceId`

#### Scenario: 保护敏感信息
- **WHEN** 出现数据库或内部依赖异常
- **THEN** 客户端仅收到脱敏后的统一错误消息，敏感细节仅保留在服务端日志
