# QuickBoot 登录与 OAuth 客户端架构设计

日期：2026-07-27  
状态：与当前代码对齐（活文档）  
关联：

- `docs/superpowers/specs/2026-07-26-spring-security-oauth2-as-design.md`
- `docs/superpowers/specs/2026-07-26-oauth-client-secret-plaintext-design.md`
- `docs/superpowers/specs/2026-07-27-login-client-basic-design.md`

---

## 1. 一句话与能力范围

**一句话：** 同进程内 Spring Authorization Server（AS）是唯一发 JWT 的柜台；管理端 SPA 通过「混淆 Basic 校验客户端 + JSON 校验用户」的门面登录拿用户 JWT；客户端元数据由 MyBatis 管理表 `oauth2_registered_client`；业务 API 用 Resource Server 验 Bearer。

| 能力 | 用户感知 | 技术要点 |
|------|----------|----------|
| 密码登录 | 账号密码进系统 | `POST /auth/login` + 无 JWT 时混淆 Basic |
| 刷新 | 静默续期（前端待接） | `POST /auth/refresh` |
| 当前用户 | 拉取资料 | `GET /auth/me` |
| 社交登录 | 点 Gitee 等 | OAuth2 Client + ticket 交接 |
| 外部 App | 授权码 / client_credentials / 可选 password | 标准 `/oauth2/*` |
| 客户端管理 | 后台 CRUD + 查看凭证 | `/system/oauth-clients` |

**明确不做（本期）：** JustAuth、Sa-Token、BFF、PKCE 公共客户端强制、验证码后端（前端钩子已预留）。

---

## 2. 模块与依赖

```text
quick-ui (Vue)
    │  /dev-api → 9993
    ▼
quickboot-web          启动、YAML、DDL、嵌入式 Redis
    ├── quickboot-system   用户、社交绑定、OAuth 客户端 CRUD/种子、AuthUserLookup 实现
    └── quickboot-auth     AS/RS、门面登录、Basic Filter、社交 Handler、Token 发牌
            └── quickboot-common   R / HttpCodes 等
```

### 2.1 关键类一览

| 层级 | 类 | 路径（模块内） |
|------|-----|----------------|
| AS 链 | `AuthorizationServerConfig` | `auth/.../security/AuthorizationServerConfig.java` |
| RS 链 | `ResourceServerConfig` | `auth/.../security/ResourceServerConfig.java` |
| 客户端校验 | `ClientBasicAuthenticationFilter` | `auth/.../security/client/` |
| 混淆 | `ClientCredentialObfuscator` | 同上 |
| 密码编码 | `DualModePasswordEncoder` | `auth/.../security/` |
| 门面 | `AuthController` / `AuthTokenService` | `auth/.../web`、`auth/.../token` |
| JWT claims | `TokenClaimCustomizer` | `auth/.../token` |
| password grant | `OAuth2ResourceOwnerPassword*` | `auth/.../security/password` |
| 客户端管理 | `OAuthClientController` / `OAuthClientService` | `system/.../oauth` |
| 持久化 | `MybatisRegisteredClientRepository` | `system/.../oauth` |
| 用户端口 | `AuthUserLookup` ← `SysUserAuthService` | auth port / system |

---

## 3. 总体架构

```text
                 ┌──────────────────────────────────────────┐
                 │     Authorization Server (Order 1)        │
                 │  /oauth2/authorize  /oauth2/token  /jwks  │
                 │  + password 扩展 grant                    │
                 └───────────────┬──────────────────────────┘
                                 │ 同一 TokenGenerator / 同一表
                 ┌───────────────▼──────────────────────────┐
                 │     Resource Server 链 (Order 2)          │
                 │  Bearer JWT + formLogin + oauth2Login     │
                 │  ClientBasicAuthenticationFilter          │
                 │  /auth/login|refresh|me|social/**         │
                 │  /system/oauth-clients/**                 │
                 └───────────────┬──────────────────────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              ▼                  ▼                  ▼
         quick-ui SPA      外部 OAuth App      机机 job-runner
         Basic+登录门面     授权码/cc/password   client_credentials
```

