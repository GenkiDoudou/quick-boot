## Why

错误码与用户提示需要随语言环境返回不同文案；业务侧希望以统一入口按错误码与占位参数读取文案，并在词条缺失或基础设施异常时有可控兜底，而不是散落在各处手写字符串拼接。

## What Changes

- 在 `quickboot-common` 新增 `common/i18n`（包路径按需对齐仓库惯例，例如 `io.github.genkidoudou.common.i18n`）提供 **`I18nUtil`** 静态工具。
- 提供 **`getMessage(String code)`**、**`getMessage(String code, Object[] args)`**、**`getMessage(String code, Object[] args, String defaultMessage)`**（及等价可变参数重载若团队采纳），默认 **`Locale`** 取自 **`LocaleContextHolder.getLocale()`**。
- 提供 **`getLocale()` / `setLocale(Locale)`**（或对 `LocaleContextHolder` 的薄封装），便于在非 Web 线程或与过滤器协作时同步线程 Locale。
- 当容器中 **`MessageSource` 不可用**或 **`getMessage` 抛出无可用语义文案异常**（如 Spring **`NoSuchMessageException`**）时，返回 **`defaultMessage`**；若调用未提供默认文案则在规范中约定兜底（通常为 **`code` 字符串**）。
- **BREAKING**：若有代码此前自建同名 `I18nUtil` 或依赖不存在 Bean 时的旧行为，需核对导入包名与兜底返回值是否与新品一致。

## Capabilities

### New Capabilities

- `common-i18n`：`quickboot-common` 国际化文案读取工具能力与语义边界（依赖 Spring `MessageSource`、`LocaleContextHolder`；不负责词条内容维护）。

### Modified Capabilities

- （无）不涉及变更既有 `openspec/specs/common-cache/spec.md` 等行为规范。

## Impact

- **代码**：`quickboot-common` 新增 `I18nUtil`（及可选包可见辅助方法）；可加 classpath 测试资源 `messages_zh_CN.properties` 等用于自动化验收。
- **依赖**：沿用 **`spring-boot-starter-validation`/Boot 引入的 Spring Context** 中的 `MessageSource`、`LocaleContextHolder`；依原始需求使用 Hutool **`SpringUtil`** 获取 Bean。
- **配置**：沿用全局 **`spring.messages.basename`**（如 `i18n/messages`），词条文件维护归属业务与运维流程，不在本变更范围内扩展 CMS。
- **消费者**：全局异常处理、业务 Service、校验提示组装等凡需国际化文案处均可调用 `I18nUtil`。
