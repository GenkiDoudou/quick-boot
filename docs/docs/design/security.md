# 安全防护

## 网络与传输

- 生产全站 **HTTPS**
- Nginx 限制方法、隐藏版本头

## 应用层（quickboot-common）

| 能力 | 说明 |
|------|------|
| XSS 过滤 | 请求参数与 multipart |
| SQL 注入检测 | 可配置忽略 JSON 字段 |
| 敏感词 | 黑白名单 |
| 安全响应头 | CSP、HSTS 等 |
| CORS | 白名单源 |
| 幂等 Token | 防重复提交 |

配置根：`qc.security.firewall.*`

## 调用方身份

- **Client HMAC**：所有 API（白名单除外）
- **OAuth2 token**：`/open-api/**`
- **路径授权**：`api_path_patterns` Ant 匹配

## 数据安全

- 密码 BCrypt；密钥 SM4
- 日志与操作记录脱敏（`@Sensitive`）
- `client_secret` 揭示接口需独立权限

## 相关

- [安全防护模块（后端）](../backend/modules/security-module)
- [客户端管理](../backend/modules/client-management)
