# 环境搭建

## 必需软件

| 软件 | 版本建议 | 用途 |
|------|----------|------|
| JDK | 17+ | 编译与运行 quickboot |
| Maven | 3.6+ | 后端构建 |
| Node.js | 18 或 20 LTS | quick-ui、quick-h5、docs |
| pnpm | 9.x（与 `quick-ui/package.json` 一致） | 前端/文档包管理 |

## 可选（按环境）

| 软件 | 用途 |
|------|------|
| MariaDB / MySQL 8+ | 生产或关闭嵌入式库后的外部数据库 |
| Redis | 生产 Token / 缓存；开发默认可嵌入式 |
| Git | 克隆仓库 |
| 微信开发者工具 | quick-h5 小程序调试 |

## 后端配置要点

### 开发（默认 `dev` Profile）

- 数据源：进程内 MariaDB Embedded（mariadb4j），默认端口 `3307`，库名 `quickboot`
- 数据目录：`./data/mariadb`（解压缓存 `./data/mariadb-base`）
- 关闭嵌入式库：`qc.dev.embedded-mariadb.enabled=false` 后改连外部库
- Redis：开发默认可嵌入式；关闭：`qc.dev.embedded-redis.enabled=false`
- HTTP 端口：**9993**

### 生产（`prod` Profile）

- 外部 MariaDB/MySQL + Redis；敏感项放目标机 `.env.properties`（见 `deploy/env/README.md`）
- Jenkins 只发 jar，不覆盖机上密钥文件

配置文件位置：

- `quickboot/quickboot-app/src/main/resources/application.yml`
- `application-dev.yml` / `application-prod.yml`（若有）
- `.env.properties.example`

## 管理端配置要点

文件：`quick-ui/.env.development`（勿提交生产密钥）

| 变量 | 说明 |
|------|------|
| `VITE_APP_BASE_API` | 开发常用 `/dev-api` |
| `VITE_OAUTH_CLIENT_ID` | 默认 `quick-ui` |
| `VITE_OAUTH_CLIENT_SECRET` | 默认 `quick-ui-secret` |

## 移动端配置要点

见 `quick-h5/.env.development`：`VITE_APP_BASE_API`、`VITE_APP_BASE_API_NATIVE`、`VITE_OAUTH_CLIENT_*`（默认 `quick-h5`）。

## 文档站

```bash
cd docs && pnpm i
```

无额外数据库依赖。生产构建产物由 `Jenkinsfile.docs` 发布到 `/opt/quickboot/www/docs`，Nginx 路径 `/docs/`。

## IDE 建议

- 后端：IntelliJ IDEA，启用 Lombok、Annotation Processing
- 前端：VS Code / Cursor，推荐 Volar、ESLint
- 统一编码：**UTF-8 无 BOM**（见仓库根 `code_formater.md`）

## 下一步

[快速上手](./quick-start)
