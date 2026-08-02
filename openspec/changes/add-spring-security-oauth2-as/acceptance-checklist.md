# 验收清单（对照 delta specs）

## auth-password-login
- [x] `POST /auth/login` 发用户 JWT（`token_kind=user`）
- [x] `POST /auth/refresh` 刷新
- [x] `GET /auth/me` 需用户 Token；client Token 拒绝

## auth-social-login
- [x] `GET /oauth2/authorization/{registrationId}` 发起（Gitee 可配）
- [x] 未绑定：ticket → pending / auto-create / bind
- [x] 已绑定：ticket → complete 发用户 JWT
- [x] 冲突绑定拒绝
- [x] 与密码登录共用 TokenGenerator（`AuthTokenService.issueUserToken`）

## oauth2-authorization-server
- [x] `/oauth2/token`、`/oauth2/authorize`、`/oauth2/jwks`
- [x] 种子 `quick-ui`（password）；外部禁 password
- [x] `demo-app` authorization_code + consent；第一方可跳过 consent
- [x] `job-runner` client_credentials → `token_kind=client`

## api-jwt-resource-server
- [x] Bearer JWT 保护业务 API
- [x] 匿名放行登录/社交相关路径
- [x] 用户/client Token 区分
