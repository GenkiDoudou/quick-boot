# 外部 OAuth2 对接说明

与 `docs/demo/oauth2-auth-flow-prototype.html`、`quickboot/README.md` 对齐。

## 端点

| 端点 | 用途 |
|------|------|
| `GET /oauth2/authorize` | 授权码 |
| `POST /oauth2/token` | 换票 / refresh / client_credentials |
| `GET /oauth2/jwks` | JWT 验签公钥 |
| `POST /auth/login` | 第一方管理端密码门面（非第三方） |

Issuer：`http://127.0.0.1:9993`（见 `AuthorizationServerSettings`）

## 示例客户端

见 README 种子表：`demo-app`（用户授权码）、`job-runner`（机机）。

规则：**非 `quick-ui` 客户端禁止 password grant**（`/system/oauth-clients` 校验）。
