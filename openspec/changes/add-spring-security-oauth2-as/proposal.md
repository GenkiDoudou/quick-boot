## Why

当前 `quickboot` 活代码几乎没有可用登录（仅占位 `LoginController`），无法支撑管理端账号密码登录、社交登录，也无法作为 OAuth2 授权服务器向外部应用发牌。需要在 Spring Boot 4 + Spring Security 栈上重建统一认证，并以 Authorization Server 作为唯一 JWT 签发点，替代已放弃的 Sa-Token / JustAuth 路线。

## What Changes

- 新增 `quickboot-auth` 模块：Spring Authorization Server、双 Security 过滤器链、password 扩展 grant、JWT Customizer、Resource Server 验签
- 管理端密码登录：`POST /auth/login` 门面 + 内部 password 扩展发用户 JWT；支持 refresh
- 社交登录：Spring Security OAuth2 Client；`sys_oauth_user_bind`；首次未绑定支持自动建号或绑定已有账号；发牌与密码登录共用 TokenGenerator
- 自建 AS：标准 `/oauth2/authorize`、`/oauth2/token`、`/oauth2/jwks`；RegisteredClient 管理；外部 client 仅 authorization_code（禁止 password）
- Token 区分：用户 Token（`token_kind=user`）与客户端 Token（`token_kind=client`，client_credentials）
- 数据：`sys_user`、`sys_oauth_user_bind`、SAS JDBC 表；种子 client `quick-ui`
- 前端 `quick-ui` 登录对接改为新认证 API（后续任务）
- **不做：** JustAuth、Sa-Token、多租户多 Issuer、向任意第三方开放 password grant

## Capabilities

### New Capabilities

- `auth-password-login`: 管理端账号密码登录、刷新、当前用户、用户 JWT 门面
- `auth-social-login`: 社交 OAuth2 Client、绑定/建号、社交完成后发用户 JWT
- `oauth2-authorization-server`: 自建 AS、授权码、RegisteredClient、JWKS、可选 client_credentials
- `api-jwt-resource-server`: 业务 API Bearer JWT 校验与 token_kind 约束

### Modified Capabilities

- （无）当前 `openspec/specs/` 无既有能力需改需求；本变更为全新认证基线

## Impact

- 模块：新建 `quickboot-auth`；扩展 `quickboot-system` / `quickboot-web` / `quickboot-common`
- 依赖：`spring-authorization-server`、`spring-boot-starter-oauth2-resource-server`、`spring-boot-starter-oauth2-client`、Spring Security（Boot 4 对应 starter）
- API：新增 `/auth/**`、`/oauth2/**`、`/system/oauth-clients/**`；受保护接口需 Bearer
- 数据：用户/绑定/SAS JDBC schema（Flyway 或启动初始化）
- 参考设计：`docs/superpowers/specs/2026-07-26-spring-security-oauth2-as-design.md`
- 原型：`docs/demo/oauth2-auth-flow-prototype.html`
- 与 `bak/` Sa-Token OAuth 实现解耦，不迁栈
