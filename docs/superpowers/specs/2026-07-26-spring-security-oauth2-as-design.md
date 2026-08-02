# QuickBoot 统一认证详细方案

日期：2026-07-26  
状态：详细方案（三能力全做）  
原型：`docs/demo/oauth2-auth-flow-prototype.html`

---

## 0. 一句话 + 三个必须能力

**一句话：** 本系统自建 Authorization Server（AS）作为**唯一发 JWT 的柜台**；管理端用账号密码拿用户 Token；用户也可用 GitHub/Gitee 等登录后拿**同一种用户 Token**；外部合作 App 走授权码拿用户 Token；服务间可选 client_credentials 拿**客户端 Token**。业务 API 只验 JWT。

| # | 能力 | 用户感知 | 技术要点 |
|---|------|----------|----------|
| 1 | 密码登录 | 输入账号密码进系统 | `password` 扩展 grant + 可选 `POST /auth/login` |
| 2 | 社交登录 | 点 GitHub/Gitee | OAuth2 Client + 绑定/建号 + 同一套发牌 |
| 3 | 自建 AS | 外部 App「用我们账号登录」 | `authorization_code` + RegisteredClient 管理 |

**明确不做：** JustAuth、Sa-Token、把 password 开放给任意第三方、多租户多 Issuer。

---

## 1. 名词（先对齐再往下看）

| 名词 | 含义 |
|------|------|
| **AS（Authorization Server）** | 发码、发 token、暴露 JWKS 的组件（Spring Authorization Server） |
| **RS（Resource Server）** | 业务 API；只校验 Bearer JWT |
| **Client（OAuth 客户端）** | 来申请 token 的应用，如 `quick-ui`、`acme-report` |
| **IdP / 社交平台** | GitHub、Gitee 等；此时**本系统是它们的 Client** |
| **用户 Token** | 代表终端用户；`token_kind=user`，`sub=userId` |
| **客户端 Token** | 代表应用自己（无用户）；`token_kind=client`，通常来自 `client_credentials` |
| **password grant** | 用用户名密码直接换 token；OAuth2.1 废弃，但第一方后台常用；SAS 需**扩展实现** |

**两个方向不要反：**

```text
用户 ──用 GitHub 登录──► 本系统     ⇒ 本系统 = OAuth2 Client（能力 2）
外部 App ──用本系统用户登录──► App   ⇒ 本系统 = Authorization Server（能力 3）
```

---

## 2. 总体架构

```text
                         ┌─────────────────────────────────────┐
                         │     quickboot-web（同进程）            │
                         │                                     │
                         │  ┌──────── AS 过滤器链 ────────┐    │
                         │  │ /oauth2/authorize            │    │
                         │  │ /oauth2/token  ← 唯一发牌     │    │
                         │  │ /oauth2/jwks                 │    │
                         │  │ password 扩展 / code /        │    │
                         │  │ refresh / client_credentials │    │
                         │  └─────────────┬───────────────┘    │
                         │                │ JWT                 │
                         │  ┌─────────────▼───────────────┐    │
                         │  │ RS：业务 API + /auth/me       │    │
                         │  │ oauth2ResourceServer.jwt()   │    │
                         │  └─────────────────────────────┘    │
                         │                                     │
                         │  oauth2Login（社交，挂在登录相关链）   │
                         └─────────────────────────────────────┘
           ▲                ▲                    ▲
           │                │                    │
     quick-ui          GitHub/Gitee         外部 App
   password 登录         （IdP）          authorization_code
```

### 2.1 模块

| 模块 | 职责 |
|------|------|
| `quickboot-auth` | Security 多链、AS 配置、password 扩展、TokenCustomizer、JWT 相关 |
| `quickboot-system` | `sys_user`、绑定/建号、`/auth/*` 门面、RegisteredClient 管理 API |
| `quickboot-common` | 异常、PasswordCodec、通用模型 |
| `quickboot-web` | 启动、`application-*.yml`、依赖组装 |

依赖规则：`auth` 依赖「用户查询端口」接口；`system` 实现该接口，避免循环依赖。

### 2.2 Security 过滤器链（建议两条）

1. **AS 链**：`securityMatcher` 匹配 `/oauth2/**`、SAS 端点；配置 Authorization Server +（授权时需要的）登录能力。  
2. **应用链**：其余路径；`oauth2ResourceServer().jwt()`；放行 `/auth/login`、社交回调、健康检查等。

社交 `oauth2Login` 可挂在「登录相关」链上（与 AS 登录页共用用户认证），实现时以「能完成授权码登录页 + 社交回调」为准，允许微调 matcher，但**发牌仍只走 Token 端点/同一 TokenGenerator**。

