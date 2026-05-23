# 认证授权

## 管理端会话（Sa-Token）

- 登录：`POST /login`（需 Client HMAC）
- Token：Header `Authorization: Bearer <token>`
- 前端存储：Cookie `Admin-Token`
- 有效期：见 `sa-token.timeout`（默认约 30 天）

## 权限模型

```text
用户 ──N:N── 角色 ──N:N── 菜单(含 perms)
                └── 数据权限 dataScope
```

- **接口级**：`@SaCheckPermission("system:user:list")`  
- **路由级**：`getRouters` 按用户过滤菜单  
- **按钮级**：前端 `v-hasPermi`

## OAuth2

| 角色 | 说明 |
|------|------|
| AS | `/oauth2/*` 发放 code/token |
| Client | 联邦登录 IdP |
| Open API | Bearer OAuth2 access_token |

详见 [OAuth2 集成](../backend/modules/oauth2)。

## 匿名与白名单

`qc.security.web.anonymous-paths`、`qc.security.client-sign.exclude-paths` 配置无需登录或无需签名的路径。
