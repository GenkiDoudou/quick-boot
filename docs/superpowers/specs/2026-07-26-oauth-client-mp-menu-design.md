# OAuth2 客户端管理（MyBatis-Plus + 菜单）设计

日期：2026-07-26  
状态：已确认（用户同意草案）  
关联：`docs/superpowers/specs/2026-07-26-spring-security-oauth2-as-design.md`、`openspec/changes/add-spring-security-oauth2-as`

## 1. 目标

在脚手架中提供可登录后使用的 **「客户端管理」** 菜单页，对 Spring Authorization Server 注册客户端做 CRUD；持久层统一为 **MyBatis-Plus**，且仍以 SAS 标准表 `oauth2_registered_client` 为**唯一数据源**。

## 2. 已确认决策

| 项 | 决策 |
|----|------|
| 数据源 | 仅 `oauth2_registered_client`（方案 A） |
| 管理 API | 保留 REST `/system/oauth-clients`；前端改对接（方案 B） |
| 菜单 | `/getRouters` 写死「客户端管理」（方案 A） |
| 不做 | `sys_menu` 表、revealSecret、client 签名校验、独立业务表双写 |

## 3. 架构

```text
quick-ui 菜单「客户端管理」
    → GET/POST/PUT/DELETE /system/oauth-clients
        → OAuthClientService（校验 + 编解码 RegisteredClient 字段）
            → Oauth2RegisteredClientMapper (MyBatis-Plus)
                → 表 oauth2_registered_client
AS 运行时
    → MybatisRegisteredClientRepository implements RegisteredClientRepository
        → 同一 Mapper / 同一表
```

- 启动种子客户端（`quick-ui` / `demo-app` / `job-runner`）改为经 MP Repository `save`，避免与 Jdbc 仓库双轨。
- 删除现有基于 `JdbcTemplate` 裸 SQL 的 `OAuthClientQuerySupport`（或降为不再使用）。

## 4. 数据层

### 4.1 Entity

`Oauth2RegisteredClient` 映射列与现有 DDL 一致（见 `db/oauth2-registered-client-schema.sql`）：

- `id`, `clientId`, `clientIdIssuedAt`, `clientSecret`, `clientSecretExpiresAt`
- `clientName`, `clientAuthenticationMethods`, `authorizationGrantTypes`
- `redirectUris`, `postLogoutRedirectUris`, `scopes`
- `clientSettings`, `tokenSettings`（JSON 字符串，与 SAS JDBC 编解码兼容）

编解码策略：复用或对齐 `JdbcRegisteredClientRepository` 对 settings/集合字段的序列化格式（逗号分隔 grant/method/uri/scope；settings 为 SAS 默认 JSON），保证与已有种子数据可互通。

### 4.2 Repository 适配

`MybatisRegisteredClientRepository`：

- `findById` / `findByClientId` / `save`（insert or update by id）
- 管理扩展：`listAll` / `listPage` / `deleteById` / `deleteByClientId`

Bean 替换：`AuthorizationServerConfig.registeredClientRepository` 返回该实现，不再 `new JdbcRegisteredClientRepository`。

## 5. API 与规则

路径前缀：`/system/oauth-clients`（需 Bearer 用户 Token）。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 列表；支持可选 `pageNum`/`pageSize`（缺省返回全量或默认分页） |
| GET | `/{clientId}` | 详情（不含 secret） |
| POST | `/` | 创建（必填 clientSecret） |
| PUT | `/{clientId}` | 更新（secret 空则保留原值） |
| DELETE | `/{clientId}` | 删除 |
| POST | `/{clientId}/reveal-secret` | 校验当前用户密码后返回明文 secret |

业务规则（保持）：

- 非 `quick-ui` 禁止 `password` grant
- 禁止删除 `quick-ui`
- 列表/详情不返回 `clientSecret`（需 reveal）
- 第一方默认 `requireAuthorizationConsent=false`
- 客户端密钥明文存储；用户密码仍 BCrypt

响应字段（View）：`id`, `clientId`, `grantTypes`, `redirectUris`, `scopes`, `requireAuthorizationConsent`；分页时包装 `records` + `total`（若采用分页）。

## 6. 前端

- 更新 `quick-ui/src/api/system/oauthClient.js` → 调用 `/system/oauth-clients*`。
- 提供/精简 `views/system/oauthClient/index.vue`：围绕上述 View 字段做列表与表单（grantTypes、redirectUris、scopes、consent）；**去掉**历史专有字段（`signVerify`、旧 status 语义等，除非后续从 SAS 扩展）。
- 权限指令：脚手架阶段 `*:*:*` 已放开，按钮 permi 可保留字符串但不强依赖后端权限码。

## 7. 菜单

在 `ScaffoldCompatController#getRouters`（或等价）返回若依形态树，至少包含：

- 目录：系统管理（`Layout`）
- 菜单：客户端管理  
  - `path`：如 `oauth-client`  
  - `component`：`system/oauthClient/index`  
  - `meta.title`：客户端管理  

登录后侧栏可见并进入页面。

## 8. 验收标准

1. 重启后种子三客户端仍可通过 `GET /system/oauth-clients` 查到。  
2. 新建外部 client（仅 authorization_code）成功；配置 password 被拒绝。  
3. 删除 `demo-app` 成功；删除 `quick-ui` 被拒绝。  
4. AS `findByClientId("quick-ui")` 仍可用于 `/auth/login` 发牌。  
5. 登录后侧栏出现「客户端管理」，页面列表与增删改可用。

## 9. 非目标 / 后续

- AES 等可逆加密、reveal 审计日志（明文存储与密码二次确认见 `2026-07-26-oauth-client-secret-plaintext-design.md`）
- 客户端请求签名校验
- `sys_menu` 动态菜单持久化
- 将本变更单独拆 OpenSpec change（可与现有 AS change 一并实现）

## 10. 实现顺序建议

1. Entity + Mapper + MybatisRegisteredClientRepository + 替换 AS Bean / 种子  
2. Service 重构管理 API（分页可选）  
3. `/getRouters` 写死菜单  
4. 前端 API + 页面精简对接  
5. 冒烟验收上述标准  
