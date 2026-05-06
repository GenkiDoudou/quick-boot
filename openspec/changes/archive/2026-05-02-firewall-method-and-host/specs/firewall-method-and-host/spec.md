## ADDED Requirements

### Requirement: 配置前缀与开关

系统 MUST 提供安全防火墙能力 `firewall-method-and-host`，并以 `qc.security.firewall.method-and-host` 作为配置前缀。系统 MUST 支持 `enabled` 开关且默认值 MUST 为 `false`；当 `enabled=false` 时系统 MUST 不应用本能力的 method/host 拦截逻辑。

#### Scenario: 默认关闭不拦截

- **WHEN** 未配置 `qc.security.firewall.method-and-host.enabled` 或其值为 `false`
- **THEN** 任意 HTTP 请求（任意 method 与 Host）MUST NOT 因本能力而被拦截

#### Scenario: 显式开启后生效

- **WHEN** `qc.security.firewall.method-and-host.enabled=true`
- **THEN** 系统 MUST 按本 spec 的 method/host/排除路径条款对请求进行校验

### Requirement: excludeUrls 排除规则

系统 MUST 支持 `excludeUrls[]`，元素为 Ant 风格路径模式。对任一请求，若其路径匹配 `excludeUrls` 中任一项，本能力 MUST 完全跳过（不进行 method 白名单与 Host 白名单校验）。

#### Scenario: 命中排除路径后跳过

- **WHEN** `excludeUrls` 包含 `/health/**` 且请求路径为 `/health/ping`
- **THEN** 本能力 MUST 不进行 method 与 Host 校验（无论其是否在白名单内）

### Requirement: allowedMethods 语义

系统 MUST 支持 `allowedMethods[]`（字符串列表）。当 `allowedMethods` 为空列表时，系统 MUST 视为“放行所有 HTTP Method”。当 `allowedMethods` 非空时，系统 MUST 将其视为白名单：若请求 method 不在允许集合内，则 MUST 拦截并返回业务码 `30401` 的统一 JSON 响应。

#### Scenario: allowedMethods 为空时放行

- **WHEN** `allowedMethods` 为空，且请求 method 为 `POST`
- **THEN** 请求 MUST 不因 method 原因被本能力拦截

#### Scenario: method 不在白名单时拦截

- **WHEN** `allowedMethods=["GET"]` 且请求 method 为 `POST`
- **THEN** 系统 MUST 返回 HTTP 200 + 统一 JSON，且 `code` MUST 为 `30401`

### Requirement: Host 校验输入来源与规范化

系统 MUST 仅基于 `Host` 请求头执行域名/端口白名单校验（不以 `Origin`、`Referer` 等字段作为本能力的校验输入）。系统 MUST 将 `Host` 头进行规范化后参与匹配：

- 系统 MUST 将域名部分按 lower-case 规则统一（域名大小写不敏感）。
- 系统 MUST 在匹配时包含端口；当 `Host` 头未显式携带端口时，系统 MUST 使用 `request.getServerPort()` 补齐端口后参与匹配。
- 系统 MUST 支持 IPv6 `Host` 头的方括号形式（如 `[::1]:8080`），并在匹配时保持该表示的一致性。

#### Scenario: Host 缺失视为不允许

- **WHEN** `allowedHosts` 非空，且请求缺失 `Host` 头或 `Host` 为空字符串
- **THEN** 系统 MUST 返回 HTTP 200 + 统一 JSON，且 `code` MUST 为 `30402`

#### Scenario: Host 规范化为 lower-case

- **WHEN** `allowedHosts` 包含 `example.com:8080`，请求头为 `Host: ExAmPlE.CoM:8080`
- **THEN** 系统 MUST 视为匹配成功并放行（不因 Host 原因拦截）

#### Scenario: Host 缺省端口时补齐参与匹配

- **WHEN** `allowedHosts` 包含 `example.com:8080`，请求头为 `Host: example.com`，且 `request.getServerPort()` 为 `8080`
- **THEN** 系统 MUST 视为匹配成功并放行

#### Scenario: 支持 IPv6 Host 匹配

- **WHEN** `allowedHosts` 包含 `[::1]:8080`，请求头为 `Host: [::1]:8080`
- **THEN** 系统 MUST 视为匹配成功并放行

### Requirement: allowedHosts 匹配与通配符

系统 MUST 支持 `allowedHosts[]`（字符串列表）。当 `allowedHosts` 为空列表时，系统 MUST 视为“放行所有 Host”。当 `allowedHosts` 非空时，系统 MUST 将其视为白名单：若规范化后的请求 Host 不匹配任一允许项，则 MUST 拦截并返回业务码 `30402` 的统一 JSON 响应。

允许项 MUST 支持以下匹配模式：

- 精确匹配：`example.com:8080`
- 子域名通配：`*.example.com:8080` 或 `*.example.com:*`（表示 `a.example.com`、`b.c.example.com` 等任意子域名）
- 端口通配：`host:*`（如 `localhost:*`、`example.com:*`、`[::1]:*`）

子域名通配 MUST NOT 匹配根域本身（`*.example.com` 不匹配 `example.com`）。

#### Scenario: allowedHosts 为空时放行

- **WHEN** `allowedHosts` 为空，且请求头为 `Host: evil.example.com:8080`
- **THEN** 请求 MUST 不因 Host 原因被本能力拦截

#### Scenario: 精确匹配成功

- **WHEN** `allowedHosts=["example.com:8080"]`，请求头为 `Host: example.com:8080`
- **THEN** 系统 MUST 放行（不因 Host 原因拦截）

#### Scenario: 端口通配匹配成功

- **WHEN** `allowedHosts=["localhost:*"]`，请求头为 `Host: localhost:12345`
- **THEN** 系统 MUST 放行

#### Scenario: 子域名通配匹配成功

- **WHEN** `allowedHosts=["*.example.com:*"]`，请求头为 `Host: api.example.com:443`
- **THEN** 系统 MUST 放行

#### Scenario: 子域名通配不匹配根域

- **WHEN** `allowedHosts=["*.example.com:*"]`，请求头为 `Host: example.com:443`
- **THEN** 系统 MUST 返回 HTTP 200 + 统一 JSON，且 `code` MUST 为 `30402`

### Requirement: 拦截响应与兜底文案

当请求因本能力被拦截时，系统 MUST 写出 HTTP 200 + `R` 形态统一 JSON，`code` 必须与拦截原因对应：

- method 不允许 → `30401`
- Host 不允许 → `30402`

系统 MUST 使用 i18n 词条键（`String.valueOf(code)`）获取文案；当 i18n 文案未命中时，系统 MUST 使用 `forbiddenMessage` 作为兜底文案（若 `forbiddenMessage` 为空白，则使用实现定义的默认回退策略）。

#### Scenario: i18n 命中时不使用 forbiddenMessage

- **WHEN** 当前 Locale 下存在 `30402` 的 i18n 文案，且请求因 Host 不允许被拦截，同时 `forbiddenMessage` 非空
- **THEN** 响应 JSON 的 `msg` MUST 为 i18n 解析到的文案（而非 `forbiddenMessage`）

#### Scenario: i18n 未命中时回退 forbiddenMessage

- **WHEN** 当前 Locale 下 `30402` i18n 文案未命中，且请求因 Host 不允许被拦截，同时 `forbiddenMessage="禁止访问"`
- **THEN** 响应 JSON 的 `msg` MUST 为 `"禁止访问"`

