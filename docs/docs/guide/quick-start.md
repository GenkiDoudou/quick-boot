# 快速上手

以下命令均在仓库根目录下执行，假设已安装 [环境搭建](./installation) 中的依赖。

## 1. 启动后端

```bash
cd quickboot
mvn -pl quickboot-app -am install -DskipTests
mvn -pl quickboot-app spring-boot:run
```

- 默认端口：**9993**
- 开发环境：嵌入式 MariaDB（`127.0.0.1:3307` / 库 `quickboot`）+ 嵌入式 Redis
- 种子账号：`admin` / `admin123`
- 详细说明：[后端快速上手](/docs/backend/getting-started)

## 2. 启动管理端

```bash
cd quick-ui
pnpm i
pnpm dev
```

- 代理：`.env.development` 中 `VITE_APP_BASE_API=/dev-api`（指向后端 9993）
- Client：`VITE_OAUTH_CLIENT_ID` / `VITE_OAUTH_CLIENT_SECRET`（默认 `quick-ui` / `quick-ui-secret`）
- 详见：[管理端快速上手](/docs/frontend/getting-started)

## 3. 启动移动端（可选）

```bash
cd quick-h5
pnpm i
pnpm dev:h5
```

- Client 默认 `quick-h5` / `quick-h5-secret`
- 详见：[移动端快速上手](/docs/h5/getting-started)

## 4. 启动文档站（可选）

```bash
cd docs
pnpm i
pnpm dev
```

浏览器打开控制台提示的本地地址（站点 `base` 为 `/docs`）。  
线上演示入口：[在线演示](./demo)。

## 5. 常用验证

| 检查项 | 方式 |
|--------|------|
| 后端健康 | `GET http://127.0.0.1:9993/actuator/health` |
| 动态菜单 | 管理端登录后查看 `getRouters` |
| H5 登录 | `pnpm dev:h5` 后使用同一管理员账号 |

## 下一步

- [环境搭建](./installation)
- [项目介绍](./introduction)
- [后端概述](/docs/backend/) · [管理端概述](/docs/frontend/) · [移动端概述](/docs/h5/)
