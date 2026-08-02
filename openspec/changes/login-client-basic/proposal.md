## Why

`POST /auth/login` 发牌写死第一方 `quick-ui`，客户端身份未由调用方证明；管理端需要在无用户 JWT 时校验 client，且与用户名密码校验分离。本期按已确认设计采用混淆 + HTTP Basic，去掉硬编码发牌客户端。

## What Changes

- **BREAKING**：无用户 Bearer JWT 的请求须带 `Authorization: Basic`（混淆后的 clientId:clientSecret）；缺少或错误 →「客户端无效」
- 新增 Security Filter：无合法用户 JWT 时验 Basic → 解混淆 → RegisteredClient + secret matches
- `/auth/login` body 仍仅 username/password；发牌使用 Filter 放入的 RegisteredClient（须含 password grant）
- **移除** `AuthTokenService` 中 `findByClientId(FIRST_PARTY_CLIENT_ID)` 硬编码发牌
- 前端：env 凭证 + 混淆工具；无 token 时 axios 自动注入 Basic

## Capabilities

### New Capabilities

- `login-client-basic`: 无用户 JWT 时的 Basic 客户端校验、解混淆约定、登录发牌绑定已校验 client、前端混淆与拦截器注入

### Modified Capabilities

- （无主库 `openspec/specs/` 条目；相对既有 AS/登录行为以本 capability 承接）

## Impact

- 后端：`quickboot-auth` Filter / Security 配置、`AuthTokenService`、`AuthController`；依赖 `RegisteredClientRepository`、`PasswordEncoder`
- 前端：`quick-ui` request 拦截器、`login.js`、env 与混淆工具
- 文档：`docs/superpowers/specs/2026-07-27-login-client-basic-design.md`
- 不做：BFF、PKCE、HMAC、client ticket
