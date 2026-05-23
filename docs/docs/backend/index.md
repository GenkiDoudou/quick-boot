# 后端概述

`quickboot` 是 QuickBoot 的 **Java 后端工程**，采用 Maven 多模块，对外提供 REST API、OAuth2 端点、Open API 与 Flyway 数据库迁移。

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Spring Boot 3.5.x、Spring Validation |
| 持久化 | MyBatis-Plus、Flyway、Druid |
| 安全 | Sa-Token 1.45（含 OAuth2 模块） |
| 任务 | Spring Quartz |
| 文档 | SpringDoc OpenAPI 3 |
| 工具 | Hutool、EasyExcel、Jasypt、OWASP HTML Sanitizer（公告富文本） |

## 模块职责

### quickboot-common

下沉全项目复用能力，业务模块**不应重复实现**同类逻辑：

- **API 契约**：`R<T>`、`PageInfo`、`HttpCodes`
- **异常**：`BaseException`、`WarningException`、`ErrorCodes`
- **校验**：`AddGroup` / `UpdateGroup`
- **国际化**：`I18nUtil`、中英文资源文件
- **缓存**：`cacheName#ttl` 约定的 Caffeine/Redis `CacheManager`
- **Excel**：EasyExcel 封装、字典转换、导入错误定位
- **脱敏**：`@Sensitive` + Jackson 序列化
- **验证码**：Tianai 行为验证码、本地/Redis 存储适配
- **文件**：`FileTemplate`（本地 / MinIO）
- **操作日志**：AOP 采集、异步落库、`@IgnoreLogger`
- **Web 防火墙**：安全头、CORS、XSS、SQL 注入检测、敏感词、幂等 Token、Method/Host 白名单、密码编解码（BCrypt + SM4）

### quickboot-core

预留扩展模块，当前**无业务 Java 实现**，`quickboot-web` 依赖它以便未来抽取领域核心。

### quickboot-web

- 启动类：`io.github.genkidoudou.WebApplication`
- 全部 `controller` / `service` / `mapper` / `domain`
- 资源：`application*.yml`、`db/migration/V*.sql`

## 运行与端口

```bash
cd quickboot
mvn -pl quickboot-web spring-boot:run
```

- 默认端口：**9992**（`server.port`）
- 默认 Profile：**dev**（H2）
- Swagger UI：`/swagger-ui.html`

## 配置命名空间（`qc.*`）

| 前缀 | 作用 |
|------|------|
| `qc.oauth2` | Token 存储、AS/Client 开关、授权类型 |
| `qc.login` | 登录验证码开关 |
| `qc.monitor` | 操作日志、任务导出上限等 |
| `qc.security.web` | 匿名路径 |
| `qc.security.client-sign` | Client HMAC 签名校验 |
| `qc.security.firewall` | XSS/SQL/敏感词/幂等/CORS 等 |
| `qc.captcha` | 行为验证码 |
| `quickboot.gen` | 代码生成作者、包名、ZIP 名 |

生产环境差异见 `application-prod.yml`（MySQL、Redis、关闭部分监控暴露面）。

## 可观测性

- **Actuator**：`health`、`metrics`、`prometheus`（dev 可配置匿名）
- **OTLP**：`management.otlp.tracing` 可对接链路追踪
- **Druid**：开发环境 StatView；生产建议关闭

## 相关文档

- [项目结构](./structure)
- [功能模块总览](./modules/index)
- [OAuth2 集成](./modules/oauth2)
