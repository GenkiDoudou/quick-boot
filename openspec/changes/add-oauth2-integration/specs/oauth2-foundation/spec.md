## ADDED Requirements

### Requirement: Sa-Token 版本升级与 OAuth2 依赖

系统 MUST 将父 POM `sa-token.version` 升级至 **1.44.0 或 1.45.0**，并引入 `sa-token-oauth2` 及 OAuth2 所需的 Redis 整合依赖（`sa-token-redis-template` 或项目等价方案）。升级后现有 `/login`、`/logout`、`/getInfo`、`/getRouters` 行为 MUST 与升级前一致（P0 回归）。

#### Scenario: 账号密码登录仍可用

- **WHEN** 管理员使用现有账号密码调用 `/login`
- **THEN** 系统 MUST 签发内部会话 token 且 `getInfo` 返回用户信息

#### Scenario: OAuth2 模块可加载

- **WHEN** 应用启动且 `qc.oauth2.server.enabled=true`
- **THEN** `SaOAuth2ServerProcessor`（或 1.44+ 等价入口）MUST 可处理 `/oauth2/*` 请求

---

### Requirement: OAuth2 数据 Redis 持久化

OAuth2 授权码、access_token、refresh_token 等 Server 侧状态 MUST 持久化到 Redis（生产环境）；不得仅依赖单机内存作为生产默认。

#### Scenario: 多实例共享 token 状态

- **WHEN** 实例 A 签发 authorization code 且实例 B 处理 `/oauth2/token` 换 token
- **THEN** 换 token MUST 成功（同一 Redis）

#### Scenario: 生产启动校验

- **WHEN** `spring.profiles.active` 含 `prod` 且 Redis 未配置或不可达
- **THEN** 应用 MUST 拒绝启动或记录明确错误（实现二选一，须在配置文档说明）

---

### Requirement: 三轨认证路径隔离

系统 MUST 区分三类凭证：（1）内部 `StpUtil` Admin-Token 访问 `/system/*`、`/monitor/*`；（2）OAuth2 `access_token` 仅访问 `/open-api/**`；（3）外部 IdP 的 code/token 仅用于 Client 回调换本地会话。OAuth2 `access_token` MUST NOT 通过管理端权限拦截器；内部 Admin-Token MUST NOT 仅凭 scope 逻辑访问开放 API。

#### Scenario: OAuth2 token 访问管理 API 被拒绝

- **WHEN** 请求携带有效 OAuth2 `access_token` 访问 `GET /system/user/list`
- **THEN** 响应 MUST 为未登录或 forbidden（HTTP 与项目 `GlobalExceptionHandler` 一致）

#### Scenario: Admin-Token 访问 open-api 无 scope

- **WHEN** 请求仅携带内部 Admin-Token 访问 `GET /open-api/v1/userinfo`
- **THEN** 响应 MUST 为未授权（不得返回第三方 userinfo）

---

### Requirement: 全局 OAuth2 配置项

系统 MUST 支持配置：

- `qc.oauth2.server.enabled`（默认 true）
- `qc.oauth2.server.grant.password-enabled`（prod 默认 false）
- `qc.oauth2.server.grant.implicit-enabled`（prod 默认 false）
- `qc.oauth2.client.enabled`（默认 true）
- `qc.oauth2.client.default-redirect-after-login`（默认 `/`）

#### Scenario: 生产默认关闭 password grant

- **WHEN** profile 为 prod 且未显式开启 `password-enabled`
- **THEN** password grant 请求 MUST 被拒绝
