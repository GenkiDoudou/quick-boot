## 1. 配置与自动配置骨架

- [x] 1.1 新增 `FirewallCorsProperties`（`@ConfigurationProperties(prefix="qc.security.firewall.cors")`），字段覆盖 `enabled/allowedOrigins/allowedMethods/allowedHeaders/exposedHeaders/allowCredentials/maxAge/pathPattern` 并对齐 spec 默认值
- [x] 1.2 新增 `FirewallCorsAutoConfiguration`：仅在 Servlet Web 应用且 `qc.security.firewall.cors.enabled=true` 时注册 CORS Filter（`FilterRegistrationBean`）
- [x] 1.3 将 `FirewallCorsAutoConfiguration` 加入 `quickboot-common` 的 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 2. CORS Filter 行为实现（按 spec）

- [x] 2.1 选择实现路径：优先复用 Spring Framework CORS 处理（如 `CorsFilter` / `CorsConfiguration` / `UrlBasedCorsConfigurationSource`），确保预检请求返回标准 200/204 且不强制 `R` JSON
- [x] 2.2 实现 `pathPattern` 的路径匹配与注册：仅命中路径时应用 CORS；未命中时不得写入任何 `Access-Control-*` 响应头
- [x] 2.3 实现 `allowedMethods/allowedHeaders/exposedHeaders/maxAge/allowCredentials` 到响应头的映射，并验证默认值生效
- [x] 2.4 明确并实现 `allowedOrigins` 语义：支持精确 Origin 列表与 `*`；当 `allowCredentials=true` 且 `allowedOrigins` 包含 `*` 时，必须回显请求 `Origin` 并包含 `Vary: Origin`

## 3. Filter 顺序与与其他防火墙能力协同

- [x] 3.1 确定 CORS Filter 的 order：必须早于可能写出拦截响应的防火墙 Filter（至少早于 `SensitiveWordFirewallFilter` 与 `MethodAndHostFirewallFilter`），并在文档/代码中固化该顺序
- [x] 3.2 验证“后续被拦截时仍携带 CORS 头”的协同行为：确保当后续 Filter 写出统一 JSON 时，最终响应仍包含适用的 CORS 响应头（命中 `pathPattern` 且存在 `Origin` 的情况下）

## 4. 测试（MockMvc / 集成测试）

- [x] 4.1 增加测试：`enabled=false` 时任何请求不出现 `Access-Control-*` 头
- [x] 4.2 增加测试：命中 `pathPattern` 的简单跨域请求（带 `Origin`）返回 `Access-Control-Allow-Origin` 等必要头；未命中路径不返回
- [x] 4.3 增加测试：预检 OPTIONS（含 `Origin` + `Access-Control-Request-Method`）返回成功（200/204）且包含 `Access-Control-Allow-*` 头
- [x] 4.4 增加测试：`allowedOrigins=["*"]` 且 `allowCredentials=true` 时回显 Origin（并校验 `Vary: Origin`）
- [x] 4.5 增加测试：与 `method-and-host`（或其他拦截型 Filter）叠加时，拦截响应仍携带 CORS 头（命中 `pathPattern` 且为跨域请求）

