## 1. 配置与属性绑定

- [x] 1.1 在 `quickboot-common` 新增 `@ConfigurationProperties(prefix = "qc.security.firewall.headers")` 属性类：字段覆盖 `enabled`（默认 `false`）、七类头对应的字符串配置、`excludeUrls`、`excludeFromStrictPolicyUrls`（`List<String>`），YAML 键与字段采用常规 kebab/relaxed binding
- [x] 1.2 在自动配置类上 `@EnableConfigurationProperties` 注册上述属性类

## 2. Filter 与写头逻辑

- [x] 2.1 实现 `OncePerRequestFilter`（或等价）：`shouldNotFilter` / 内部分支实现 **AntPathMatcher** 对请求路径匹配；先判 `excludeUrls`，再判 `excludeFromStrictPolicyUrls`；两表均命中时按 **完全跳过** 处理
- [x] 2.2 实现写头：完全跳过路径不写本模块七种头；**严格策略豁免**路径仅写基础四头（配置非空优先，否则内建默认）；正常路径写基础四头 + 配置非空时的 CSP/HSTS/Permissions-Policy
- [x] 2.3 使用 `response.setHeader`；在 `response.isCommitted()` 为 true 时不抛异常、不写头（与 design 一致，必要时打 debug）
- [x] 2.4 为 Filter 指定合理 `@Order`（见 `design.md`），避免与现有防火墙 Filter 写出 JSON 的次序冲突

## 3. Spring Boot 自动配置注册

- [x] 3.1 新增 `@AutoConfiguration` / 条件装配：`@ConditionalOnProperty(name = "qc.security.firewall.headers.enabled", havingValue = "true")` 注册 Filter Bean（或使用 `enabled` 字段 + `FirewallHeadersProperties` 的显式判断，与项目惯例一致）
- [x] 3.2 在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册本自动配置类

## 4. 校验、文档与示例

- [x] 4.1 为 `quickboot-common` 增加面向本 Filter 的单元/集成测试（`MockMvc` 或 `MockFilterChain`）：覆盖启用、完全排除、严格策略豁免、两表同时命中、强策略未配置不出现、基础头默认值
- [x] 4.2 （可选）在 `quickboot-web` 的 `application.yml` 增加注释示例：`excludeFromStrictPolicyUrls` 含 `/v3/api-docs/**`、`/swagger-ui/**` 等

## 5. 收尾

- [ ] 5.1 `mvn -pl quickboot-common test` 通过（本机当前默认 JDK 8，需在 **JDK 17+** 下执行以与项目一致）
- [x] 5.2 实现完成后执行 `openspec-verify-change` / 人工对照 `specs/firewall-security-headers/spec.md` 场景自查