---

## 3. Token 如何区分（用户 vs 客户端）

都从 AS 发，但 **JWT 内容与允许的 scope 不同**。

### 3.1 规则

| | 用户 Token | 客户端 Token |
|--|------------|--------------|
| 获得方式 | password / authorization_code / 社交完成后发牌 | `client_credentials` |
| `token_kind` | `user` | `client` |
| `sub` | 本地 `userId` | `client_id` |
| `client_id` | 有（如 `quick-ui` / `acme-report`） | 有（与 sub 通常相同） |
| 典型 scope | `openid` `profile` `api.read` | `job.run` `internal.*` |
| 能否调 `/auth/me` | 能 | **不能**（返回 401/403） |

自定义 Claim 通过 `OAuth2TokenCustomizer` 写入，保证 RS 可稳定判断。

### 3.2 RS 侧伪逻辑

```text
if 接口需要登录用户:
  require token_kind == user
  principal = sub (userId)
else if 接口需要机机:
  require token_kind == client
  principal = client_id
else if 两者皆可:
  按 token_kind 分支审计字段
```

### 3.3 默认时效（可配置）

| Token | 建议默认 |
|-------|----------|
| access_token | 2 小时 |
| refresh_token | 7 天（仅用户 Token；客户端按需） |

---

## 4. 能力一：密码登录（详细）

### 4.1 推荐对外形态

前端只调业务门面（便于加验证码、统一 R 响应）：

```http
POST /auth/login
Content-Type: application/json

{ "username": "admin", "password": "******" }
```

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<opaque-or-jwt>",
  "tokenType": "Bearer",
  "expiresIn": 7200,
  "tokenKind": "user"
}
```

门面内部：

1. 校验用户名密码（`UserDetailsService` / 领域服务）  
2. 确认调用方等价于第一方 client `quick-ui`  
3. 调用与 `grant_type=password` **同一套** `OAuth2TokenGenerator` 发牌（或内部构造 Token 请求走扩展 Provider）  
4. 返回 JSON  

也可直接暴露标准：

```http
POST /oauth2/token
grant_type=password&username=...&password=...&client_id=quick-ui&scope=openid profile api.read
```

### 4.2 password 扩展（SAS）

Spring Authorization Server **不内置** password，需按官方扩展 grant：

- `AuthenticationConverter`：解析 form 中的 username/password/client  
- `AuthenticationProvider`：验密 + 验 client 是否允许 password + 调 TokenGenerator  
- 注册到 `tokenEndpoint`  

`RegisteredClient(quick-ui)`：

- 类型：第一方（public 或 confidential，SPA 倾向 public + 仅后端门面持有交互）  
- **允许** `password`、`refresh_token`  
- **不允许**随意注册的外部 client 带 password（管理 API 校验）

### 4.3 刷新

```http
POST /auth/refresh
{ "refreshToken": "..." }
```

或 `POST /oauth2/token` + `grant_type=refresh_token`。

---

## 5. 能力二：社交登录（详细）

### 5.1 配置（首期 yml）

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          gitee:
            client-id: ${GITEE_CLIENT_ID}
            client-secret: ${GITEE_CLIENT_SECRET}
            # provider 细节按 Gitee 文档补全
        provider:
          gitee:
            authorization-uri: ...
            token-uri: ...
            user-info-uri: ...
            user-name-attribute: id
```

首期至少打通 **1 个** IdP（建议 Gitee 或 GitHub）；其余配置化追加。

### 5.2 时序

```text
浏览器                本系统                      Gitee
  |  GET /oauth2/authorization/gitee
  |--------------------->|
  |  302 去 Gitee
  |<---------------------|
  |  用户同意
  |---------------------------------------------->|
  |  回调 /login/oauth2/code/gitee?code=
  |<----------------------------------------------|
  |--------------------->|
  |                 换 userinfo
  |                 查 sys_oauth_user_bind
  |    已绑定 ──────────────────────────────┐
  |    未绑定 → 返回/跳转绑定页               │
  |       用户选：自动建号 or 绑已有           │
  |       写 bind +（可选）建 sys_user        │
  |<──────────────────────────────────────────┘
  |                 调用同一 TokenGenerator 发用户 JWT
  |  落地页带 token 或 JSON
  |<---------------------|
```

### 5.3 绑定表

`sys_oauth_user_bind`：

| 字段 | 说明 |
|------|------|
| id | 主键 |
| user_id | 本地用户 |
| registration_id | 如 `gitee` |
| external_subject | 第三方用户唯一 ID |
| display_name / avatar | 可选快照 |
| created_at | |

