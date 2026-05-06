## ADDED Requirements

### Requirement: I18nUtil 提供基于 MessageSource 的 getMessage

系统 MUST 在 `quickboot-common` 提供 **`I18nUtil`**，通过 Spring **`MessageSource`** 解析消息键 **`code`**。必须至少支持 **`getMessage(String code)`**、**`getMessage(String code, Object[] args)`**（或与 **`Object... args`** 等价且不歧义的一组重载）、**`getMessage(String code, Object[] args, String defaultMessage)`**。在未显式传入 **`Locale`** 的重载中，系统 MUST 使用 **`LocaleContextHolder.getLocale()`** 作为解析 **`Locale`**。

#### Scenario: 中文词条命中

- **WHEN** classpath 中存在 `messages_zh_CN.properties`（或与 **`basename`** 配置一致的 **`*_zh_CN.properties`**）且包含键 **`demo.msg`**，当前 **`Locale`** 为 **`zh_CN`**，调用 **`I18nUtil.getMessage("demo.msg")`**
- **THEN** 返回该属性文件中定义的对应中文文案

#### Scenario: 带占位参数

- **WHEN** 词条含占位符（例如 **`{0}`**）且调用 **`getMessage`** 并传入参数数组
- **THEN** 返回格式化后的文案（与 Spring **`MessageSource`** 默认占位语义一致）

### Requirement: 失败与非就绪兜底

当容器中 **`MessageSource` Bean 不可用**（例如 **`SpringUtil`** 无法获取），或在解析过程中抛出 **`NoSuchMessageException`**（或与 **`MessageSource`** 配置等价语义的无可用文案），系统 MUST：若调用方提供了 **`defaultMessage`** 参数，则返回该 **`defaultMessage`**；若当前重载未提供 **`defaultMessage`**，则 MUST 返回原始 **`code` 字符串**。

#### Scenario: 无词条且无默认消息参数时返回 code

- **WHEN** **`MessageSource`** 可用但词条 **`missing.code`** 不存在，调用 **`getMessage("missing.code")`**（无 **`defaultMessage`** 的重载）
- **THEN** 返回 **`"missing.code"`**（或与 **`code` 原始传入完全一致的非翻译字符串**）

#### Scenario: 无词条但提供默认消息

- **WHEN** 词条不存在且调用 **`getMessage(..., "兜底文案")`**
- **THEN** 返回 **`"兜底文案"`**

### Requirement: 当前 Locale 读写

系统 MUST 提供 **`getLocale()`** 与 **`setLocale(Locale locale)`**（或对 **`LocaleContextHolder`** 的等价封装），使调用方能够在当前线程读取或覆盖 **`LocaleContextHolder`** 中的 **`Locale`**，且不隐含持久化会话语义。

#### Scenario: 读取线程 Locale

- **WHEN** 已通过 **`setLocale`** 或上层 **`LocaleResolver`** 将 **`LocaleContextHolder`** 设为 **`zh_CN`**
- **THEN** **`I18nUtil.getLocale()`** 返回 **`zh_CN`**
