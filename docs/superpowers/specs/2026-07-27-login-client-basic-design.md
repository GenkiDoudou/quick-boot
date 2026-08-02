# 登录客户端校验（无 JWT 时 Basic + 混淆）

日期：2026-07-27  
状态：已确认待实施  
关联：`2026-07-26-spring-security-oauth2-as-design.md`、`2026-07-26-oauth-client-secret-plaintext-design.md`

## 1. 目标

1. 去掉 `AuthTokenService` 发牌时写死 `quick-ui`：登录使用请求中校验通过的 `RegisteredClient`。  
2. **客户端校验与用户名密码校验分离**：client 走请求头，用户凭证走 `/auth/login` body。  
3. **无有效用户 JWT 时**校验 client；有合法 Bearer 用户 JWT 时不再要求 client。  
4. 传输采用 **S2**：前端混淆后的 id/secret，以 `Authorization: Basic` 传递（须 HTTPS）。

## 2. 已确认决策

| 项 | 选择 |
|----|------|
| 传输方案 | S2：混淆 + `Authorization: Basic base64(混淆id:混淆secret)` |
| 校验时机 | 无 JWT / 非 Bearer → 验 client；合法用户 Bearer → 跳过 |
| 登录 body | 仅 `username`、`password` |
| 发牌 client | 本次 Basic 校验通过的 RegisteredClient（须含 password grant） |
| 前端防泄露强度 | 打包/运行时混淆（防翻源码，不防抓包） |
| 不做 | BFF、PKCE、HMAC、client ticket、全站每次请求带 secret |

## 3. 后端

### 3.1 Filter

在 Security 过滤器链中增加客户端校验 Filter（位于 JWT Resource Server 认证之后或与之协调）：

1. 路径属于 exclude → 放行（`/actuator/**`、`/h2-console/**`、静态资源等固定小名单）。  
2. 若 `Authorization` 为 `Bearer` 且已解析为合法**用户** JWT（`token_kind=user` 或等价）→ 放行，不验 Basic。  
3. 否则：要求 `Authorization: Basic …`；解码得到两段字符串 → **解混淆** → `findByClientId` → `PasswordEncoder.matches(plainSecret, stored)`。  
4. 失败：401/400，文案统一「客户端无效」（避免枚举）。  
5. 成功：将 `RegisteredClient`（或 clientId）放入 request attribute，供 `/auth/login` 发牌使用。

### 3.2 `/auth/login`

- Body：`{ "username", "password" }`（必填）。  
- 从 request attribute 取已校验 client；若无（未走 Filter）→ 拒绝。  
- 校验该 client 的 authorization grant types **包含 password**；否则拒绝。  
- 再校验用户名密码；成功则 `issueUserToken(account, registeredClient)`，**禁止**再写死 `FIRST_PARTY_CLIENT_ID`。

### 3.3 其它无 JWT 入口

如 `/auth/refresh`、社交 pending 等：同样依赖 Filter 的 Basic 校验；refresh 发牌优先使用 Authorization 记录中的 client，若设计需与 Basic client 一致可在实现时校验二者相同。

### 3.4 解混淆

与前端约定同一算法（如固定盐的 XOR 后再 UTF-8；或双端约定的可逆变换）。**不是**安全加密，仅提高源码检索成本。

## 4. 前端

- 环境变量：`VITE_OAUTH_CLIENT_ID`、`VITE_OAUTH_CLIENT_SECRET`（本地建议 `.env.local`，勿把生产 secret 提交公开仓库）。  
- 工具函数：明文 → 混淆串；组装 `Basic base64(obfId:obfSecret)`。  
- axios / request 拦截器：当请求**不**附带用户 Bearer token 时，自动设置 `Authorization: Basic …`。  
- `login()`：body 仅用户名密码；依赖拦截器注入 Basic。

## 5. 验收

1. 无 Basic、无 JWT 调受保护或需 client 的接口 → 客户端无效。  
2. 错误 secret → 客户端无效；正确 Basic + 正确用户 → `/auth/login` 发牌成功，JWT 关联该 client。  
3. 已带用户 Bearer 的业务请求 → 可不带 Basic 仍成功。  
4. 源码/dist 中不易直接搜到明文 `quick-ui-secret`（混淆后存在）。  
5. 发牌路径无 `findByClientId("quick-ui")` 硬编码。

## 6. 非目标 / 后续

- BFF、授权码+PKCE、HMAC 签名、短期 client ticket。  
- 将混淆升级为真正的传输加密（S2+3）或 secret 不出站（S4）。
