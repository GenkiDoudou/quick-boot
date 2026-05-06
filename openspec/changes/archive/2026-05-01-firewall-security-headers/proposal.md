## Why

前后端与网关需要一致的「基线安全响应头」以降低点击劫持、MIME 嗅探与部分 XSS 面；同时开发/文档类路径常需放宽强策略（如 CSP、HSTS）。当前仓库已在 `application.yml` 中预留 `qc.security.firewall.headers` 开关，但缺少与原始需求对齐的可验证行为规范与实现契约，需在 **quickboot-common** 侧落地可配置的注入能力与两段式排除策略。

## What Changes

- 新增「安全响应头」防火墙能力：在响应提交前按配置注入 7 类 HTTP 安全头；**强策略**头（CSP/HSTS/Permissions-Policy）仅当配置非空时写入；**基础**四头在需写入时使用配置值或内建默认值（见 spec）。
- 支持 **`excludeUrls`**（Ant）：命中则本模块**不注入**需求所列全部头。
- 支持 **`excludeFromStrictPolicyUrls`**（Ant）：命中则仍注入**基础头**（`X-Frame-Options`、`X-Content-Type-Options`、`X-XSS-Protection`、`Referrer-Policy`），**不注入** `Content-Security-Policy`、`Strict-Transport-Security`、`Permissions-Policy`；若路径同时命中两表，以 **`excludeUrls` 为准**（完全跳过）。
- 提供与 Spring Boot 3 集成的自动配置（如 Filter 注册、`@ConfigurationProperties`），默认 **`enabled` 为 false**（与原始需求一致）；应用层可按环境打开并配置排除列表。
- （可选）补齐 `application.yml` 示例键名与 Java 属性绑定约定（kebab-case ↔ 字段名），避免与现有 `qc.security.firewall` 其他子配置风格冲突。

## Capabilities

### New Capabilities

- `firewall-security-headers`：安全防火墙子模块「HTTP 安全响应头」的配置契约、排除语义、与 Servlet 过滤器行为及验收场景。

### Modified Capabilities

- （无）本次不修改 `openspec/specs/` 下已有公共能力的需求，仅新增能力 spec。

## Impact

- **代码**：`quickboot-common` 新增配置类、Filter（或等价机制）、自动配置注册；可能新增/调整 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。
- **配置**：`qc.security.firewall.headers` 下新增 `excludeFromStrictPolicyUrls` 等键；现有仅设 `enabled: true` 的环境将获得**基础头默认值 + 可选强策略**行为（见 spec）。
- **依赖**：Servlet API / Spring Web / Spring Boot 自动配置；与反向代理、Spring Security 若并存时需约定 Filter 顺序（见 design）。
- **行为**：所有经 Filter 的 HTTP 响应（含错误路径，若仍经同一链）在「未排除」时携带约定头；**非对外 REST 契约变更**，对业务 API 路径无破坏性，但对安全扫描与浏览器行为有可观测差异。
