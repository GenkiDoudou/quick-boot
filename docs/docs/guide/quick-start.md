# 快速上手

以下命令均在仓库根目录 `quickboot2` 下执行，假设已安装 [环境](./installation) 中的依赖。

## 1. 启动后端

```bash
cd quickboot
mvn clean install -DskipTests
mvn -pl quickboot-web spring-boot:run
```

- 默认端口：**9992**
- 开发环境：H2 文件库 `./data/qcc`（首次启动 Flyway 自动建表）
- 接口文档：启动后访问本机 `9992` 端口的 Swagger UI（路径 `/swagger-ui.html`）
- 默认需配置 Jasypt 密钥（见 `quickboot/README.md`）：`-Djasypt.encryptor.password=你的密钥`

## 2. 启动前端

```bash
cd quick-ui
pnpm i
pnpm dev
```

- 开发服务器端口以 Vite 控制台为准（常见 80 或 5173）
- 代理目标：`.env.development` 中 `VITE_APP_BASE_API`（指向后端 9992）
- 首方 Client 签名：`VITE_APP_CLIENT_ID`、`VITE_APP_CLIENT_SIGN_KEY`（见 [OAuth2 集成](../backend/modules/oauth2)）

## 3. 启动文档站（可选）

```bash
cd docs
pnpm i
pnpm dev
```

浏览器打开控制台提示的本地地址，阅读本手册。

## 4. 首次登录

1. 使用 Flyway 初始化的管理员账号登录（具体账号密码以迁移脚本 `V13__sys_user_admin_password.sql` 等为准，或查阅运维配置）。
2. 登录成功后菜单由后端下发；系统管理、监控、代码生成、OAuth 等菜单随迁移版本逐步增加。

## 5. 常用验证

| 检查项 | 方式 |
|--------|------|
| 后端健康 | `GET /actuator/health`（dev 可匿名） |
| 动态菜单 | 登录后 Network 中查看 `getRouters` |
| 代码生成 | 菜单「系统工具 → 代码生成」 |
| OAuth2 管理 | 「系统管理 → OAuth 客户端 / OAuth 提供方」 |

## 下一步

- [环境搭建](./installation) — JDK、数据库、Redis 生产配置
- [项目介绍](./introduction) — 能力总览
- [后端模块总览](../backend/modules/index)
