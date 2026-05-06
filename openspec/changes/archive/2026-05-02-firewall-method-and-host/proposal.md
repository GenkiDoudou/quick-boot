## Why

需要在进入业务逻辑前，对不允许的 HTTP Method 与异常 Host（域名/端口）请求进行拦截，降低被异常网关、回源链路或恶意构造 Host 绕过的风险，并让拦截行为与现有防火墙能力保持一致的统一 JSON 错误响应契约。

## What Changes

- 新增“请求方式与域名拦截”防火墙能力：在 Servlet 链路较前位置校验请求 Method 与 `Host` 请求头是否在允许列表中。
- 支持按 Ant 风格路径配置排除 URL（命中则跳过本能力）。
- Host 允许列表支持精确匹配、子域名通配（`*.example.com`）与端口通配（`localhost:*`），并支持 IPv6（`[::1]:8080`）形式。
- 拦截返回统一 JSON：
  - Method 不允许：业务码 `30401`
  - Host 不允许：业务码 `30402`
- 支持 `forbiddenMessage` 作为国际化文案未命中时的兜底文案（不覆盖已命中的 i18n 文案）。

## Capabilities

### New Capabilities

- `firewall-method-and-host`: 安全防火墙：基于 `Host` 头与 HTTP Method 的白名单拦截能力（含排除路径与通配匹配）。

### Modified Capabilities

- `common-servlet-utils`: 为支持 `forbiddenMessage` 的“i18n 未命中兜底”，可能需要扩展 Filter 场景下写出统一 JSON 的工具契约（具体选择见设计）。

## Impact

- 后端：`quickboot-common` 新增/调整防火墙自动配置、配置绑定与 Filter；可能涉及 `ServletUtils.writeResponse` / i18n 未命中判定的最小扩展。
- 配置：新增 `qc.security.firewall.method-and-host.*` 配置项。
- 文档/规范：新增对应 capability spec；可能补充 `common-servlet-utils` 中对“兜底文案”的约定。

