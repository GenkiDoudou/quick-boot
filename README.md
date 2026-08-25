# QuickBoot

企业级全栈后台解决方案：**Spring Boot 后端** + **Vue 3 管理端** + **uni-app 移动端** + **VitePress 文档站**。

## 在线演示

| 端 | 地址 |
|----|------|
| 文档 | https://qc.126w.com/docs |
| 管理后台 | https://qc.126w.com |
| H5 | https://qc.126w.com/h5 |

本地文档：`cd docs && pnpm i && pnpm dev`（站点 `base` 为 `/docs`）。

## 仓库结构

```text
quickboot/                 # 本 Monorepo
├── quickboot/             # Java 17 / Spring Boot 4（Maven 多模块）
├── quick-ui/              # 管理端 Vue 3 + Element Plus + C7 组件
├── quick-h5/              # 移动端 uni-app（H5 / 微信小程序）
├── docs/                  # VitePress 文档
├── deploy/                # Jenkins / Nginx / systemd / 环境说明
├── openspec/              # OpenSpec 变更
├── AGENTS.md              # AI / 协作者流程
└── code_formater.md       # 编码与分层约定
```

## 技术栈摘要

| 端 | 技术 |
|----|------|
| 后端 | Java 17、Spring Boot 4、MyBatis-Plus、Flyway、OAuth2 AS、SpringDoc |
| 管理端 | Vue 3、Vite、Pinia、Element Plus、pnpm 9 |
| 移动端 | uni-app Vue3、uView Pro |
| 文档 | VitePress |

开发默认后端端口 **9993**；开发可用嵌入式 MariaDB / Redis。种子账号：`admin` / `admin123`。

## 快速开始

### 1. 后端

```bash
cd quickboot
mvn -pl quickboot-app -am install -DskipTests
mvn -pl quickboot-app spring-boot:run
```

详见 `quickboot/README.md`、文档 [后端快速上手](https://qc.126w.com/docs/docs/backend/getting-started)。

### 2. 管理端

```bash
cd quick-ui
pnpm install
pnpm dev
```

### 3. 移动端（可选）

```bash
cd quick-h5
pnpm install
pnpm dev:h5
```

### 4. 文档站（可选）

```bash
cd docs
pnpm install
pnpm dev
```

更完整的步骤见文档 [快速上手](https://qc.126w.com/docs/docs/guide/quick-start)、[环境搭建](https://qc.126w.com/docs/docs/guide/installation)。

## 文档与规范

| 资源 | 说明 |
|------|------|
| https://qc.126w.com/docs | 在线手册（指南 / 三端说明 / 组件 API） |
| `AGENTS.md` | 协作与 Agent 约定 |
| `code_formater.md` | 编码、命名、分层红线 |
| `deploy/env/README.md` | 生产部署、Jenkins Job、目标机目录与密钥约定 |

## 部署

传统 Linux 机：`java -jar` + Nginx 静态托管。流水线与示例配置在 `deploy/`：

- `deploy/jenkins/Jenkinsfile.quickboot` / `quick-ui` / `quick-h5` / `docs`
- `deploy/nginx/quickboot.conf.example`
- 运维说明：`deploy/env/README.md`

## License

以仓库内声明为准（若未单独声明，请联系维护者）。
