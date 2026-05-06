## 1. 规格对齐与常量准备

- [x] 1.1 在 `quickboot-common` 的业务码常量（如 `HttpCodes`）中补充 `30401`/`30402`（或确认现有项目对错误码常量的归属与命名）
- [x] 1.2 为 `30401`/`30402` 补充 i18n 词条（`messages*.properties`），并在无词条时验证兜底文案路径

## 2. common-servlet-utils：兜底文案能力

- [x] 2.1 设计并实现 `ServletUtils` 的“i18n 未命中使用 fallbackMessage”的 API 形式（重载或新方法），并补充 JavaDoc 说明边界（响应已提交、Locale 顺序等）
- [x] 2.2 为 `common-servlet-utils` 补充单测：i18n 命中时不使用 fallback；i18n 未命中时使用 fallback（覆盖不同 MessageSource 行为）

## 3. firewall-method-and-host：配置与 Filter

- [x] 3.1 新增 `MethodAndHostFirewallProperties`（`qc.security.firewall.method-and-host`）并对齐默认值：`enabled=false`，列表默认空（放行语义）
- [x] 3.2 实现 `MethodAndHostFirewallFilter`（`OncePerRequestFilter`）：支持 `excludeUrls`（Ant）、`allowedMethods` 白名单、`allowedHosts` 白名单与通配匹配
- [x] 3.3 实现 Host 规范化与匹配：lower-case、缺省端口补齐、IPv6 `[]` 支持、`*.example.com` 子域名通配不匹配根域、端口通配 `*`
- [x] 3.4 在 Filter 内按拦截原因写出统一 JSON：method→`30401`，host→`30402`，并在 i18n miss 时使用 `forbiddenMessage` 兜底

## 4. 自动配置与元数据

- [x] 4.1 新增 `MethodAndHostFirewallAutoConfiguration`：在 Servlet Web 应用且 `enabled=true` 时注册 Filter（`FilterRegistrationBean`），并设置合适的 order（尽早拦截但注意 Locale 顺序）
- [x] 4.2 更新 `AutoConfiguration.imports`（如需）并确保 `spring-configuration-metadata` 正确生成（属性可提示）

## 5. 验收用例与回归

- [x] 5.1 集成测试/MockMvc：仅允许 GET 时 POST 被拦截返回 `30401`
- [x] 5.2 集成测试/MockMvc：Host 不在白名单时拦截返回 `30402`；白名单内放行；覆盖 `localhost:*`、`*.example.com:*`、IPv6（若测试环境可构造）
- [x] 5.3 测试 `excludeUrls`：命中排除路径时不拦截 method/host

