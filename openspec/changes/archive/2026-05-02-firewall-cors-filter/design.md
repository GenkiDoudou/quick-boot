## Context

本仓库的 `quickboot-common` 已以“安全防火墙”形式提供多项 Servlet Filter 能力（敏感词、响应安全头、Method/Host 白名单等），并采用：

- `qc.security.firewall.<capability>.*` 前缀的 `@ConfigurationProperties` 绑定
- `@AutoConfiguration` + `@ConditionalOnProperty` 按开关注册 `FilterRegistrationBean`
- 通过 `FilterRegistrationBean#setOrder(...)` 控制 Filter 顺序

当前尚无统一的后端 CORS 配置入口；前端开发阶段主要依赖 Vite dev proxy（`/dev-api`）绕过浏览器跨域限制，但在真实跨域部署（不同域名/端口）时，需要后端提供可配置的 CORS 策略与预检放行。

本变更将新增 `qc.security.firewall.cors` 子能力，以与现有防火墙能力保持一致的接入方式提供。

## Goals / Non-Goals

**Goals:**

- 提供基于 Servlet Filter 的 CORS 跨域能力，并支持开关 `qc.security.firewall.cors.enabled`（默认关闭）。
- 支持通过配置项定义 `allowedOrigins/allowedMethods/allowedHeaders/exposedHeaders/allowCredentials/maxAge/pathPattern`，并具备明确默认值。
- 对命中 `pathPattern` 的请求：
  - 为简单跨域请求与预检请求输出正确的 CORS 响应头
  - 确保 OPTIONS 预检不会被 CORS 相关逻辑阻塞（允许返回标准 200/204，不强制统一 `R` JSON）
- 与现有防火墙 Filter 协同：当后续链路写出拦截响应（HTTP 200 + `R` JSON）时，若请求命中 `pathPattern` 且为跨域请求，响应仍应携带 CORS 响应头，避免浏览器仅暴露“CORS error”。

**Non-Goals:**

- 不覆盖“鉴权/权限”语义（CORS 仅决定浏览器是否允许前端读写跨域资源，不等同于授权）。
- 不在本变更中引入网关层 CORS、反向代理（Nginx）CORS 或前端构建期代理策略调整。
- 不定义基于 `Origin` 的安全防护白名单（如拒绝未知 Origin）；本能力的定位是“可配置的 CORS 策略输出”，而非替代 `method-and-host` 的 Host 白名单。

## Decisions

### Decision 1：实现形态采用 Spring Framework CORS 组件，但以 Filter 方式集成

**选择**：在 `quickboot-common` 内新增 CORS Filter（可使用 `org.springframework.web.filter.CorsFilter` 或等价的自定义 `OncePerRequestFilter` + `DefaultCorsProcessor`），并通过 `FilterRegistrationBean` 注册。

**理由**：

- CORS 语义细节多（预检判定、响应头组合、`Vary` 处理、`allowCredentials` 与 `*` 的约束），复用 Spring Framework 的 CORS 处理更稳妥。
- 仍满足“基于 Filter”的需求，并与现有防火墙能力注册方式一致，便于统一开关与测试。

**备选方案**：

- A. `WebMvcConfigurer#addCorsMappings`（放弃）：更偏 MVC 层，且与本仓库既有防火墙 Filter 模式不一致。
- B. Spring Security `http.cors()`（放弃）：当前工程未体现 Security 链路，且该方式依赖 Security 过滤链配置，落点不如 `quickboot-common` 的通用 Filter 稳定。

### Decision 2：`allowedOrigins="*"` 且 `allowCredentials=true` 的语义采用“回显 Origin”

**选择**：当配置 `allowedOrigins` 包含 `"*"` 且 `allowCredentials=true` 时，允许任意 Origin，但响应 `Access-Control-Allow-Origin` 不使用 `*`，而是回显请求的 `Origin`（并输出 `Vary: Origin`）。

**理由**：

- 浏览器规范不允许 `Access-Control-Allow-Origin: *` 与 `Access-Control-Allow-Credentials: true` 同时出现。
- 原始需求明确“allowedOrigins 支持 `*`”且 `allowCredentials` 默认 `true`，因此必须选择一种可运行且符合规范的组合语义。

**备选方案**：

- 启动时报错/拒绝该组合（更严格，但与“默认 allowCredentials=true 且支持 *”相冲突，会显著增加接入成本）。

### Decision 3：Filter 顺序应尽量靠前，确保拦截响应也携带 CORS 头

**选择**：CORS Filter 的 order 必须早于可能写出拦截响应的防火墙 Filter（如 `SensitiveWordFirewallFilter`、`MethodAndHostFirewallFilter`），以保证命中 `pathPattern` 的跨域请求在被拦截时仍能携带 CORS 响应头。

**理由**：

- 若拦截响应缺少 CORS 头，浏览器会在网络层拦截响应，前端无法读取到统一 `R` JSON，导致排障困难与用户体验差。

## Risks / Trade-offs

- **[风险] Filter 顺序与现有能力冲突** → **缓解**：明确 order 规则并通过集成测试验证“拦截响应也带 CORS 头”的场景。
- **[风险] `pathPattern` 语义与已有 Ant 匹配不一致** → **缓解**：统一以应用内路径（pathWithinApplication）与 Ant 风格匹配；在 spec 中明确匹配对象。
- **[风险] 误把 CORS 当作安全边界** → **缓解**：在 spec/文档中声明 Non-Goals：CORS 不替代鉴权授权与 Host 白名单。

