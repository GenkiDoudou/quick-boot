## ADDED Requirements

### Requirement: ServletUtils 写出统一错误 JSON

系统 SHALL 在 `quickboot-common` 提供 `ServletUtils.writeResponse(HttpServletResponse response, Integer code, Object... args)`。调用时，系统 MUST 将 HTTP 状态码设为 **200**；系统 MUST 设置 `Content-Type` 为 **`application/json;charset=UTF-8`**，并 SHALL 使用 UTF-8 写入响应体。

#### Scenario: HTTP 与编码

- **WHEN** Filter 调用 `writeResponse` 且响应尚未提交
- **THEN** 响应状态码为 200，`Content-Type` 含 `application/json` 与 `charset=UTF-8`，且响应体为合法 JSON

### Requirement: 业务码与词条键一致（选项 A）

系统 SHALL 使用入参 `Integer code` 作为 `R.error` 的 **`code` 字段取值**；系统 SHALL 使用 **`String.valueOf(code)`** 作为 `I18nUtil.getMessage` 的**词条键**。`Object... args` SHALL 以 `Object[]` 形式传入 `I18nUtil.getMessage(String, Object[])`（无参时可传 `null` 或空数组，由实现选定并与单测一致）。

#### Scenario: message 来自 i18n

- **WHEN** `MessageSource` 存在键 `"40301"` 对应文案，且调用 `writeResponse(response, 40301)`
- **THEN** JSON 体中 `code` 为 40301，`msg` 为该键在当前 `LocaleContextHolder` Locale 下解析到的字符串

### Requirement: 响应体为 R.error 的 JSON

系统 SHALL 序列化 `R.error(code, message)` 的输出作为响应体，其中 `message` 为上一要求解析得到的字符串。系统 SHALL 依赖 Jackson `ObjectMapper` 完成写出；`ObjectMapper` SHOULD 优先取自 Spring 容器以保持与全局 Jackson 配置一致，若不可得则实现 MUST 在代码或文档中声明回退策略。

#### Scenario: JSON 形状与 R 一致

- **WHEN** 调用 `writeResponse` 成功
- **THEN** 响应 JSON 包含与 `R` 类型一致的字段（至少含 `code`、`msg`、`timestamp`，以及 `R` 定义的 `data`/`traceId` 等在序列化策略下出现的字段）

### Requirement: 边界

本能力 SHALL NOT 抛出业务性「统一异常」替代写出响应；本能力 SHALL NOT 负责将 Servlet 错误转换为 MVC 异常。实现 MAY 在响应已提交时不写出或静默返回，但 MUST 在 JavaDoc 中说明该行为。

#### Scenario: 职责单一

- **WHEN** 仅引入该工具类而未修改全局异常配置
- **THEN** 不改变未调用该方法的请求处理路径
