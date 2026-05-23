## Why

`quickboot` + `quick-ui` 目前仅支持 Sa-Token 账号密码登录，无法满足第三方系统按标准 OAuth2 只读获取用户基础信息，也无法通过企业 IdP（企业微信、Keycloak 等）联邦登录管理端。在保留现有 `StpUtil` 会话与 RBAC 的前提下，需要基于 Sa-Token OAuth2 模块同时承担**授权服务器（AS）**与**OAuth2 客户端（Client）**双角色，并与内部管理 API 硬性隔离。

## What Changes

- 升级父 POM `sa-token.version` 至 **1.44.0 或 1.45.0**，新增 `sa-token-oauth2`、OAuth2 数据 **Redis 持久化**；P0 全量回归现有登录/权限/在线用户/操作日志。
- 实现 OAuth2 AS：`/oauth2/*` 标准端点、`SaOAuth2DataLoader`、与 `AuthLoginService` / 验证码 / 登录锁桥接；四种 Grant Type 可配置，**生产默认关闭** password / implicit。
- 实现 Scope `openid` / `profile` 及 **`/open-api/v1/**`** 只读开放 API；OAuth2 `access_token` **不得**访问 `/system/*`、`/monitor/*`。
- Flyway：`sys_oauth_client`、`sys_oauth_user_openid`、`sys_oauth_provider`、`sys_oauth_user_bind`、可选 `sys_oauth_approve`；菜单与权限种子。
- 实现 OAuth2 Client：外部 IdP 授权/回调、用户绑定、登录页可配置第三方入口。
- `quick-ui`：OAuth 客户端管理、外部 IdP 管理、授权确认页、登录页扩展；对接文档与 prod 配置清单。
- **非 BREAKING**：保留现有 `/login` 账号密码流程；OAuth2 为增量能力。Sa-Token 大版本升级需 P0 回归验证，属行为风险而非对外 API 契约删除。

## Capabilities

### New Capabilities

- `oauth2-foundation`：Sa-Token 升级、Redis 整合、三轨认证隔离、全局配置项、P0 回归验收基线。
- `oauth2-authorization-server`：AS 端点、Grant Type 策略、DataLoader、登录桥接、授权确认流程、审计。
- `oauth2-open-api`：`/open-api/v1/userinfo` 及 scope 裁剪；token 与内部 Admin-Token 路径隔离。
- `oauth2-client-federation`：外部 IdP 注册、authorize/callback、用户绑定策略、登录页联邦登录。
- `oauth2-management`：OAuth 客户端 / IdP 管理端 CRUD、Vue 页面与菜单权限。

### Modified Capabilities

- （无）`openspec/specs/` 下既有 capability 规范正文不变；实现复用 `common-field-desensitization`、`common-tracing` 等，不在本变更中重定义其行为。

## Impact

- **后端**：`quickboot/pom.xml`、`quickboot-common`（Sa-Token/Redis）、`quickboot-web`（`web/auth/oauth2/`、`system/oauthclient`、`system/oauthprovider`、`open`）、Flyway、`SaTokenWebMvcConfig` 白名单。
- **前端**：`quick-ui` 登录页、`views/oauth/authorize.vue`、`views/system/oauthClient`、`views/system/oauthProvider`、对应 API 模块。
- **依赖**：`sa-token-oauth2`、`sa-token-redis-template`（或项目既有 Redis 方案）；**必须**启用 Redis 存储 OAuth2 code/token（多实例）。
- **真源设计**：`docs/superpowers/specs/2026-05-23-oauth2-integration-design.md`。