唯一索引：`(registration_id, external_subject)`。

### 5.4 未绑定 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/auth/social/pending` | 当前社交会话待绑定信息（需短时登录态/state） |
| POST | `/auth/social/auto-create` | 自动建号并发用户 JWT |
| POST | `/auth/social/bind` | body: username/password，绑定后发用户 JWT |

冲突：若该社交身份已绑定其他 user → 拒绝。

### 5.5 社交成功后如何发 JWT（钉死）

**采用：服务端直接调用与 password 相同的 `OAuth2TokenGenerator` 签发用户 Token**，经 `/auth/social/complete` 或前端落地接口返回。

- 不引入第二套签名密钥  
- 不强制再走一遍浏览器 password  
- Claim 规则与密码登录用户 Token 一致（`token_kind=user`）

---

## 6. 能力三：自建 AS 给外部 App（详细）

### 6.1 注册客户端

管理端 CRUD（数据进 `oauth2_registered_client`）：

| 字段意图 | 说明 |
|----------|------|
| clientId / secret | secret 加密存储 |
| 允许的 grant | 外部：**仅** `authorization_code`、`refresh_token`；可选另建机机 client 用 `client_credentials` |
| redirect_uris | 精确匹配 |
| scopes | 如 `openid profile api.read` |
| require PKCE | 公共客户端强制 |

**禁止**对外部 client 勾选 password。

内置种子数据：

| client_id | 用途 | grants |
|-----------|------|--------|
| `quick-ui` | 管理端 | password, refresh |
| （示例）`demo-third` | 联调外部 | authorization_code, refresh |
| （可选）`job-runner` | 机机 | client_credentials |

### 6.2 授权码时序

```text
外部 App                         本系统 AS                         用户
  |  /oauth2/authorize?...        |                                  |
  |------------------------------>|  未登录 → 登录页（密码/社交）      |
  |                               |<---------------------------------|
  |                               |  Consent（可配置跳过第一方）       |
  |  302 redirect?code&state      |                                  |
  |<------------------------------|                                  |
  |  POST /oauth2/token (code)    |                                  |
  |------------------------------>|                                  |
  |  access_token (用户 JWT)      |                                  |
  |<------------------------------|                                  |
  |  API Bearer                   |                                  |
```

### 6.3 标准端点

| 端点 | 说明 |
|------|------|
| `GET /oauth2/authorize` | 授权 |
| `POST /oauth2/token` | 换 token |
| `GET /oauth2/jwks` | 公钥 |
| `POST /oauth2/revoke` | 可选吊销 |
| OIDC `/.well-known/openid-configuration` | 若启用 OIDC 一并打开 |

---

## 7. 数据模型（DDL 级草案）

### 7.1 `sys_user`（最小列）

- `user_id`（PK，雪花/UUID）  
- `user_name`（唯一）  
- `password`（哈希；社交自动建号可空或随机不可登录密）  
- `nick_name`、`status`、`del_flag`  
- 审计字段  

### 7.2 `sys_oauth_user_bind`

见 §5.3。

### 7.3 SAS JDBC 表

使用 Spring Authorization Server 官方 JDBC schema：

- `oauth2_registered_client`  
- `oauth2_authorization`  
- `oauth2_authorization_consent`  

（随 SAS 版本取对应 schema SQL，纳入 Flyway/Liquibase 或启动初始化。）

---

## 8. 接口清单（汇总）