### 3.1 双过滤器链

| Order | 匹配 | 职责 |
|-------|------|------|
| 1 | AS `endpointsMatcher` | 标准 OAuth2 端点 |
| 2 | `/**` | 门面、业务 API、社交、客户端管理 |

---

## 4. 无 JWT 时的客户端校验（S2）

### 4.1 规则

1. 已有**合法用户** Bearer JWT（`token_kind != client`）→ **不验** Basic。  
2. 否则 → 必须 `Authorization: Basic base64(混淆id:混淆secret)`。  
3. 校验：解混淆 → `findByClientId` → `PasswordEncoder.matches`（明文 secret 走 equals）。  
4. 成功：request attribute `qc.oauth.registeredClient` = `RegisteredClient`。  
5. 失败：HTTP 200 + `R`，`code=401`，`msg=客户端无效`（避免枚举）。

### 4.2 排除路径

`/actuator/**`、`/h2-console/**`、`/error`、`/login`、`/oauth2/authorization/**`、`/login/oauth2/**`、`/favicon.ico`，以及 CORS `OPTIONS`。

> 维护提示：新增「浏览器跳转、无法带 Basic」的入口时，加入排除名单；新增 axios 调用的免登录 API **不必**改名单（自动要求 Basic）。

### 4.3 混淆算法（前后端一致）

```text
plain UTF-8 bytes
  → XOR 循环盐 "QuickBootOAuth1"
  → URL-safe Base64（无 padding）
  → 得到 obfuscatedId / obfuscatedSecret
  → pair = obfId + ":" + obfSecret
  → Authorization: Basic + 标准 Base64(pair)
```

后端：`ClientCredentialObfuscator`  
前端：`quick-ui/src/utils/oauthClientBasic.js`

### 4.4 代码示例：组装 Basic（前端）

```javascript
import { buildObfuscatedBasicAuthorization } from '@/utils/oauthClientBasic'

// 读 VITE_OAUTH_CLIENT_ID / VITE_OAUTH_CLIENT_SECRET
const header = buildObfuscatedBasicAuthorization()
// "Basic xxxxx"
```

### 4.5 代码示例：登录请求（curl）

```bash
# 1) 用管理端「查看凭证」复制出的 Authorization 行，或自建混淆
AUTH='Basic ....'

curl -s http://127.0.0.1:9993/auth/login \
  -H "Content-Type: application/json" \
  -H "Authorization: $AUTH" \
  -d '{"username":"admin","password":"admin123"}'
```

无 Basic：

```json
{"code":401,"msg":"客户端无效","success":false,...}
```

---

## 5. 门面登录 / 刷新 / 当前用户

### 5.1 `POST /auth/login`

| 项 | 内容 |
|----|------|
| Body | `{ "username", "password" }`（**不含** client 凭证） |
| Header | 无用户 JWT 时必须混淆 Basic |
| 逻辑 | Basic 客户端须含 `password` grant → 验用户 BCrypt → `issueUserToken(account, registeredClient)` |
| 响应 | `accessToken`、`refreshToken?`、`expiresIn`、`tokenKind=user` |

```java
// AuthTokenService.login（摘要）
RegisteredClient client = requireValidatedClientFromRequest();
if (!client.getAuthorizationGrantTypes().contains(PasswordGrantSupport.PASSWORD)) {
  throw new IllegalArgumentException("password grant not allowed for client");
}
// 验用户 → issueUserToken(account, client)
```

### 5.2 `POST /auth/refresh`

Body：`{ "refreshToken" }`  
Basic 校验的 client **内部 id** 必须与 `OAuth2Authorization.registeredClientId` 一致，再重新发牌。

> 现状：未做 refresh 轮换/作废；前端尚未统一接入刷新接口。

### 5.3 `GET /auth/me`

Bearer 用户 JWT；拒绝 `token_kind=client`。

### 5.4 与标准 `POST /oauth2/token`（password）对比

