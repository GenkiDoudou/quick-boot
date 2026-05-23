# 联调测试

## 启动顺序

1. 后端 `9992` 就绪（`/actuator/health` 为 UP）
2. 前端 `8800`，确认代理 `/dev-api` → 9992
3. 浏览器登录管理端

## 检查项

| 项 | 验证 |
|----|------|
| Client 签名 | 登录请求带 `X-Client-*` 头，非 30002 |
| 动态菜单 | Network 中 `getRouters` 200 |
| 列表页 | 任一带 C7JsonTable 的菜单可分页 |
| OAuth | 「OAuth 客户端」列表可打开 |
| Swagger | 直连 9992 `/swagger-ui.html` 与前端代理均可 |

## 常见失败

| 现象 | 处理 |
|------|------|
| 401 / 30002 | 检查 `VITE_APP_CLIENT_SIGN_KEY` 与库中 secret |
| 404 on API | `VITE_APP_BASE_API` 与 vite proxy 不一致 |
| 菜单空白 | 用户无角色或菜单 `status` 停用 |

详见 [常见问题](../guide/faq)。
