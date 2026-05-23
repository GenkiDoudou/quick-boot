## ADDED Requirements

### Requirement: OAuth2 标准端点

系统 MUST 挂载 Sa-Token OAuth2 Server，提供至少以下路径（前缀 `/oauth2/`）：`authorize`、`token`、`refresh`、`revoke`、`doLogin`、`doConfirm`、`client_token`。上述路径 MUST 加入 Sa-Token 拦截器白名单，由 OAuth2 模块处理登录态。

#### Scenario: 授权码流程入口可访问

- **WHEN** 已注册 client 请求 `GET /oauth2/authorize` 且参数合法
- **THEN** 系统 MUST 进入授权流程（登录或确认页）

#### Scenario: 授权码换 token

- **WHEN** client 使用有效 code 调用 `POST /oauth2/token`
- **THEN** 系统 MUST 返回 `access_token`（及 refresh_token 若 grant 允许）

---

### Requirement: 客户端模型 DataLoader

`SaOAuth2DataLoader` 实现 MUST 从 `sys_oauth_client` 加载 `SaClientModel`（含 `redirect_uri`、`grant_types`、`scopes`、`is_confidential`）；`getOpenid(clientId, loginId)` MUST 读写 `sys_oauth_user_openid`，无记录时生成稳定 openid 并插入。

#### Scenario: 未知 client_id 拒绝

- **WHEN** 请求携带未在库中或已停用的 `client_id`
- **THEN** 授权或换 token MUST 失败

#### Scenario: openid 稳定

- **WHEN** 同一 `client_id` 与 `user_id` 多次授权
- **THEN** 返回的 openid MUST 与首次一致

---

### Requirement: Grant Type 可配置策略

系统 MUST 支持 authorization_code、refresh_token、client_credentials、password、implicit（实现层）。生产环境 MUST 默认禁用 password 与 implicit（全局配置）；单 client 的 `grant_types` MUST 可进一步限制。

#### Scenario: dev 可测 authorization_code

- **WHEN** client 签约 `authorization_code` 且环境非 prod 限制
- **THEN** Postman/浏览器 MUST 可跑通 code 流程

#### Scenario: client_credentials 无用户 profile

- **WHEN** 使用 client_credentials 换 token
- **THEN** token MUST NOT 用于获取用户 profile 字段（无用户上下文）

---

### Requirement: OAuth 授权页登录桥接

`SaOAuth2Strategy.doLoginHandle`（或等价）MUST 调用 `AuthLoginService.authenticate`、`LoginLockService`；验证码策略 MUST 与 `qc.login.captcha-enabled` 一致（`client_credentials` 除外）。成功后 `StpUtil.login(userId)`，写登录日志且 `msg` 含 `OAuth2-AS`；MUST 执行 `LoginDataScopeService.refreshSession`（与主登录一致）。

#### Scenario: 错误密码触发锁定

- **WHEN** OAuth 登录页连续错误密码达到锁定阈值
- **THEN** 账户 MUST 与主登录页相同锁定策略

#### Scenario: 验证码开启时必填

- **WHEN** `qc.login.captcha-enabled=true` 且用户在 OAuth 流程登录
- **THEN** 无验证码 MUST 拒绝登录

---

### Requirement: 用户授权确认与记忆

系统 MUST 支持用户确认 scope（同意/拒绝）。可选表 `sys_oauth_approve` 记录用户对 client 已同意的 scope 与过期时间以减少重复确认。授权确认 UI 由 `quick-ui` `/oauth/authorize` 提供（见 `oauth2-management`）。

#### Scenario: 用户拒绝 scope

- **WHEN** 用户在确认页点击拒绝
- **THEN** 授权 MUST 失败且不签发 code

---

### Requirement: redirect_uri 与 state 安全

`redirect_uri` MUST 与 `sys_oauth_client` 登记值精确匹配（生产禁止 `*`）。OAuth2 流程 MUST 启用并校验 `state`（Sa-Token 1.39+ 能力）。

#### Scenario: redirect_uri 不匹配

- **WHEN** authorize 请求 redirect_uri 不在 client 白名单
- **THEN** 请求 MUST 被拒绝