| | 门面 `/auth/login` | AS `/oauth2/token` |
|--|-------------------|---------------------|
| 客户端认证 | 混淆 Basic + Filter | 标准 Basic/POST（明文 id:secret） |
| 用户凭据 | JSON | form `username`/`password` |
| 响应 | 项目 `R` + camelCase | OAuth2 标准 |
| 发牌器 | 同一 `OAuth2TokenGenerator` | 同左 |

任意 RegisteredClient 只要注册了 `password` grant 即可使用（**无**写死 `quick-ui` 限制）。

---

## 6. JWT 约定

`TokenClaimCustomizer`（仅 access token）：

| Grant | `token_kind` | `sub` |
|-------|--------------|-------|
| `client_credentials` | `client` | `clientId` |
| 其它（password / code / refresh 等） | `user` | `userId` |

另写入 `client_id`。  
`DualModePasswordEncoder`：`encode` 恒 BCrypt；`matches` 对 `$2a$/$2b$/$2y$` 走 BCrypt，否则明文 equals（客户端密钥）。

---

## 7. OAuth 客户端管理

### 7.1 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/system/oauth-clients` | 列表（可选分页） |
| GET | `/system/oauth-clients/{clientId}` | 详情（无 secret） |
| POST | `/system/oauth-clients` | 创建；**服务端生成** secret；响应含一次性明文 + `authorization` |
| PUT | `/system/oauth-clients/{clientId}` | 更新；`regenerateSecret=true` 时轮换密钥 |
| DELETE | `/system/oauth-clients/{clientId}` | 删除（无特殊 client 保护） |
| POST | `/system/oauth-clients/{clientId}/reveal-secret` | Body `{password}`；回显 id/secret/混淆 Authorization |

### 7.2 前端页面

`quick-ui/src/views/system/oauthClient/index.vue`

- 新增不手填密钥；修改可选「重新生成密钥」  
- 查看：管理员密码 → 凭证文本块 + **复制全部**  
- 字段旁小眼睛说明各选项含义  

### 7.3 种子客户端（开发）

| clientId | secret | grants |
|----------|--------|--------|
| `quick-ui` | `quick-ui-secret` | password, refresh, authorization_code |
| `demo-app` | `demo-app-secret` | authorization_code, refresh |
| `job-runner` | `job-runner-secret` | client_credentials |

种子用户：`admin` / `admin123`。

---

## 8. 社交登录（摘要）

```text
GET /oauth2/authorization/{id}
  → IdP → /login/oauth2/code/{id}
  → SocialLoginSuccessHandler
       已绑定 → issueUserToken(account) → ticket → 前端 complete
       未绑定 → ticket PENDING → 前端 bind（auto-create / bind）
```

社交发牌客户端由配置指定（**非**代码死判断）：

```yaml
qc:
  oauth:
    social-issue-client-id: quick-ui   # 须已存在于 oauth2_registered_client
```

相关：`/auth/social/pending|auto-create|bind|complete`，开发 mock：`POST /auth/social/dev/pending`（`qc.auth.social.dev-mock=true`）。

---

## 9. 前端请求约定

`quick-ui/src/utils/request.js`：

```javascript
if (getToken() && headers.isToken !== false) {
  // Bearer 用户 JWT
} else {
  // 注入混淆 Basic（VITE_OAUTH_CLIENT_ID / SECRET）
}
```

环境变量（开发示例）：

```env
VITE_OAUTH_CLIENT_ID=quick-ui
VITE_OAUTH_CLIENT_SECRET=quick-ui-secret
VITE_APP_BASE_API=/dev-api
```

登录页：`views/login.vue` → `store/modules/user.js` → `POST /auth/login`。

---

## 10. 运行时配置摘要

| 项 | 值 |
|----|-----|
| HTTP 端口 | `9993` |
| Issuer | `http://127.0.0.1:9993`（当前 Java 硬编码，可外化） |
| 嵌入式 Redis | `qc.dev.embedded-redis.port=6379` |
| 社交回调前端 | `qc.auth.social.frontend-*` |

