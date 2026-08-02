## Why

客户端管理需要在校验管理员密码后查看 Client ID / Secret，但当前 `client_secret` 以 BCrypt 存储无法回显。脚手架阶段改为明文存储并增加 reveal 接口与表单字段说明，便于联调外部 OAuth 客户端。

## What Changes

- **BREAKING（相对现有运行态）**：`oauth2_registered_client.client_secret` 改为明文写入；已存在的 BCrypt 种子密钥需迁移为约定明文
- `PasswordEncoder` 双模式：`encode` 仍 BCrypt（用户密码）；`matches` 对 BCrypt 形态走哈希、否则明文 equals（客户端密钥）
- 新增 `POST /system/oauth-clients/{clientId}/reveal-secret`（Body 校验当前登录用户密码后返回明文 secret）
- 列表/普通详情仍不返回 `clientSecret`
- 前端「查看」流程（密码确认 → 展示可复制 ID/Secret）及表单字段小眼睛说明

## Capabilities

### New Capabilities

- `oauth-client-secret`: 客户端密钥明文存储、双模式校验、密码二次确认后 reveal、管理端查看与表单字段提示

### Modified Capabilities

- （无主库 `openspec/specs/` 条目；行为变更相对归档变更 `add-oauth-client-mp-menu` 的 `oauth-client-admin`，本变更以新 capability 承接，避免未同步主库时的空 delta）

## Impact

- 后端：`AuthBeansConfiguration`、`OAuthClientService`、`Oauth2RegisteredClientSeeder`、`OAuthClientController`；依赖 `AuthUserLookup` / JWT 当前用户
- 前端：`quick-ui` `api/system/oauthClient.js`、`views/system/oauthClient/index.vue`
- 安全：库内明文 secret；reveal 依赖 Bearer 用户 Token + 密码二次确认；用户密码哈希策略不变
- 文档：对齐 `docs/superpowers/specs/2026-07-26-oauth-client-secret-plaintext-design.md`
