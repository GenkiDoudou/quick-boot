## Context

现有 `quickboot-common` 已提供多类“安全防火墙”能力（如 `firewall-sensitive-word`、`firewall-security-headers`、`firewall-idempotent`），其共性为：

- 通过 `qc.security.firewall.*` 前缀进行 `@ConfigurationProperties` 绑定，并由 Spring Boot 自动配置在满足开关条件时注册。
- 在需要“尽早拦截/写出统一 JSON”的场景，采用 Servlet Filter（典型为 `OncePerRequestFilter` + `FilterRegistrationBean`），并使用 `ServletUtils.writeResponse(...)` 写出 HTTP 200 + `R.error` 形态 JSON。
- 排除路径普遍使用 Ant 风格（与 `AntPathMatcher` 语义一致）。

本变更新增“请求方式与域名拦截”能力：在进入业务逻辑前拦截不允许的 HTTP Method 与异常 Host（基于 `Host` 请求头白名单），并对 Host 匹配做 lower-case 统一、端口匹配与 IPv6 支持。

同时，需求提出 `forbiddenMessage`：当 i18n 文案未命中时作为兜底文案。由于当前 `ServletUtils.writeResponse` 的契约是“业务码即词条键”，且未暴露兜底文案参数，因此本变更需要明确“兜底文案”的实现路径与公共契约边界。

## Goals / Non-Goals

**Goals:**

- 提供新的防火墙能力 `qc.security.firewall.method-and-host`：
  - `enabled` 开关默认关闭，显式开启后生效
  - `allowedMethods[]`：为空默认放行；否则白名单拦截
  - `allowedHosts[]`：为空默认放行；否则基于 `Host` 头白名单拦截
  - `excludeUrls[]`：Ant 风格，命中则跳过本能力
  - `forbiddenMessage`：i18n 未命中时作为兜底文案
- Host 校验口径固定为 `Host` 请求头；统一 lower-case；匹配端口；支持 IPv6（`[::1]:8080`）。
- 拦截输出统一 JSON（HTTP 固定 200）：
  - Method 不允许：业务码 `30401`
  - Host 不允许：业务码 `30402`

**Non-Goals:**

- 不在本能力中处理跨域语义（`Origin` / `Referer`）或 CSRF 防护。
- 不引入外部新依赖（优先复用 Spring / `quickboot-common` 现有工具与模式）。
- 不扩展到网关层面的“真实客户端 Host”追溯（如 `Forwarded` / `X-Forwarded-Host`）作为默认校验来源；仅按 `Host` 头口径执行。

## Decisions

### 1) 拦截实现：Servlet Filter（尽早）而非 MVC Interceptor

选择在 `quickboot-common` 中新增 `OncePerRequestFilter` 并通过 `FilterRegistrationBean` 注册到 `/*`，顺序尽量靠前（参考敏感词 Filter 的 `Ordered.HIGHEST_PRECEDENCE`）。原因：

- 需求要求“进入业务逻辑前拦截”，Filter 更贴近链路入口；
- host/method 校验无需依赖 HandlerMapping 等 MVC 语义，避免引入额外路径解析差异；
- 与现有防火墙风格一致，便于统一配置与行为验证。

### 2) URL 排除规则：AntPathMatcher + UrlPathHelper.getPathWithinApplication

复用现有防火墙实现的通用口径：从 request 得到 application 内 path，再用 Ant 匹配 `excludeUrls`。命中排除即跳过 method/host 校验。

### 3) Host 解析与匹配口径：只校验 Host 头，统一 lower-case，并补齐端口参与匹配

- 校验来源固定为 `Host` 请求头（避免 `Origin/Referer` 语义混淆）。
- 规范化：整体 lower-case。
- 端口：若 `Host` 头缺省端口，则使用 `request.getServerPort()` 补齐为 `host:port` 参与匹配（保证“匹配端口”的语义闭环）。
- IPv6：支持 `Host: [::1]:8080` 形式；规范化与匹配均以保留方括号的 host 表示为准。
- 匹配模式：
  - 精确：`example.com:8080`
  - 子域名通配：`*.example.com:<port>` 或 `*.example.com:*`
  - 端口通配：`host:*`（含 `localhost:*`、`[::1]:*`）

### 4) forbiddenMessage 兜底的公共契约：扩展 ServletUtils（推荐）

为保持 Filter 场景的统一写出与 i18n 规则一致性，推荐在 `common-servlet-utils` 能力中扩展 `ServletUtils.writeResponse` 的兜底文案支持。

备选方案对比：

- A. **扩展 ServletUtils（推荐）**
  - 在 `ServletUtils` 增加带 `fallbackMessage` 的重载方法（或新增明确命名的方法），当 i18n 词条未命中时使用兜底文案。
  - 优点：防火墙能力之间保持统一输出与复用；后续其它 Filter 也可复用兜底能力。
  - 代价：需要在 `common-servlet-utils` spec 中新增/扩展要求，并补充单测覆盖 i18n miss 的判定。
- B. method+host Filter 自行写出 JSON
  - Filter 直接构造 `R.error(code, msg)` 并用 ObjectMapper 写出；i18n 未命中时回退 `forbiddenMessage`。
  - 优点：修改面更小。
  - 风险：需要定义“i18n 未命中”的可靠判定口径，且容易与现有 `ServletUtils` 契约偏离（重复逻辑）。

本变更选择方案 A，以 `common-servlet-utils` 作为统一写出工具能力的归口。

## Risks / Trade-offs

- **[i18n 未命中判定不一致]** → 在 `common-servlet-utils` 规格中明确“未命中”的判定规则，并以单测固定行为（例如：MessageSource 抛异常/返回默认值时的处理）。
- **[Host 解析边界复杂（IPv6、缺省端口）]** → 在 `firewall-method-and-host` spec 的场景中覆盖：IPv6、缺省端口补齐、端口通配、子域名通配等关键用例。
- **[Filter 顺序与 Locale 设置顺序冲突]** → 复用现有约定：写出前依赖 Locale 已写入 `LocaleContextHolder`；在实现中通过 order 与文档说明约束顺序，并保持与 `ServletUtils` JavaDoc 一致。

