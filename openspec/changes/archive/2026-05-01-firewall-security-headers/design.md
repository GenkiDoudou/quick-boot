## Context

- **业务背景**：需在 HTTP 响应上统一注入常见安全头，并允许按环境、按路径裁剪（静态资源、OAuth 回调、Swagger 等）。
- **当前状态**：`quickboot-web` 的 `application.yml` 已存在 `qc.security.firewall.headers.enabled`；`quickboot-common` 内尚未检索到对应 Filter/`@ConfigurationProperties` 实现，行为规范以 `原始需求/后端/安全防火墙-安全头管理.md` 与本次变更 spec 为准。
- **约束**：实现放在 **quickboot-common**，通过 Spring Boot 自动配置注册；与既有 `qc.security.firewall.*` 配置风格保持一致（多数字段采用 kebab-case YAML）；须兼容 **Spring Boot 3 / Jakarta Servlet**。

## Goals / Non-Goals

**Goals:**

- `enabled=true` 时，在响应**提交前**为「未完全排除」的请求设置约定安全头；**仅当配置值非空（非 null 且非空串）**时设置对应响应头。
- 支持 **Ant 风格** `excludeUrls`：命中则本模块**不设置**下列任一头：`X-Frame-Options`、`X-Content-Type-Options`、`X-XSS-Protection`、`Content-Security-Policy`、`Strict-Transport-Security`、`Referrer-Policy`、`Permissions-Policy`。
- 支持 **Ant 风格** `excludeFromStrictPolicyUrls`：命中且**未**命中 `excludeUrls` 时，仍设置**基础头**（同上列表中的前四项中的可配置子集：`X-Frame-Options`、`X-Content-Type-Options`、`X-XSS-Protection`、`Referrer-Policy`），**禁止**设置 **强策略头**：`Content-Security-Policy`、`Strict-Transport-Security`、`Permissions-Policy`（即使对应配置非空）。
- **两表同时命中**时：`excludeUrls` **优先**（完全跳过）。
- 提供清晰默认值（与原始需求一致）：`X-Frame-Options=SAMEORIGIN`，`X-Content-Type-Options=nosniff`，`X-XSS-Protection=1; mode=block`，`Referrer-Policy=strict-origin-when-cross-origin`；CSP/HSTS/Permissions-Policy 默认不设置直至配置非空。

**Non-Goals:**

- 不在本变更中定义业务错误码或异常到 HTTP 的映射（属异常模块）。
- 不强制替换 Spring Security HeaderWriter；若项目后续引入 Spring Security，仅文档化 **顺序/重复头** 风险与推荐 `Order`（不本变更内引入对 spring-security-web 的硬依赖）。

## Decisions

| 决策 | 选择 | 理由 | 曾考虑方案 |
|------|------|------|------------|
| 注入机制 | `OncePerRequestFilter`（或等价）在 **整条链末尾附近、响应未提交前** 写头 | 与 MVC/静态资源/错误分发路径兼容面大 | `HandlerInterceptor`（部分错误路径不经过）、纯 `@ControllerAdvice`（难覆盖 Filter 层早返回） |
| 配置绑定 | `@ConfigurationProperties(prefix = "qc.security.firewall.headers")` + `enabled` 默认 `false` | 与现有 `qc.security.firewall` 树一致 | 独立前缀（会增加认知负担） |
| 排除匹配 | Spring `AntPathMatcher`（与项目其他 firewall 配置惯用 Ant 描述一致） | 与原始需求「Ant 风格」一致 | 正则（配置成本高） |
| 「非空才设置」 | `null` 与 **空串** 均视为不设置该头；默认值在 `enabled` 且路径未豁免时赋予「内建默认」字段值 | 与原始需求一致 | 空串表示清空头（易与 YAML 误配混淆，不采用） |
| Filter 顺序 | 使用显式 `@Order`（建议区间：`Ordered.LOWEST_PRECEDENCE - 10` 一类），保证在多数业务 Filter 之后、仍能在提交前写头；**若与写 JSON 的防火墙 Filter 同模块**，保证**不**在 `response.isCommitted()` 之后才尝试 `setHeader` | 避免与 `ServletUtils` 等早返回路径竞态 | 固定 `HIGHEST_PRECEDENCE`（可能早于 Locale/Trace，仍可用但需实测） |
| 与网关/重复头 | 若上游已设置同名头：**默认覆盖为 `setHeader`**（后写覆盖），与「应用内统一基线」一致；若需合并 CSP 等复杂语义，不在本变更范围 | 简单可测 | `addHeader` 导致多值 CSP（难测，不默认） |

## Risks / Trade-offs

- **[Risk] 强 CSP 破坏 Swagger/内嵌页** → **缓解**：使用 `excludeFromStrictPolicyUrls` 对 `/v3/api-docs/**`、`/swagger-ui/**` 等放行强策略；生产环境单独收紧并回归。
- **[Risk] 本地 HTTP 误配 HSTS** → **缓解**：HSTS 仍「非空才设置」；文档建议仅 HTTPS/生产启用；开发环境保持空。
- **[Risk] `X-XSS-Protection` 在现代浏览器弱化** → **缓解**：保留为基线与扫描兼容；真正 XSS 面靠 CSP 与输入治理。
- **[Risk] 与 Spring Security 同时启用导致重复或冲突** → **缓解**：design 记录推荐关闭其一的安全头 writers 或调整 `Order`；必要时在后续变更对齐。

## Migration Plan

1. 合并后：`enabled` 默认 **false**，现有仓库无行为变更。
2. 启用：`qc.security.firewall.headers.enabled=true`，按需配置头字段与 `excludeUrls` / `excludeFromStrictPolicyUrls`。
3. 回滚：设回 `enabled=false` 或移除自动配置 bean（配置开关即回滚）。

## Open Questions

- 是否在 `quickboot-web` 的 `application.yml` 中提交一份**注释示例**（含 Swagger 路径的 `excludeFromStrictPolicyUrls`），以避免开箱即踩 CSP。（实现阶段由 tasks 决定是否纳入。）
