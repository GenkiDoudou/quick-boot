# 认证说明（非 JWT）

本项目**不使用传统 JWT 工具类**，会话与 OAuth2 由 **Sa-Token** 管理。

| 场景 | 机制 |
|------|------|
| 管理端登录 | Sa-Token Bearer |
| OAuth2 开放接口 | OAuth2 access_token |
| API 调用方 | Client HMAC（非 JWT） |

请参阅 [接口规范](../api/index)、[OAuth2 集成](../modules/oauth2)。
