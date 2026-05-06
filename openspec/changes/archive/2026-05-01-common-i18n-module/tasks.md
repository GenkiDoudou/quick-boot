## 1. I18nUtil 核心实现

- [x] 1.1 在 `quickboot-common` 新增 `io.github.genkidoudou.common.i18n.I18nUtil`：`getMessage` 多重载、`getLocale`/`setLocale`，默认 `Locale` 使用 `LocaleContextHolder.getLocale()`
- [x] 1.2 通过 Hutool `SpringUtil.getBean(MessageSource.class)` 获取 `MessageSource`；Bean 缺失或 `NoSuchMessageException` 时按 spec：`defaultMessage` 优先，否则返回原始 `code`
- [x] 1.3 （可选）提供 `Object... args` 重载或与 `Object[]` 并存的文档化约定，避免与 Spring `MessageSource` 占位语义冲突

## 2. 测试与验收资源

- [x] 2.1 在 `src/test/resources` 提供最小 `i18n/messages_zh_CN.properties`（含至少一条演示键）或与现有 basename 对齐的路径
- [x] 2.2 编写单元/切片测试：`zh_CN` 词条命中、占位格式化、`missing` 键返回 `code`、提供 default 时返回 default、`LocaleContextHolder` 读写、`MessageSource` 缺失兜底

## 3. 文档

- [x] 3.1 在 `AGENTS.md`（或仓库既定文档入口）补充：`spring.messages.basename`、`I18nUtil` 使用约束（须在 Spring 容器就绪后调用）、与 Web `LocaleResolver` 协作说明（不负责切换链路）
