# 后端关键约定

本节只列日常开发必知约定；完整规范见 `code_formater.md`。

## 统一响应

- 接口返回统一包装类型 **`R<T>`**（成功/失败码与消息字段遵循现网约定）。
- 业务异常走统一异常处理，避免 Controller 直接抛裸 `RuntimeException` 给前端未约定结构。

## 认证与权限

- 管理端密码登录：`POST /auth/login`；无用户 JWT 时需带 Client Basic（混淆后的 clientId:clientSecret）。
- 菜单与按钮权限由后端下发；前端配合 `v-hasPermi` / `v-hasRole`。
- OAuth2 AS 端点：`/oauth2/authorize`、`/oauth2/token`、`/oauth2/jwks` 等。
- Host 防火墙：请求 `Host` 须在允许列表，否则可能返回业务码（见 [FAQ](/docs/guide/faq)）。

## OpenAPI

- 使用 SpringDoc；Controller / DTO 应补充必要注解，便于生成接口文档。
- 新增对外 API 时同步考虑匿名路径与权限注解，避免误开放。

## 配置

- 敏感配置优先环境变量 / 机上 `.env.properties`，**禁止提交真实密钥**。
- 布尔与字典字段等编码红线以 `code_formater.md` 与 `.cursor/rules` 为准。

## 与前端协作

- 管理端 base：`/dev-api`（开发代理）或 `/prod-api`（Nginx 去前缀反代）。
- Client 凭据与 `sys_oauth_client` / RegisteredClient 种子保持一致。
