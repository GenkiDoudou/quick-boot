# 接口规范

## 统一响应 `R<T>`

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { }
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 401 | 未登录或 Token 失效 |
| 403 | 无权限 |
| 500 | 系统错误 |
| 30002 | Client 签名校验失败（OAuth） |

业务失败通常 HTTP 仍为 **200**，以 `code` 判断；前端 `request.js` 统一拦截提示。

## 分页

请求：`pageNum`、`pageSize`（`PageRequest`）  
响应：`PageInfo`：`rows`、`total` 等。

## 认证头

```http
Authorization: Bearer <sa-token>
```

管理端登录后 Token 存 Cookie `Admin-Token`，Axios 自动附带。

## Client HMAC 头（除白名单外必填）

```http
X-Client-Id: quick-ui
X-Client-Timestamp: 1710000000
X-Client-Nonce: <random-hex>
X-Client-Signature: <base64-hmac>
```

算法见 [OAuth2 集成](../modules/oauth2#调用方鉴权client-hmac-签名)。

## Open API

```http
GET /open-api/v1/userinfo
Authorization: Bearer <oauth2-access-token>
```

须同时满足客户端 `api_path_patterns` 授权。

## 文档与调试

- **Swagger UI**：启动后端后访问 `/swagger-ui.html`
- 开发 Profile 可对 `/actuator/**` 放行（勿用于生产）

## 模块索引

- [用户接口](./user)
- [权限接口](./permission)
- [系统接口](./system)
