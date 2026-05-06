## 1. 配置与契约

- [x] 1.1 新增 `@ConfigurationProperties(prefix = "qc.security.firewall.sensitive-word")`：`enabled`、`whiteList`、`blackList`、`ignoreUrls`、`strategy`（及 JavaDoc 示例）
- [x] 1.2 新增 `HttpCodes.SENSITIVE_WORD = 30501`；补充 `i18n/messages*.properties` 键 `30501`
- [x] 1.3 将 `quickboot-web`（或全仓）示例 `application.yml` 中 `enable` 改为 **`enabled`**，并注明 BREAKING

## 2. 词库与引擎

- [x] 2.1 实现启动期 `ResourceLoader` 加载多资源、跳过 `#` 与空行、合并内置黑名单与自定义黑白名单（houbb API）
- [x] 2.2 构建只读 Facade（REPLACE / 检测 / THROW 所需），Bean 级线程安全说明写入 JavaDoc

## 3. Filter 与请求包装

- [x] 3.1 实现 `SensitiveWordException`（码 30501 + 命中词访问器）
- [x] 3.2 实现包装 `HttpServletRequest`：`getParameter*` 走敏感词策略；`ignoreUrls` Ant 匹配则短路
- [x] 3.3 对 `application/json`：读 body → Jackson 树/Map+List 递归 String → 回写 `ServletInputStream`；非 JSON 不重写 body
- [x] 3.4 实现 `Filter`：`REPLACE` 就地改；`THROW` 抛异常并在 Filter 内 `catch` 后 `ServletUtils.writeResponse` 写 `R.error(30501, ...)`
- [x] 3.5 `FilterRegistrationBean`：`Ordered.HIGHEST_PRECEDENCE`（或等价最早），`/*` URL 模式与项目一致

## 4. 自动配置

- [x] 4.1 `@AutoConfiguration` + `@ConditionalOnWebApplication(SERVLET)` + `@ConditionalOnProperty(enabled=true)` 注册 Filter
- [x] 4.2 注册 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 5. 验证

- [x] 5.1 单元测试：词库解析（注释行）、REPLACE/THROW、递归 Map+List、`ignoreUrls` 跳过、非 JSON body 不改写
- [x] 5.2（可选）`MockMvc` 或 `ApplicationContextRunner` + Mock Filter 链集成测：JSON 嵌套、参数、30501 JSON 体
