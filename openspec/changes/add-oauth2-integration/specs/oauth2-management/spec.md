## ADDED Requirements

### Requirement: OAuth 客户端管理 CRUD

系统 MUST 提供 `sys_oauth_client` 的管理 REST（包路径 `system/oauthclient`），权限至少包含：`system:oauthClient:list|add|edit|remove`（与现有系统模块命名风格一致）。`client_secret` 入库 MUST 加密；列表/日志 MUST 脱敏。删除使用 **POST** 而非 DELETE。

#### Scenario: 管理员创建 confidential client

- **WHEN** 具备 `system:oauthClient:add` 的用户提交合法 client
- **THEN** 库中 MUST 存在记录且 secret 不以明文存储

#### Scenario: 无权限拒绝

- **WHEN** 无 `system:oauthClient:list` 调用列表
- **THEN** MUST 返回权限不足

---

### Requirement: 外部 IdP 管理 CRUD

系统 MUST 提供 `sys_oauth_provider` 管理 REST，权限 `system:oauthProvider:list|add|edit|remove`。

#### Scenario: 配置 Keycloak 沙箱

- **WHEN** 管理员录入 authorize/token/userinfo URL 与凭证
- **THEN** 联邦登录流程 MUST 可使用该 `provider_code`

---

### Requirement: 管理端 Vue 页面

`quick-ui` MUST 提供：

- `views/system/oauthClient/index.vue`（参照 `system/config` 列表模板）
- `views/system/oauthProvider/index.vue`
- `views/oauth/authorize.vue`（授权确认，遵循 `DESIGN.md`）
- `views/login.vue` 扩展：可配置第三方登录按钮列表

API 模块：`api/system/oauthClient.js`、`api/system/oauthProvider.js`、`api/oauth/authorize.js`。

#### Scenario: 浏览器完整 AS 流程

- **WHEN** 第三方应用发起 authorize 且管理员在 Vue 确认页同意 scope
- **THEN** 第三方 MUST 获得 code 并可换 token

#### Scenario: 登录页显示 IdP 按钮

- **WHEN** 后端返回已启用 provider 列表
- **THEN** `login.vue` MUST 渲染对应入口

---

### Requirement: 菜单与 Flyway 种子

Flyway MUST 插入 OAuth 客户端、外部 IdP 相关菜单与 `sys_role_menu`；`menu_id` MUST 与仓库现有迁移错开。

#### Scenario: 超级管理员可见菜单

- **WHEN** Flyway 执行完成且角色已授权
- **THEN** 侧边栏 MUST 出现 OAuth 管理入口