### 8.1 认证门面

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/auth/login` | 匿名 | 密码登录 → 用户 JWT |
| POST | `/auth/refresh` | 匿名+refresh | 刷新 |
| POST | `/auth/logout` | 用户 JWT | 可选黑名单/删 refresh |
| GET | `/auth/me` | 用户 JWT | 当前用户 |
| GET | `/oauth2/authorization/{regId}` | 匿名 | 社交入口（框架） |
| GET | `/auth/social/pending` | 短时态 | 待绑定信息 |
| POST | `/auth/social/auto-create` | 短时态 | 建号+JWT |
| POST | `/auth/social/bind` | 短时态 | 绑定+JWT |

### 8.2 AS / 管理

| 方法 | 路径 | 说明 |
|------|------|------|
| * | `/oauth2/**` | SAS 标准 |
| CRUD | `/system/oauth-clients/**` | 客户端管理（需管理权限；首期可先放行 admin） |

### 8.3 统一响应

与现有工程约定对齐（若有 `R<T>` 则门面包一层；`/oauth2/token` 保持 OAuth 标准 JSON，不强制包 R）。

---

## 9. 安全设计

1. password **仅** `quick-ui`（及显式标记为 first-party 的 client）。  
2. 外部 client 创建/更新时服务端校验 grant 列表。  
3. redirect_uri 精确匹配；防开放重定向。  
4. 登录失败限流；`/auth/login` 可接验证码。  
5. client_secret、社交 secret：jasypt 或等价加密。  
6. CORS 白名单；禁止把长期 token 放 URL 查询串（落地页用 fragment / 后端 set cookie / postMessage 择一，实现期定，默认：**前端用后端 JSON 接口收 token，避免 query**）。  
7. 社交 state / PKCE 走框架默认。  

### 错误映射

| 场景 | HTTP / OAuth error |
|------|---------------------|
| 密码错误 | 401 + 业务码 或 `invalid_grant` |
| 外部 client 用 password | `unauthorized_client` / `unsupported_grant_type` |
| 社交未完成绑定 | 403 + `SOCIAL_BIND_REQUIRED` |
| 绑定冲突 | 409 |
| JWT 坏/过期 | 401 |
| client token 调 /auth/me | 403 |

---

## 10. 分期落地（能力都要，但分批交付）

三能力都在目标内，**实现顺序**如下，每期可验收，避免一次啃爆。

### P0 — AS 骨架 + 密码用户 Token（能力 1）

- 引入 `spring-authorization-server`、RS JWT  
- JDBC 客户端表 + 种子 `quick-ui`  
- password 扩展 + `/auth/login`、`/auth/me`、`/auth/refresh`  
- `token_kind=user` Customizer  

**验收：** 密码登录拿 JWT 调通 `/auth/me`。

### P1 — 社交（能力 2）

- 一个 IdP yml  
- 绑定表 + 自动建号/绑定 API  
- 社交完成后同一 TokenGenerator 发用户 JWT  

**验收：** 新社交用户建号、老用户绑定、二次直登。

### P2 — 外部 App（能力 3）+ 可选机机

- 客户端 CRUD  
- 授权码联调页/文档  
- 外部 client 拒绝 password  
- （可选）`job-runner` + `token_kind=client` + 样例内部接口  

**验收：** 外部 code 换用户 Token；password 被拒；可选机机 Token 不能进 `/auth/me`。

---

## 11. 前端（quick-ui）改造要点

1. 登录页：`POST /auth/login`，存 access/refresh。  
2. 请求拦截器：`Authorization: Bearer`。  
3. 社交：跳转 `/oauth2/authorization/{id}`，落地页处理绑定或收 token。  
4. 去掉对 Sa-Token / 旧 `/oauth2/client/**` 的依赖路径。  
5. 外部 App 不在管理端登录页完成，另附对接说明即可。  

静态说明书：`docs/demo/oauth2-auth-flow-prototype.html`（可继续当培训/评审材料）。

---

## 12. 与现状

- 当前仓库几乎无登录实现；`bak/` 为 Sa-Token 方案，**逻辑可参考绑定表思路，代码不迁栈**。  
- 基础设施已有 H2 + Redis（Luban）等，会话/限流可按需用 Redis；JWT 本身无状态，refresh 存 JDBC/Redis 择一（P0 用 SAS JDBC 即可）。  

---

## 13. 总验收清单

1. 密码登录 → 用户 JWT → `/auth/me` 成功。  
2. 社交：建号 / 绑定 / 再登录 三条路径通过。  
3. 外部 App：authorization_code 成功；对同一 client 使用 password 失败。  
4. （若做机机）client_credentials Token 无法访问用户接口。  
5. 无 token / 坏 token 访问受保护 API → 401。  
6. 依赖中无 Sa-Token、无 JustAuth。  
7. 所有用户 Token 与客户端 Token 带可区分的 `token_kind`（或等价约定并文档化）。  

---

## 14. 已钉死的实现默认（原「开放细节」）

| 项 | 默认 |
|----|------|
| 社交后发牌 | 同 TokenGenerator，不二次 password 浏览器流 |
| access / refresh | 2h / 7d |
| 第一方 Consent | 可跳过 |
| 外部 Consent | 默认需要（可配置） |
| Token 区分 | 强制 `token_kind` + sub 语义 |
| 管理端收 token | JSON 门面，避免 query 传长期 token |

---

## 15. 下一步

1. 审阅本详细方案；若无异议，再出 **分 P0/P1/P2 的实现计划**（`docs/superpowers/plans/...`）。  
2. 实现严格按 P0 → P1 → P2，每期合并可运行。  