端口冲突：`script/free-dev-ports.ps1`。

---

## 11. 扩展点设计

以下扩展点按「改动面从小到大」排列，便于增量加能力。

### 11.1 扩展点地图

```text
┌─ 前端 login.vue ── getLoginCaptchaConfig / Tianai 弹层（已有钩子）
│
├─ axios 拦截器 ── 统一注入 Basic / Bearer / 未来设备指纹头
│
├─ ClientBasicAuthenticationFilter
│     ├─ 前：限流、IP 黑名单、Bot 检测
│     └─ 后：已绑定 RegisteredClient，可供下游读取
│
├─ AuthController.login / AuthTokenService.login
│     ├─ 验证码校验
│     ├─ MFA challenge（不直接发牌）
│     └─ 审计日志
│
├─ OAuth2ResourceOwnerPasswordAuthenticationProvider
│     └─ 标准 /oauth2/token password 路径的同等扩展
│
├─ AuthorizationServerConfig.tokenEndpoint
│     └─ 新 grant：仿 PasswordGrantSupport + Converter + Provider
│
└─ TokenClaimCustomizer
      └─ amr / acr / mfa / tenant 等 claims
```

---

### 11.2 如何增加登录验证码（推荐落地步骤）

**现状：**

- 前端已调用 `GET /login/captcha-config`；失败则 `captchaEnabled=false`，直登。  
- Tianai：`/api/captcha/generate`、`/api/captcha/validate`（前端期望）。  
- `POST /auth/login` **尚未**接收/校验 `captchaId`。

**推荐方案：门面登录强制校验；标准 `/oauth2/token` 可选。**

#### 步骤 A — 配置开关

```yaml
# application-dev.yml
qc:
  login:
    captcha-enabled: true
```

#### 步骤 B — 配置接口（对齐前端）

```java
@RestController
public class LoginCaptchaConfigController {
  @Value("${qc.login.captcha-enabled:false}")
  private boolean captchaEnabled;

  @GetMapping("/login/captcha-config")
  public R<Map<String, Object>> config() {
    return R.ok(Map.of("captchaEnabled", captchaEnabled));
  }
}
```

并在 `ResourceServerConfig` 将该路径 `permitAll`（仍会走 Basic Filter，与现登录一致）。

#### 步骤 C — Captcha 服务（示例接口契约）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST | `/api/captcha/generate` | 返回滑块/点选数据 + `id` |
| POST | `/api/captcha/validate` | 校验轨迹；成功则 Redis 写入 `captcha:ok:{id}` TTL 2～5 分钟 |

可用现成 Tianai 服务或自研；存储建议 Redis（项目已有嵌入式 Redis）。

#### 步骤 D — 登录体扩展

```java
// AuthController
public record LoginRequest(String username, String password, String captchaId) {}

// AuthTokenService.login 开头
if (captchaEnabled) {
  if (!StringUtils.hasText(captchaId) || !captchaService.consume(captchaId)) {
    throw new IllegalArgumentException("captcha required");
  }
}
```

`consume`：**一次性**删除 Redis key，防重放。

#### 步骤 E — 前端打通

```javascript
// api/login.js
export function login(username, password, captchaId) {
  return request({
    url: '/auth/login',
    headers: { isToken: false },
    method: 'post',
    data: { username, password, captchaId }
  })
}
```

`login.vue` 在 Tianai `onSuccess` 后把 `captchaId` 传入 `userStore.login`（页面已有 `passwordForm.captchaId` 字段，补齐 store → API 即可）。

#### 步骤 F — 与 Basic 的关系

验证码校验放在 **用户密码之前或之后均可**，但应在 `AuthTokenService.login` 内、发牌前；**不要**塞进 Basic Filter（Filter 只认客户端）。

#### 验证码时序

```text
GET /login/captcha-config → captchaEnabled=true
  → 用户点登录 → Tianai generate/validate
  → POST /auth/login + Basic + {username,password,captchaId}
       → Filter 验 client
       → Service consume captcha
       → 验用户密码
       → 发 JWT
```

