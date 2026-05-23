# 环境搭建

## 必需软件

| 软件 | 版本建议 | 用途 |
|------|----------|------|
| JDK | 17+ | 编译与运行 quickboot |
| Maven | 3.6+ | 后端构建 |
| Node.js | 18 或 20 LTS | quick-ui、docs |
| pnpm | 9.x（与 `quick-ui/package.json` 一致） | 前端/文档包管理 |

## 可选（按环境）

| 软件 | 用途 |
|------|------|
| MySQL 8+ | 生产/联调数据库（`application-prod.yml`） |
| Redis | 生产 Token、nonce 防重放、验证码存储等 |
| Git | 克隆仓库 |

## 后端配置要点

### 开发（默认 `dev` Profile）

- 数据源：H2 文件模式，路径 `./data/qcc`
- H2 Console：开发配置下可开启（注意勿暴露到公网）
- Token 存储：`qc.oauth2.token-store=local`（单实例）
- 启动参数示例：

```text
-Djasypt.encryptor.password=<你的加密密钥>
```

### 生产（`prod` Profile）

- 数据源：MySQL，敏感项建议 Jasypt `ENC(...)` 包裹
- Token / 缓存：建议 Redis（`qc.oauth2.token-store=redis`）
- 关闭 Druid 监控页、收紧 `qc.security.web.anonymous-paths`

配置文件位置：

- `quickboot/quickboot-web/src/main/resources/application.yml`
- `application-dev.yml` / `application-prod.yml`

## 前端配置要点

文件：`quick-ui/.env.development`（勿将生产密钥提交仓库）

| 变量 | 说明 |
|------|------|
| `VITE_APP_BASE_API` | 后端 API 根路径（含 `/dev-api` 等前缀时与代理一致） |
| `VITE_APP_CLIENT_ID` | OAuth Client HMAC，默认 `quick-ui` |
| `VITE_APP_CLIENT_SIGN_KEY` | 与库中 `client_secret` 明文一致 |

## 文档站

```bash
cd docs && pnpm i
```

无额外数据库依赖。

## IDE 建议

- 后端：IntelliJ IDEA，启用 Lombok、Annotation Processing
- 前端：VS Code / Cursor，推荐 Volar、ESLint
- 统一编码：**UTF-8 无 BOM**（见仓库 `AGENTS.md`）

## 下一步

[快速上手](./quick-start)
