# OAuth2 双角色集成对接说明

> **文档站入口**：[OAuth2 集成](/docs/backend/modules/oauth2)（与本文内容同步，建议以文档站为准阅读）。

## 调用方鉴权（Client HMAC 签名）

与 **`sys_oauth_client` 合并**；不维护 client OAuth access_token。除白名单外，**所有 API**（含 `/login`）须携带下列请求头：

| 请求头 | 说明 |
|--------|------|
| `X-Client-Id` | `sys_oauth_client.client_id` |
| `X-Client-Timestamp` | Unix 秒 |
| `X-Client-Nonce` | 随机串（建议 32 位 hex） |
| `X-Client-Signature` | Base64(HMAC-SHA256) |

**算法**（与 `ClientSignService.buildCanonical` / `quick-ui/src/utils/clientSign.js` 一致）：

1. `bodyHash = SHA-256(rawBody)` 小写 hex；无 body 用空串。
2. 规范化串（UTF-8，`\n` 分隔）：`{METHOD}\n{path}\n{bodyHash}\n{timestamp}\n{nonce}\n{clientId}`  
   - `path`：**仅 path**，不含 query（如 `/login`，不是 `/login?username=…`）。
3. `signature = Base64( HMAC-SHA256(client_secret明文, canonical) )`。

**行为**：时间窗默认 **300s**；`CacheManager` nonce 防重放（缓存名 `clientSignNonce#300`）；`status≠0` 或已删除的 client **立即 403**。失败业务码 **30002**，HTTP **401**。

**白名单**（`qc.security.client-sign.exclude-paths`）：`/oauth2/**`、`/error`、Swagger 等；开发另含 `/actuator/**`、`/h2-console/**`（见 `application-dev.yml`）。

完整设计见 `docs/superpowers/specs/2026-05-23-oauth2-integration-design.md` §16。

### 首方 quick-ui（方案 C，已实现）

管理端在构建时注入密钥（**接受密钥进前端包的风险**，仅首方 SPA）：

| 变量 | 说明 |
|------|------|
| `VITE_APP_CLIENT_ID` | 默认 `quick-ui`（Flyway `V28__oauth2_quick_ui_client.sql`） |
| `VITE_APP_CLIENT_SIGN_KEY` | 与库中 `client_secret` 明文一致 |

- 本地：见 `quick-ui/.env.development`（开发示例密钥，勿用于生产）。
- 生产：在 CI/流水线注入上述变量；**勿**将真实 `CLIENT_SIGN_KEY` 提交仓库。
- 后端：`qc.security.client-sign.enabled=true`（默认开启）；临时关闭仅用于排障。

### 第三方 / 服务端集成

1. 在「OAuth 客户端」菜单创建 client，记录 `client_id` 与 `client_secret`（展示一次）。
2. 每个请求按上文算法加签；`client_secret` 入库为 SM4 时，签名仍用**解密后的明文**。
3. **多实例生产**须启用 Redis `CacheManager`，否则 nonce 无法跨节点防重放（与 `qc.oauth2.token-store=redis` 一并配置）。

**SDK 伪代码**：

```text
canonical = method + "\n" + path + "\n" + sha256Hex(body) + "\n" + ts + "\n" + nonce + "\n" + clientId
signature = base64( hmacSha256(clientSecret, canonical) )
```

## 授权服务器端点（AS）

| 路径 | 说明 |
|------|------|
| `/oauth2/authorize` | 授权码 / 隐式入口 |
| `/oauth2/token` | 换 token、password、refresh |
| `/oauth2/refresh` | 刷新 token |
| `/oauth2/revoke` | 撤销 token |
| `/oauth2/doLogin` | OAuth 流程内登录 |
| `/oauth2/doConfirm` | 用户确认 scope |
| `/oauth2/client_token` | 客户端凭证模式 |

## 接口授权（Ant 路径）

每个 OAuth 客户端在 `sys_oauth_client.api_path_patterns` 配置 **Ant 风格路径**（每行一条，与 Spring `AntPathMatcher` 一致），对 **servlet path**（不含 query）匹配：

- **Client HMAC 签名**请求：验签通过后校验 path；
- **`/open-api/**`**：OAuth2 `access_token` 校验通过后，按 token 中的 `client_id` 校验 path。

示例（quick-ui，见 Flyway `V30`）：

```text
/login
/system/**
/open-api/v1/userinfo
```

通配：`?` 单字符、`*` 单层、`**` 多层目录。

**防火墙**：`api_path_patterns` 含 `/**` 时可能触发 SQL 注入启发式误拦（关键字 `/*`）；已在 `qc.security.firewall.sql-injection.ignore-json-fields` 中默认跳过该 JSON 字段。

**是否验签**（`sign_verify`）：`1` 时对该 `client_id` 执行 Client HMAC + 接口 Ant 授权；`0` 时带该 Client ID 的请求跳过验签（`/open-api` 仍按 OAuth token + 接口授权）。全局开关 `qc.security.client-sign.enabled` 为 `false` 时全站不验签。

OAuth2 用户授权 scope（`openid`/`profile`）仍在库字段 `scopes` 中由服务端默认维护，用于授权页与 `userinfo` 字段裁剪；**接口能否访问**以 `api_path_patterns` 为准。

开放 API：`GET /open-api/v1/userinfo`（Bearer OAuth2 `access_token`，且 path 须命中该 client 的 Ant 规则）。

## Grant Type 矩阵

| 模式 | 开发 | 生产默认 |
|------|------|----------|
| authorization_code | 开 | 开 |
| refresh_token | 开 | 开 |
| client_credentials | 按 client | 按 client |
| password | 可开 | **关**（`qc.oauth2.server.grant.password-enabled`） |
| implicit | 可开 | **关**（`qc.oauth2.server.grant.implicit-enabled`） |

## Client Secret 存储

- 入库经 `PasswordCodec` **SM4** 加密（前缀 `{sm4:...}`），需配置 `qc.security.firewall.password.codec.sm4`（见 `application-dev.yml` 示例）。
- 开发默认密钥仅用于本地；**生产**请设置环境变量 `QC_SM4_KEY_HEX`（32 位十六进制）。

## 配置（prod 清单）

```yaml
qc:
  oauth2:
    token-store: redis
    server:
      enabled: true
      grant:
        password-enabled: false
        implicit-enabled: false
    client:
      enabled: true
  security:
    client-sign:
      enabled: true
      window-seconds: 300
      nonce-cache-name: clientSignNonce#300
spring:
  data:
    redis:
      host: ...
      port: 6379
```

- 生产必须配置 **Redis**（OAuth2 token 持久化 + Client 签名 nonce 防重放）。
- `redirect_uri` 须与 `sys_oauth_client.redirect_uris` **精确匹配**，禁止 `*`。
- 全站 **HTTPS**；`client_secret` 使用 SM4 加密存储，日志脱敏。

## 联邦登录（Client）

- 登录页：`GET /oauth/login/providers` 获取已启用 IdP。
- 跳转：`GET /oauth2/client/authorize/{provider_code}`。
- 回调：`/oauth2/client/callback/{provider_code}`，成功后重定向前端并附带 `access_token`（与 `/login` 相同 Admin-Token 语义）。
