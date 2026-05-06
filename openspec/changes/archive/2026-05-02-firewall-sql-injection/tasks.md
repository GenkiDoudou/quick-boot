## 1. 常量、词条与配置属性

- [x] 1.1 在 `HttpCodes` 中新增 `SQL_INJECTION_DETECTED`（**30601**）及 JavaDoc
- [x] 1.2 在 `i18n/messages*.properties` 中增加键 **`30601`**（中/英或项目惯例语言）
- [x] 1.3 新增 `SqlInjectionFirewallProperties`（`@ConfigurationProperties(prefix = "qc.security.firewall.sql-injection")`）：`enabled`、`ignoreUrls`、`keywords`、`forbiddenMessage`；默认值与 spec 一致（`enabled` 默认 `false` 等）

## 2. 核心检测与 body 传递

- [x] 2.1 实现内置 **默认关键字集合**（private 常量/不可变集），文档化说明；实现大小写不敏感、子串匹配工具方法
- [x] 2.2 实现 `SqlInjectionFirewallFilter`（`OncePerRequestFilter`）：`ignoreUrls`（`AntPathMatcher`）、Query/Form 全量值检测
- [x] 2.3 对 `application/json` 请求：`Content-Type` 判断与现有一致，Jackson 树/DOM 递归收集字符串并检测；与 **design** 中「非 JSON/解析失败是否放行链」的约定一致并写清 JavaDoc
- [x] 2.4 未命中时：以 **可重复读** 方式将原始 body 字节传入下游（复用/抽取与 `SensitiveWordFirewallFilter` 兼容的 Wrapper，见 design **Decisions 4**）

## 3. 命中处理、注册顺序与自动配置

- [x] 3.1 命中时：`ServletUtils.writeResponse(response, 30601, properties.getForbiddenMessage())`；按 spec 打 **WARN** 级日志（路径、IP、方法、命中关键字、参数/JSON 路径）
- [x] 3.2 注册 `FilterRegistrationBean`：**`Ordered.HIGHEST_PRECEDENCE + 4`**，并注释说明与 CORS、敏感词（+5）、Method/Host（+10）的相对顺序
- [x] 3.3 新增 `SqlInjectionFirewallAutoConfiguration`；`@ConditionalOnProperty` + Servlet Web 条件；将类名追加到 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 4. 测试

- [x] 4.1 MockMvc/集成测试：`enabled=false` 时不拦截
- [x] 4.2 测试 `ignoreUrls` 命中时跳过
- [x] 4.3 测试 query 含典型注入片段被拦截、响应 `code=30601`
- [x] 4.4 测试 JSON 嵌套字符串命中被拦截
- [x] 4.5 测试与 **敏感词同时启用** 时：顺序正确、非 JSON 与 JSON 请求体均可被敏感词层继续处理（不重复读失败）

## 5. 配置元数据

- [x] 5.1 确认 `spring-boot-configuration-processor` 对新增属性生成元数据，IDE 可提示（已在 `quickboot-common/pom.xml` 增加 `optional` 依赖）
