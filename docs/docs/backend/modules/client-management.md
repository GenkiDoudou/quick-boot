# 客户端管理（OAuth Client + HMAC）

本项目的「客户端」指 **`sys_oauth_client`** 记录：同时承担 **OAuth2 授权服务器客户端** 与 **API 调用方（HMAC 签名）** 身份，不单独维护 access_token 用于管理端登录。

## 管理接口

| 项 | 值 |
|----|-----|
| Controller | `SysOauthClientController` |
| 路径 | `/system/oauthClient` |
| 前端 | `views/system/oauthClient/index.vue` |

| 接口 | 说明 |
|------|------|
| GET `/list` | 分页列表 |
| GET `/{clientId}` | 详情 |
| POST `/{clientId}/revealSecret` | 揭示密钥（一次性展示，需权限） |
| POST `/create`、`/update`、`/remove` | CRUD |

## 核心字段

| 字段 | 说明 |
|------|------|
| `client_id` | 调用方标识，对应请求头 `X-Client-Id` |
| `client_secret` | SM4 加密存储；签名用明文 |
| `redirect_uris` | OAuth 回调，精确匹配 |
| `grant_types` | 允许的授权模式 |
| `scopes` | 授权页 scope（openid/profile 等） |
| `api_path_patterns` | Open API / 签名请求的 **Ant 路径** 白名单（每行一条） |
| `sign_verify` | `1` 强制 HMAC；`0` 跳过签名校验（Open API 仍校验 token+路径） |

## 首方 quick-ui

Flyway `V28__oauth2_quick_ui_client.sql` 预置 `client_id=quick-ui`。前端构建注入：

- `VITE_APP_CLIENT_ID`
- `VITE_APP_CLIENT_SIGN_KEY`

详见 [OAuth2 集成](./oauth2)。

## 签名校验流程

1. 读取 `X-Client-Id`、Timestamp、Nonce、Signature  
2. 校验时间窗（默认 300s）与 nonce 防重放（`clientSignNonce#300` 缓存）  
3. HMAC-SHA256 与 `ClientSignService.buildCanonical` 一致  
4. 校验 `api_path_patterns` 是否允许当前 servlet path  

失败：业务码 **30002**，HTTP **401**。

## 相关文档

- [OAuth2 集成](./oauth2)
- [外部 IdP 提供方](./oauth2#联邦登录-client)（`sys_oauth_provider`）
