# 客户端管理（设计）

## 目标

统一 **OAuth2 客户端**与 **API 调用方**身份：`sys_oauth_client` 一条记录同时服务：

- 授权服务器（AS）client 配置
- Client HMAC 全站 API 签名校验
- Open API 路径授权（Ant 风格）

## 数据模型要点

- `client_id` / `client_secret`（SM4 存储）
- `redirect_uris`、`grant_types`、`scopes`
- `api_path_patterns`：每行一条 Ant 路径
- `sign_verify`：是否强制 HMAC

## 交互

- 管理端：`oauthClient/index.vue` 支持密钥一次性揭示
- 首方 SPA：构建时注入 `VITE_APP_CLIENT_*`

实现见 [客户端管理（后端手册）](../backend/modules/client-management)、[OAuth2 集成](../backend/modules/oauth2)。
