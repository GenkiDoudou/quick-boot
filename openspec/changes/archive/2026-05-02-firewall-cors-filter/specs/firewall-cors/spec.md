## ADDED Requirements

### Requirement: 配置前缀与开关

系统 MUST 提供安全防火墙能力 `firewall-cors`，并以 `qc.security.firewall.cors` 作为配置前缀。系统 MUST 支持 `enabled` 开关且默认值 MUST 为 `false`；当 `enabled=false` 时系统 MUST 不应用本能力的 CORS 策略输出逻辑。

#### Scenario: 默认关闭不产生 CORS 响应头

- **WHEN** 未配置 `qc.security.firewall.cors.enabled` 或其值为 `false`
- **THEN** 对任意请求/响应，系统 MUST NOT 因本能力而添加任何 `Access-Control-*` 响应头

#### Scenario: 显式开启后生效

- **WHEN** `qc.security.firewall.cors.enabled=true`
- **THEN** 系统 MUST 按本 spec 的路径匹配与策略配置条款为命中请求输出 CORS 响应头

### Requirement: 可配置项与默认值

系统 MUST 支持下列配置项（前缀均为 `qc.security.firewall.cors`），并具备默认值：

- `allowedOrigins`：默认空（实现定义其默认行为，见后续条款），并 MUST 支持 `*`
- `allowedMethods`：默认 `GET/POST/PUT/DELETE/OPTIONS`
- `allowedHeaders`：默认 `*`
- `exposedHeaders`：默认空
- `allowCredentials`：默认 `true`
- `maxAge`：默认 `3600`（秒）
- `pathPattern`：默认 `/**`

#### Scenario: 未显式配置时使用默认值

- **WHEN** `qc.security.firewall.cors.enabled=true` 且除 `enabled` 外未配置任何项
- **THEN** 系统 MUST 以本条款定义的默认值作为 CORS 策略输入

### Requirement: 仅对匹配路径模式生效

系统 MUST 支持 `pathPattern`（Ant 风格路径模式），仅当请求路径匹配该模式时才应用本能力的 CORS 逻辑；未命中时，本能力 MUST 不修改响应（不添加 CORS 头、不拦截）。

#### Scenario: 命中路径模式才输出 CORS 头

- **WHEN** `pathPattern=/api/**` 且请求路径为 `/api/user/profile`
- **THEN** 系统 MUST 应用 CORS 策略并输出相应 CORS 响应头

#### Scenario: 未命中路径模式不输出 CORS 头

- **WHEN** `pathPattern=/api/**` 且请求路径为 `/static/app.js`
- **THEN** 系统 MUST NOT 因本能力而添加任何 CORS 响应头

### Requirement: 预检 OPTIONS 请求必须通过

当 `enabled=true` 且请求命中 `pathPattern`，并且请求为浏览器 CORS 预检（含 `Origin` 与 `Access-Control-Request-Method` 等必要头）时：

- 系统 MUST 返回成功的 HTTP 响应（实现 MAY 返回 200 或 204）。
- 系统 MUST 在响应中包含满足本能力配置的 `Access-Control-Allow-*` 相关响应头。
- 本能力 MUST NOT 强制将预检响应包装为统一 `R` JSON。

#### Scenario: OPTIONS 预检放行并返回允许策略

- **WHEN** `enabled=true` 且命中 `pathPattern`，且请求为跨域预检 OPTIONS
- **THEN** 响应 MUST 为成功状态（200/204 之一）且包含 `Access-Control-Allow-Origin`、`Access-Control-Allow-Methods`、`Access-Control-Allow-Headers` 等所需响应头

### Requirement: `*` 与凭证（credentials）的组合语义

当 `allowCredentials=true` 且 `allowedOrigins` 包含 `*` 时，系统 MUST 允许任意 Origin，但响应 `Access-Control-Allow-Origin` MUST NOT 为 `*`，而 MUST 回显请求 `Origin` 的值；系统 MUST 输出 `Vary: Origin` 以避免缓存污染。

#### Scenario: `*` + credentials 时回显 Origin

- **WHEN** `allowedOrigins=["*"]` 且 `allowCredentials=true` 且请求头 `Origin: https://example.com`
- **THEN** 响应头 `Access-Control-Allow-Origin` MUST 为 `https://example.com` 且响应 MUST 包含 `Vary: Origin`

### Requirement: Filter 顺序与拦截响应兼容

当 `enabled=true` 且请求命中 `pathPattern` 且为跨域请求时，即使后续链路（其他 Filter/防火墙能力）对该请求写出了拦截响应（例如 HTTP 200 + 统一 `R` JSON），最终响应仍 SHOULD 携带 CORS 响应头，以保证浏览器可见业务错误信息。

#### Scenario: 被后续防火墙拦截时仍携带 CORS 头

- **WHEN** `enabled=true` 且请求命中 `pathPattern` 且为跨域请求，且后续 Filter 写出拦截响应
- **THEN** 最终响应 MUST 仍包含适用的 `Access-Control-Allow-Origin` 等 CORS 响应头

### Requirement: Spring Boot 集成与可测试性

能力 MUST 以 **quickboot-common** 内自动配置方式提供：当 `enabled=true` 时注册负责输出 CORS 响应头与处理预检的 Servlet Filter（或 Spring 认可的等价机制），并绑定 `@ConfigurationProperties`。行为 MUST 可通过集成测试或 MockMvc 风格测试验证：启用后预检通过、命中路径输出头、未命中路径不输出头。

#### Scenario: 自动配置可被关闭

- **WHEN** `enabled=false`
- **THEN** 容器 MUST NOT 注册执行本 spec CORS 逻辑的 Filter bean（或该 bean 存在但必须为 no-op 且不写入任何 `Access-Control-*` 响应头）

