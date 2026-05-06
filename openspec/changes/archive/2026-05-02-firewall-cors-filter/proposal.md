## Why

当前工程的安全防火墙能力已覆盖部分输入与响应侧治理（如敏感词、响应安全头、Method/Host 白名单等），但尚缺少一套统一、可开关、可按环境快速切换的 **CORS 跨域策略**，导致前后端分离部署时容易出现浏览器跨域拦截（尤其是 OPTIONS 预检失败或拦截响应缺少 CORS 响应头）。

## What Changes

- 新增一套基于 Servlet Filter 的 CORS 能力，通过配置前缀 `qc.security.firewall.cors` 进行绑定，并支持 `enabled` 开关。
- 支持按路径模式（`pathPattern`）选择性生效，并对命中路径的 **OPTIONS 预检**确保能正确返回 CORS 响应头，从而使浏览器跨域请求可正常完成。
- 提供可配置的 `allowedOrigins` / `allowedMethods` / `allowedHeaders` / `exposedHeaders` / `allowCredentials` / `maxAge` 等常用 CORS 策略参数，并定义默认值以降低接入成本。
- 与现有防火墙 Filter 组合使用时，保证即便后续链路写出拦截响应（统一 JSON），命中路径仍能携带所需的 CORS 响应头，避免浏览器侧“看见 CORS 错误而非业务错误码”。

## Capabilities

### New Capabilities

- `firewall-cors`: 安全防火墙的 CORS 跨域能力（基于 Filter，支持开关、路径模式与常用 CORS 参数配置），用于在 Servlet 层为命中路径统一输出 CORS 响应头并放行预检请求。

### Modified Capabilities

- （无）

## Impact

- **后端代码**：主要影响 `quickboot-common`（新增自动配置、配置属性与 Filter），通过 Spring Boot 自动配置在 Servlet Web 应用中按开关注册。
- **运行时行为**：命中 `pathPattern` 的请求（含预检 OPTIONS）将出现/变化 CORS 相关响应头；Filter 顺序需要与现有防火墙能力协同，避免拦截响应缺少 CORS 头。
- **前端与部署**：在跨域部署（非 dev 代理）场景下，浏览器请求将不再被 CORS 拦截；不同环境可通过配置快速切换允许的 Origin/Method/Header。