---

### 11.3 如何增加 MFA（摘要）

1. `login` 成功后不直接返回 accessToken，返回 `{ mfaRequired: true, mfaToken }`（短时签名票）。  
2. 新增 `POST /auth/mfa/verify`：验 TOTP/短信 → `issueUserToken`。  
3. `TokenClaimCustomizer` 可写 `amr: ["pwd","otp"]`。  
4. 社交路径在 `SocialAuthController` 同样分支。

---

### 11.4 如何增加限流

| 挂载 | Key 建议 |
|------|----------|
| Filter 前（无 JWT） | `rl:client:{clientId}:{ip}` |
| `AuthTokenService.login` | `rl:login:{username}:{ip}` |
| password Provider | 同左（防绕过门面） |

用 Redis `INCR` + TTL；超限返回统一业务码。

---

### 11.5 如何增加新的 OAuth Grant

1. 常量类仿 `PasswordGrantSupport`。  
2. `AuthenticationConverter` + `AuthenticationProvider`。  
3. 在 `AuthorizationServerConfig` 的 `tokenEndpoint` 注册。  
4. 管理端 grantTypes 下拉增加选项 + 小眼睛说明。

---

### 11.6 其它扩展清单

| 能力 | 挂载点 |
|------|--------|
| Refresh 轮换 | `AuthTokenService.refresh` + 作废旧 refresh |
| Social ticket 集群 | 替换 `SocialTicketStore` → Redis |
| Issuer 多环境 | `AuthorizationServerSettings` 外化配置 |
| 审计 | login / password Provider / social Handler 打点 |
| 机机 API | Controller 校验 `token_kind=client` + scopes |
| PKCE 公共客户端 | `OAuthClientService.toRegisteredClient` 支持 public + PKCE |

---

## 12. 安全说明（务必写进运维认知）

1. **混淆 ≠ 加密**：防翻源码，不防抓包；生产必须 HTTPS。  
2. **客户端密钥明文入库**：便于管理端查看；依赖库权限与 reveal 二次密码。  
3. **SPA 持有 client_secret**：属于 confidential-client 在浏览器的折中；长期可迁 BFF 或纯 PKCE。  
4. **password grant**：任意注册该 grant 的客户端可用，创建时务必审慎勾选。

---

## 13. 验收速查

| 场景 | 期望 |
|------|------|
| 无 Basic 调 `/auth/login` | `客户端无效` |
| 正确 Basic + admin/admin123 | 返回用户 JWT |
| Bearer 调 `/auth/me`（无 Basic） | 成功 |
| 客户端管理创建 | 自动 secret + 可复制 Authorization |
| reveal 错误管理员密码 | 失败 |
| `captcha-enabled=false` | 前端直登（当前默认行为） |

---

## 14. 相关代码索引

```text
quickboot-auth/
  security/AuthorizationServerConfig.java
  security/ResourceServerConfig.java
  security/client/ClientBasicAuthenticationFilter.java
  security/client/ClientCredentialObfuscator.java
  security/DualModePasswordEncoder.java
  token/AuthTokenService.java
  token/TokenClaimCustomizer.java
  web/AuthController.java
  web/SocialAuthController.java
quickboot-system/
  oauth/OAuthClientController.java
  oauth/OAuthClientService.java
  oauth/MybatisRegisteredClientRepository.java
  oauth/Oauth2RegisteredClientSeeder.java
quick-ui/
  src/utils/request.js
  src/utils/oauthClientBasic.js
  src/api/login.js
  src/api/system/oauthClient.js
  src/views/login.vue
  src/views/system/oauthClient/index.vue
script/free-dev-ports.ps1
```

---

## 15. 修订记录

| 日期 | 说明 |
|------|------|
| 2026-07-27 | 初版：对齐混淆 Basic、明文 client secret、无 FIRST_PARTY 死判断、验证码扩展步骤 |
