# OAuth2 集成

QuickBoot 基于 **Sa-Token OAuth2** 同时支持：

1. **授权服务器（AS）**：对外发放 OAuth2 token，管理 `sys_oauth_client`
2. **OAuth Client**：接入第三方 IdP（微信、企业 IdP 等），管理 `sys_oauth_provider`
3. **Open API**：Bearer `access_token` 访问 `/open-api/v1/userinfo` 等
4. **Client HMAC**：与 OAuth 客户端合并的 API 调用方鉴权（含管理端 quick-ui）

设计详稿：`docs/superpowers/specs/2026-05-23-oauth2-integration-design.md`。

---

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

---

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

前端授权确认页：`quick-ui/src/views/oauth/authorize.vue` → 提交 `/oauth2/doConfirm`。

---

## 接口授权（Ant 路径）

每个 OAuth 客户端在 `sys_oauth_client.api_path_patterns` 配置 **Ant 风格路径**（每行一条），对 **servlet path**（不含 query）匹配：

- **Client HMAC** 请求：验签通过后校验 path；
- **`/open-api/**`**：OAuth2 `access_token` 校验通过后，按 token 中的 `client_id` 校验 path。

示例（quick-ui）：

```text
/login
/system/**
/open-api/v1/userinfo
```

通配：`?` 单字符、`*` 单层、`**` 多层目录。

**是否验签**（`sign_verify`）：`1` 对该 `client_id` 执行 HMAC + Ant 授权；`0` 跳过验签（Open API 仍按 token + 路径）。全局 `qc.security.client-sign.enabled=false` 时全站不验签。

---

## Grant Type 矩阵

| 模式 | 开发 | 生产默认 |
|------|------|----------|
| authorization_code | 开 | 开 |
| refresh_token | 开 | 开 |
| client_credentials | 按 client | 按 client |
| password | 可开 | **关** |
| implicit | 可开 | **关** |

---

## Client Secret 存储

- 入库经 `PasswordCodec` **SM4** 加密；生产请设置 `QC_SM4_KEY_HEX`（32 位十六进制）。
- 签名计算使用**解密后的明文** secret。

---

## 联邦登录（Client）

| 步骤 | 接口 |
|------|------|
| 登录页拉 IdP 列表 | `GET /oauth/login/providers` |
| 发起授权 | `GET /oauth2/client/authorize/{provider_code}` |
| 回调 | `/oauth2/client/callback/{provider_code}` |

管理端配置：`views/system/oauthProvider/index.vue`。

---

## 生产配置清单

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

- 生产必须 **Redis**（token + nonce）。
- `redirect_uri` 与库中配置**精确匹配**。
- 全站 **HTTPS**；日志脱敏 `client_secret`。

---

## 相关页面与代码

| 端 | 路径 |
|----|------|
| AS 入口 | `SaOAuth2ServerController` |
| 客户端管理 API | `SysOauthClientController` → `/system/oauthClient` |
| 提供方管理 API | `SysOauthProviderController` |
| 前端客户端 | `quick-ui/src/views/system/oauthClient/index.vue` |
| 前端提供方 | `quick-ui/src/views/system/oauthProvider/index.vue` |
