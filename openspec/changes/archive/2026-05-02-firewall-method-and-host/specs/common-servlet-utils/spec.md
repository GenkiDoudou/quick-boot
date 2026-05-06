## ADDED Requirements

### Requirement: i18n 未命中时支持兜底文案

为支持 Filter/防火墙在 i18n 词条缺失时仍能返回可读文案，系统 SHALL 在 `quickboot-common` 的 `ServletUtils` 提供带“兜底文案（fallback message）”能力的写出方式：

- 系统 MUST 允许调用方在写出错误 JSON 时提供 `fallbackMessage`。
- 系统 MUST 仍以 `String.valueOf(code)` 作为 i18n 词条键尝试解析文案。
- 当且仅当 i18n 文案未命中时，系统 MUST 使用 `fallbackMessage` 作为 `R.error(code, msg)` 中的 `msg`。
- 当 i18n 文案命中时，系统 MUST 使用 i18n 解析结果，且 MUST NOT 被 `fallbackMessage` 覆盖。

本能力不强制限定具体方法签名；实现 MAY 采用重载（例如在 `writeResponse` 增加 `fallbackMessage` 参数）或新增明确命名的方法，但 MUST 文档化并可测试。

#### Scenario: i18n 命中时忽略兜底文案

- **WHEN** `MessageSource` 存在键 `"30402"` 对应文案，且调用写出方法时同时传入 `fallbackMessage="禁止访问"`
- **THEN** JSON 体中 `msg` MUST 为 i18n 解析到的字符串，而不是 `"禁止访问"`

#### Scenario: i18n 未命中时使用兜底文案

- **WHEN** `MessageSource` 不存在键 `"30402"` 的文案，且调用写出方法时传入 `fallbackMessage="禁止访问"`
- **THEN** JSON 体中 `msg` MUST 为 `"禁止访问"`

