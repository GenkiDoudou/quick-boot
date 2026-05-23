# 安全防护（Web 防火墙）

横切能力位于 **`quickboot-common`** 的 `security.firewall` 包，通过 `qc.security.firewall.*` 配置开关，在请求进入 Controller 前执行。

## 模块一览

| 模块 | 配置前缀（示意） | 作用 |
|------|------------------|------|
| 安全响应头 | `security.firewall.headers` | CSP、HSTS、X-Frame-Options 等 |
| CORS | `security.firewall.cors` | 跨域白名单 |
| XSS | `security.firewall.xss` | 请求体/参数 XSS 过滤（含 multipart） |
| SQL 注入 | `security.firewall.sql-injection` | 关键字检测；可配置忽略 JSON 字段 |
| 敏感词 | `security.firewall.sensitive-word` | 黑白名单（houbb/sensitive-word） |
| 幂等 | `security.firewall.idempotent` | 幂等 Token，Caffeine/Redis 存储 |
| Method/Host | `security.firewall.methodandhost` | 允许的方法与 Host 白名单 |
| 密码编解码 | `security.firewall.password.codec` | BCrypt + SM4（`QC_SM4_KEY_HEX`） |

## 与业务的关系

- **登录**：`AuthController` + Sa-Token；可配置图形/行为验证码（`qc.login.captcha-enabled`、`captcha.*`）。
- **匿名路径**：`qc.security.web.anonymous-paths`（如静态资源、OAuth 部分端点）。
- **Client 签名**：`qc.security.client-sign`（见 [客户端管理](./client-management)）。

## SQL 注入与 OAuth 配置

`sys_oauth_client.api_path_patterns` 若含 `/**`，可能触发 SQL 防火墙误报；已在配置中对相应 JSON 字段做忽略（见 [OAuth2 集成](./oauth2)）。

## 操作日志脱敏

`monitor.operlog` 采集请求参数/结果时经 `OperLogSensitiveMasker` 脱敏，避免密码、token 落库。

## 开发建议

- 新增公开接口时同步更新 **匿名路径** 或 **Client 签名校验** 策略，避免 401/403。
- 富文本（公告）使用 OWASP HTML Sanitizer（web 模块），与 XSS 过滤器互补。
- 生产关闭 Druid StatView、收紧 Actuator 暴露面（见 `application-prod.yml`）。

## 相关文档

- [异常处理模块](../components/通用组件/异常处理模块使用文档)
- [OAuth2 集成](./oauth2)
