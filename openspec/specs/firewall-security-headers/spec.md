# firewall-security-headers

## Purpose

`quickboot-common` 提供的 **安全防火墙 — HTTP 安全响应头**：在 Servlet 响应提交前按配置注入基线安全头，支持 **`excludeUrls`** 完全跳过与 **`excludeFromStrictPolicyUrls`** 仅放宽强策略（CSP / HSTS / Permissions-Policy）；与 `qc.security.firewall.headers` 配置前缀及 Spring Boot 自动配置集成。

## Requirements

### Requirement: 配置开关与属性前缀

系统 MUST 通过配置前缀 `qc.security.firewall.headers` 控制安全头注入；其中 `enabled` MUST 默认 `false`。`enabled=false` 时 MUST 不向响应添加本能力所管理的任何安全头。

#### Scenario: 默认关闭不产生头

- **WHEN** 未配置 `enabled` 或配置为 `false`
- **THEN** 对任意 HTTP 响应，本模块 MUST NOT 添加 `X-Frame-Options`、`X-Content-Type-Options`、`X-XSS-Protection`、`Content-Security-Policy`、`Strict-Transport-Security`、`Referrer-Policy`、`Permissions-Policy`

#### Scenario: 显式启用

- **WHEN** `qc.security.firewall.headers.enabled` 为 `true` 且请求路径未命中 `excludeUrls`
- **THEN** 系统 MUST 按本 spec 后续条款对响应设置头（在响应提交前）

### Requirement: 响应头设置与配置映射

配置键与响应头名的映射 MUST 为：

- `frameOptions` → `X-Frame-Options`
- `contentTypeOptions` → `X-Content-Type-Options`
- `xssProtection` → `X-XSS-Protection`
- `contentSecurityPolicy` → `Content-Security-Policy`
- `strictTransportSecurity` → `Strict-Transport-Security`
- `referrerPolicy` → `Referrer-Policy`
- `permissionsPolicy` → `Permissions-Policy`

**强策略头**（`contentSecurityPolicy`、`strictTransportSecurity`、`permissionsPolicy`）MUST 仅在对应配置值为**非 null 且非空字符串**时由本模块写入；不得写入空值头。

**基础头**（四项）在需写入时：若配置值为非空字符串，MUST 使用配置值；若配置缺失或为空白，MUST 使用内建默认值：`SAMEORIGIN`、`nosniff`、`1; mode=block`、`strict-origin-when-cross-origin`（与原始需求一致）。

#### Scenario: 可选强策略未配置时不出现

- **WHEN** `contentSecurityPolicy`、`strictTransportSecurity`、`permissionsPolicy` 均未配置或均为空白
- **THEN** 响应 MUST NOT 包含由本模块写入的 `Content-Security-Policy`、`Strict-Transport-Security`、`Permissions-Policy` 头

#### Scenario: 内建默认值用于基础头

- **WHEN** `enabled=true` 且路径需设置基础头（见排除条款），且四项配置均为缺失或空白
- **THEN** 响应 MUST 包含本模块写入的 `X-Frame-Options: SAMEORIGIN`、`X-Content-Type-Options: nosniff`、`X-XSS-Protection: 1; mode=block`、`Referrer-Policy: strict-origin-when-cross-origin`

### Requirement: excludeUrls 完全跳过

系统 MUST 支持 `excludeUrls` 列表，元素为 **Ant 风格**路径模式。对任一请求，若其**路径**匹配 `excludeUrls` 中任一项，本模块 MUST NOT 设置前述七种响应头中的任何一种（本模块不应写入这些头名）。

#### Scenario: 静态资源路径完全豁免

- **WHEN** `excludeUrls` 包含 `/static/**` 且请求路径为 `/static/app.js`
- **THEN** 响应 MUST NOT 包含由本模块注入的 `X-Frame-Options`、`X-Content-Type-Options`、`X-XSS-Protection`、`Content-Security-Policy`、`Strict-Transport-Security`、`Referrer-Policy`、`Permissions-Policy`

### Requirement: excludeFromStrictPolicyUrls 仅跳过强策略头

系统 MUST 支持 `excludeFromStrictPolicyUrls` 列表（Ant 风格）。当请求路径匹配其中任一项，且**未**匹配 `excludeUrls` 时：

- 系统 MUST 仍按「非空才设置」与内建默认值规则设置**基础头**：`X-Frame-Options`、`X-Content-Type-Options`、`X-XSS-Protection`、`Referrer-Policy`
- 系统 MUST NOT 设置 **强策略头**：`Content-Security-Policy`、`Strict-Transport-Security`、`Permissions-Policy`，即使这三项在配置中存在非空值

#### Scenario: Swagger 类路径放宽强策略

- **WHEN** `excludeFromStrictPolicyUrls` 包含 `/swagger-ui/**`，请求路径为 `/swagger-ui/index.html`，且不命中 `excludeUrls`
- **THEN** 响应 MUST 包含（若适用）本模块写入的基础头，且 MUST NOT 包含本模块写入的 `Content-Security-Policy`、`Strict-Transport-Security`、`Permissions-Policy`

### Requirement: 两表同时命中时 excludeUrls 优先

若同一请求路径同时匹配 `excludeUrls` 与 `excludeFromStrictPolicyUrls`，系统 MUST 适用 **完全跳过** 语义，等同仅命中 `excludeUrls`：本模块 MUST NOT 设置上述七种头。

#### Scenario: 路径重叠时以完全排除为准

- **WHEN** 某路径同时匹配 `excludeUrls` 与 `excludeFromStrictPolicyUrls`
- **THEN** 响应 MUST NOT 包含本模块注入的七种头中任一种

### Requirement: Spring Boot 集成与可测试性

能力 MUST 以 **quickboot-common** 内自动配置方式提供：当 `enabled=true` 时注册负责写头的 Servlet Filter（或 Spring 认可的等价机制），并绑定 `@ConfigurationProperties`。行为 MUST 可通过集成测试或 MockMvc 风格测试验证：在启用配置且未排除路径上，响应包含预期头；在命中排除规则时满足对应否定断言。

#### Scenario: 自动配置可被关闭

- **WHEN** `enabled=false`
- **THEN** 容器 MUST NOT 注册执行本 spec 写头逻辑的 Filter bean（或该 bean 存在但必须为 no-op，且 no-op MUST 不修改上述响应头）
