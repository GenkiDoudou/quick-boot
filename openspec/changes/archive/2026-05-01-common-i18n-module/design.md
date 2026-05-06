## Context

原始需求见 `原始需求/后端/国际化模块.md`。工程已在前端配置层约定 `spring.messages.basename=i18n/messages`（示例位于 web 模块 `application.yml`）。需在 `quickboot-common` 提供可直接调用的 **`I18nUtil`**，将 **`MessageSource`**、 **`LocaleContextHolder`** 与 Hutool **`SpringUtil`** 组合成统一语义，供异常体系与业务提示组装使用。

## Goals / Non-Goals

**Goals:**

- 以静态工具 **`I18nUtil`** 暴露 **`getMessage`** 多重重载与 **`Locale`** 读写，默认 **`Locale`** 来自 **`LocaleContextHolder`**。
- 在无 **`MessageSource`**、词条缺失或 **`NoSuchMessageException`** 等失败路径下返回确定性兜底字符串。
- 提供可在 CI 中稳定执行的自动化测试（最小 classpath **`messages_zh_CN.properties`**）。

**Non-Goals:**

- 词条录入、翻译流程、命名空间治理或前端国际化格式。
- 自定义 **`ResourceBundle`** 加载路径偏离 Boot **`spring.messages`** 契约。
- 替代 **`LocaleResolver`** / MVC **`LocaleChangeInterceptor`** 等 Web 层语言切换完整链路（可与 Util 并存）。

## Decisions

1. **`MessageSource` 获取方式**  
   - **做法**：优先使用 Hutool **`SpringUtil.getBean(MessageSource.class)`**，捕获拿不到 Bean 或 **`NoSuchMessageException`** 时走兜底。  
   - **备选**：构造器注入 + `@Component` 封装——侵入调用方式更大；静态 **`getBean`** 与原始需求一致。

2. **兜底优先级**  
   - **做法**：若调用含 **`defaultMessage`**（包括显式传入 **`null`** 时在 JavaDoc 中说明语义）：词条缺失或 **`getMessage` 失败时返回 **`defaultMessage`**；若无 **`defaultMessage`** 重载则返回 **`code`**（字符串）。  
   - **备选**：始终返回 **`code`**——不满足「显式默认文案」场景。

3. **`Locale` 默认来源**  
   - **做法**：未传入 **`Locale`** 的参数一律使用 **`LocaleContextHolder.getLocale()`**。  
   - **备选**：再封装 **`ServletRequestAttributes`**——与设计中非 Goal 边界耦合更重。

4. **可变参数**  
   - **做法**：在二进制 **`Object[]`** 重载之外可提供更符合 JDK 习惯的 **`Object... args`** 重载（与 **`MessageFormat`** 占位兼容）；若不新增可变参数则仅用 **`Object[]`** 并在文档注明 **`null`** 表示无参数。

5. **测试策略**  
   - **做法**：使用 **`@SpringBootTest`** 最小上下文或 **`@SpringJUnitConfig`** 注册 **`ReloadableResourceBundleMessageSource`**（basename **`i18n/messages`**）并断言 **`zh_CN`** 词条命中与缺失兜底。

## Risks / Trade-offs

- **[Risk]** 非 Spring 管理的静态上下文调用 **`I18nUtil`** → **`SpringUtil`** 取 Bean 失败。  
  → **Mitigation**：兜底 **`defaultMessage`/`code`**；文档写明须在容器就绪后调用。

- **[Risk]** **`ReloadableResourceBundleMessageSource`** 与 **`spring.messages`** 配置项语义不一致。  
  → **Mitigation**：测试仅用 **`basename`** 与仓库 **`application.yml`** 对齐；生产仍以 Boot 自动配置的 **`MessageSource`** 为准。

- **[Trade-off]** 静态工具不利于单元测试 mock **`MessageSource`**。  
  → **Mitigation**：测试以上下文 Bean 为主；必要时抽取 **`protected`/包可见** 解析函数仅限测试替身（非必需）。

## Migration Plan

1. 合并后在 **`quickboot-common`** 跑 **`mvn -pl quickboot-common test`**。  
2. Web 全量启动后抽样调用 **`I18nUtil.getMessage`** 与 **`spring.messages.basename`** 下词条对齐。

## Open Questions

- 是否要求在 **`quickboot-common`** 内置 **`src/test/resources/i18n/messages_zh_CN.properties`** 样例键集合（仅占位 **`demo`**）以减少首个使用者拷贝成本。
- **`getMessage(code)`** 无 **`args`** 时是否绑定 **`null`** 参数数组或空数组——需与 **`MessageSource`** 实现保持一致并在测试中固化。
