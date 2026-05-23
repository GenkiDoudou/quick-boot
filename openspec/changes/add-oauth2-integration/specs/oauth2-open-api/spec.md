## ADDED Requirements

### Requirement: 开放 API 路径与响应格式

系统 MUST 暴露前缀 `/open-api/v1/**` 的 REST 接口，响应继续使用统一 `R<T>`；OpenAPI `@Tag` 独立命名（如「开放 API」）。接口 MUST 使用 OAuth2 Bearer `access_token` 鉴权，不得接受仅 Admin-Token 的请求。

#### Scenario: 无 token 访问 userinfo

- **WHEN** `GET /open-api/v1/userinfo` 无 Authorization
- **THEN** MUST 返回未授权

---

### Requirement: Scope 定义与裁剪

签约 scope MUST 仅包含 `openid`（基础）与 `profile`（扩展只读字段）。系统 MUST NOT 向第三方签发含 `permissions`、`roles` 或任何写操作的 scope。`userid` scope MUST NOT 默认签约给第三方。

| Scope | 开放字段（示例） |
|-------|------------------|
| openid | `openid` |
| profile | `userName`, `nickName`, `deptName`（只读、脱敏） |

#### Scenario: 仅 openid

- **WHEN** token 仅含 `openid` scope
- **THEN** `userinfo` MUST 仅返回 `openid`

#### Scenario: 含 profile

- **WHEN** token 含 `profile` scope
- **THEN** `userinfo` MAY 返回约定 profile 字段且 MUST NOT 含权限列表

---

### Requirement: userinfo 端点

系统 MUST 提供 `GET /open-api/v1/userinfo`，使用 `@SaCheckScope` 或等价拦截校验 token；按 token scope 裁剪响应字段。

#### Scenario: 有效 token 返回 openid

- **WHEN** 请求携带有效 `access_token` 且 scope 含 openid
- **THEN** 响应 MUST 含稳定 `openid` 字符串

#### Scenario: scope 不足

- **WHEN** token 不含请求字段所需 scope
- **THEN** MUST 返回 forbidden 或省略字段（实现须在文档固定一种策略）

---

### Requirement: client_credentials 与开放 API

`client_credentials` 签发的 token MUST NOT 调用需要用户上下文的 `userinfo`；若未来提供无用户接口（如 ping），须在 spec 扩展中单独定义。

#### Scenario: 机器 token 访问 userinfo

- **WHEN** 使用 client_credentials 的 access_token 请求 userinfo
- **THEN** MUST 拒绝或返回无用户语义错误
