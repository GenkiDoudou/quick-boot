## 1. 异常模型与错误码能力建设

- [x] 1.1 在 `quickboot-common` 新增 `BaseException`，实现 `code/msg/args` 字段与含 `cause` 的构造器，并补齐 JavaDoc。
- [x] 1.2 新增 `WarningException`、`ErrorException` 继承 `BaseException`，明确可预期异常与严重异常语义。
- [x] 1.3 新增错误码常量总类（含 `Common/Biz/Security/System` 分组），落地 `1xxxx/2xxxx/3xxxx/4xxxx` 首批码位。
- [x] 1.4 校验并整理现有错误码占用，避免与新增常量冲突，形成保留区段约定。

## 2. 应用层全局异常处理实现

- [x] 2.1 在 `quickboot-web` 新增或改造 `@RestControllerAdvice`，统一处理 `BaseException`、`WarningException`、`ErrorException` 与兜底 `Throwable`。
- [x] 2.2 实现消息解析链路：`I18nUtil.getMessage(code,args,msg)` -> `msg` -> 统一兜底文案。
- [x] 2.3 实现异常类型到 HTTP 状态映射表，覆盖默认 400、401、403、429 与 500 兜底规则。
- [x] 2.4 统一响应输出为 `R.error(code, message)`，并确保失败响应携带 `traceId`。

## 3. 国际化与安全日志完善

- [x] 3.1 在 `i18n/messages*.properties` 补充首批错误码文案，key 使用 `String.valueOf(code)`。
- [x] 3.2 统一日志策略：`WarningException` 记 `warn`，严重异常记 `error`，服务端保留完整堆栈。
- [x] 3.3 增加响应脱敏保护，确保客户端不暴露堆栈、SQL、密钥与内部路径。

## 4. 测试与回归验证

- [x] 4.1 新增/更新异常处理测试，覆盖 `WarningException`、`ErrorException`、未捕获异常三类主路径。
- [x] 4.2 新增安全映射测试，验证认证失败/权限不足/限流分别映射到 401/403/429。
- [x] 4.3 新增 i18n 回退测试，验证命中、未命中与空 `msg` 兜底文案行为。
- [ ] 4.4 执行模块级构建与测试命令，确认变更不破坏既有 `R` 契约与响应结构。

