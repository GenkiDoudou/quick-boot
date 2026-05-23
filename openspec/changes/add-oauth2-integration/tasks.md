## 1. P0 — Sa-Token 升级与基础（oauth2-foundation）

- [x] 1.1 父 POM `sa-token.version` 升至 1.44.0 或 1.45.0；`quickboot-common` 引入 `sa-token-oauth2`、Redis 整合依赖
- [x] 1.2 配置 `sa-token` OAuth2 + Redis（`application.yml` / prod 加固）；实现 prod Redis 启动校验或文档约定
- [x] 1.3 更新 `SaTokenWebMvcConfig`：预留 `/oauth2/**`、Client 回调白名单占位
- [x] 1.4 P0 回归：`/login`、`/logout`、`/getInfo`、`/getRouters`、`@SaCheckPermission` 抽样、在线用户、`GlobalExceptionHandler`、操作日志异步
- [x] 1.5 新增 `qc.oauth2.*` 配置属性类（server/client grant 开关）

## 2. P1 — 数据库与 AS 骨架（oauth2-authorization-server）

- [x] 2.1 Flyway：`sys_oauth_client`、`sys_oauth_user_openid`、可选 `sys_oauth_approve`；版本号取当前 `db/migration` 最大 +1
- [x] 2.2 Entity/Mapper：`SysOauthClient`、`SysOauthUserOpenid`（及 Approve 若启用）
- [x] 2.3 实现 `SaOAuth2DataLoaderImpl`（`getClientModel`、`getOpenid`）
- [x] 2.4 实现 `SaOAuth2ServerConfigurer` + `SaOAuth2ServerController`（`dister()` 挂载 `/oauth2/*`）
- [x] 2.5 Postman/集成测试：authorization_code 换 token 跑通

## 3. P2 — Scope、开放 API、Grant 开关（oauth2-open-api + AS）

- [x] 3.1 实现 `OpenApiUserinfoController`：`GET /open-api/v1/userinfo`，`@SaCheckScope`，按 scope 裁剪
- [x] 3.2 路径隔离：OAuth2 token 访问 `/system/*` 返回 403；Admin-Token 访问 open-api 拒绝
- [x] 3.3 配置 `qc.oauth2.server.grant.password-enabled`、`implicit-enabled`；per-client `grant_types` 读取
- [x] 3.4 dev 环境验证四种 grant；prod profile 断言 password/implicit 默认关闭
- [x] 3.5 实现 `OAuth2LoginBridgeService` + Strategy：验证码、锁定、`LoginDataScopeService`、登录日志 `OAuth2-AS`

## 4. P3 — 管理端 CRUD 与授权 UI（oauth2-management）

- [x] 4.1 后端 `system/oauthclient`、`system/oauthprovider`：Controller/Service/BO/VO，POST 删除，Jakarta Validation，OpenAPI，`@SaCheckPermission`
- [x] 4.2 Flyway：OAuth 客户端/IdP 菜单与 `sys_role_menu`；`menu_id` 与现有迁移错开
- [x] 4.3 `quick-ui`：`views/system/oauthClient/index.vue`、`views/system/oauthProvider/index.vue`（参照 `system/config`）
- [x] 4.4 `views/oauth/authorize.vue` + `api/oauth/authorize.js`；授权确认流程浏览器验收
- [x] 4.5 `login.vue`：第三方按钮列表 API 与展示

## 5. P4 — OAuth2 Client 联邦登录（oauth2-client-federation）

- [x] 5.1 Flyway：`sys_oauth_provider`、`sys_oauth_user_bind`
- [x] 5.2 `OAuth2ClientController`：`/oauth2/client/authorize/{provider}`、`/oauth2/client/callback/{provider}`
- [x] 5.3 `OAuth2ClientService` + `provider/` 策略（至少 1 个：Keycloak 或 GitHub 沙箱）
- [x] 5.4 绑定策略：已绑定登录、未绑定拒绝（`auto_register=false` 默认）；回调后 Admin-Token 与 `/login` 一致
- [x] 5.5 端到端：外部 IdP 登录进入管理端首页

## 6. P5 — 文档、审计与验证

- [x] 6.1 可选 `sys_oauth_audit` 或扩展现有 `sys_logininfor`：授权、换 token、撤销
- [x] 6.2 编写第三方对接文档：端点、scope、Grant 矩阵、redirect_uri、与 `X-Client-Id` 区别
- [x] 6.3 prod 配置清单：`qc.oauth2.*`、HTTPS、CORS、secret 轮换说明
- [x] 6.4 `mvn -pl quickboot-web -am test`；`quick-ui` `pnpm build:prod`
- [x] 6.5 更新设计文档状态为「已实现」；`openspec verify` / archive 准备
