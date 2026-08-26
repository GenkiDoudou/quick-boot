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
| [贡献指南](https://qc.126w.com/docs/docs/guide/contributing) | OpenSpec 流程、文档贡献、提交前检查 |

## 参与开发

欢迎 Fork 后提 Pull Request。上游仓库：[GenkiDoudou/quick-boot](https://gitee.com/GenkiDoudou/quickboot.git)

### 1. Fork 与克隆

1. 在 GitHub 打开上游仓库，点击 **Fork** 到你自己的账号/组织。
2. 克隆 **你的 Fork**（将 `YOUR_USER` 换成你的 GitHub 用户名）：

```bash
git clone https://gitee.com/GenkiDoudou/quickboot.git
cd quick-boot
git remote add upstream https://gitee.com/GenkiDoudou/quickboot.git
```

3. 确认远程：

```bash
git remote -v
# origin    → 你的 Fork
# upstream  → 上游仓库
```

### 2. 创建分支

从上游默认分支（一般为 `main` / `master`）拉最新代码，再开功能分支：

```bash
git fetch upstream
git checkout upstream/main   # 或 upstream/master
git checkout -b feat/your-topic
```

分支命名建议：`feat/…`、`fix/…`、`docs/…`，与改动类型一致。

### 3. 本地开发与验证

按上文 [快速开始](#快速开始) 启动需要改动的子工程，并遵守：

- 编码与分层：`code_formater.md`
- 协作约定：`AGENTS.md`
- 较大功能：先在 `openspec/changes/<change-id>/` 写 proposal / design / tasks（见 [贡献指南](https://qc.126w.com/docs/docs/guide/contributing)）

提交前建议至少验证你改动的部分：

```bash
# 后端
cd quickboot && mvn -pl quickboot-app -am package -DskipTests

# 管理端
cd quick-ui && pnpm build:prod

# 文档（若改了 docs）
cd docs && pnpm build
```

**勿提交**：`.env`、真实密钥、`node_modules/`、`target/`、`dist/` 等。

### 4. 提交与推送

使用 [Conventional Commits](https://www.conventionalcommits.org/)（与 `code_formater.md` 一致），例如：

```text
feat(system): 支持 xxx
fix(ui): 修复 xxx
docs: 补充 xxx 说明
```

```bash
git add <files>
git commit -m "feat(scope): 简要说明"
git push origin feat/your-topic
```

### 5. 发起 Pull Request

1. 打开 **你的 Fork** 页面，GitHub 会提示 **Compare & pull request**；或手动选：`base` = 上游 `main`，`compare` = 你的分支。
2. PR 标题与 commit 风格一致；正文说明：**做了什么、为什么、如何验证**。
3. 若上游已有新提交，先同步再推送：

```bash
git fetch upstream
git rebase upstream/main   # 或 merge upstream/main
git push origin feat/your-topic --force-with-lease   # 仅 rebase 后需要
```

4. 根据 Review 修改后追加 commit 或 squash（按维护者要求）。

### 6. 保持 Fork 同步（可选）

长期参与时，定期把上游合入本地默认分支：

```bash
git checkout main
git fetch upstream
git merge upstream/main
git push origin main
```

问题讨论、Bug 与功能建议也可通过 GitHub **Issues** 提出（若仓库已开启）。

## 部署

传统 Linux 机：`java -jar` + Nginx 静态托管。流水线与示例配置在 `deploy/`：

- `deploy/jenkins/Jenkinsfile.quickboot` / `quick-ui` / `quick-h5` / `docs`
- `deploy/nginx/quickboot.conf.example`
- 运维说明：`deploy/env/README.md`

## License

以仓库内声明为准（若未单独声明，请联系维护者）。
