## ADDED Requirements

### Requirement: 外部 IdP 配置模型

系统 MUST 提供表 `sys_oauth_provider`（与 AS 的 `sys_oauth_client` 命名区分），字段至少包含：`provider_code`、`client_id`、`client_secret`（加密）、`authorize_url`、`token_url`、`userinfo_url` 或 `discovery_url`、`redirect_uri`、`enabled`、`auto_register`（默认 false）。

#### Scenario: 停用 IdP 不可发起登录

- **WHEN** `enabled=0` 且用户点击该 IdP 登录
- **THEN** MUST 拒绝或隐藏入口

---

### Requirement: 用户绑定表

系统 MUST 提供 `sys_oauth_user_bind`：`provider_code`、`external_subject`、`user_id`、`bind_time`；同一 `(provider_code, external_subject)` MUST 唯一对应一个 `user_id`。

#### Scenario: 已绑定用户登录

- **WHEN** 回调换得 external_subject 且存在绑定
- **THEN** 系统 MUST `StpUtil.login(user_id)` 并走与 `/login` 相同的 Admin-Token 下发方式

#### Scenario: 未绑定且禁止自动注册

- **WHEN** `auto_register=false` 且无绑定记录
- **THEN** MUST NOT 自动创建用户；须返回可理解的绑定/联系管理员错误

---

### Requirement: Client 授权与回调端点

系统 MUST 提供：

- `GET /oauth2/client/authorize/{provider}`：重定向至外部 IdP
- `GET /oauth2/client/callback/{provider}?code=...`：换 token、拉 userinfo、查绑定、登录

`redirect_uri` 固定为 `{baseUrl}/oauth2/client/callback/{provider_code}`。

#### Scenario: 完整联邦登录

- **WHEN** 用户从 `login.vue` 点击已启用 IdP 并完成外部授权
- **THEN** 浏览器 MUST 回到管理端且持有有效内部会话

#### Scenario: state 校验

- **WHEN** 回调 `state` 与发起时不一致
- **THEN** MUST 拒绝登录

---

### Requirement: AS 与 Client 命名空间隔离

代码与配置 MUST 使用 `oauth2.server.*` 与 `oauth2.client.*` 前缀；同一字符串不得混用于 AS client 表与 IdP 表。文档 MUST 说明与防火墙 `X-Client-Id`（API 调用方识别）的区别。

#### Scenario: 配置键不冲突

- **WHEN** 同时启用 server 与 client
- **THEN** `application.yml` 中两类配置 MUST 可独立开关
