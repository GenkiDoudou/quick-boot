# Jenkins Pipeline 部署 quickboot / quick-ui / quick-h5

日期：2026-08-17  
状态：已批准（OpenSpec `jenkins-pipeline-deploy`）  
范围：`deploy/`（Jenkinsfile、Nginx/systemd 示例）、目标机发布约定；不改业务功能代码（前端 base/`VITE_*` 若与 `/h5/`、`/prod-api` 不一致则在实现计划中最小修正）

## 1. 已确认决策

| 项 | 选择 |
|----|------|
| 运行形态 | A：传统机 — 后端 `java -jar`，前端 Nginx 静态托管 |
| Jenkins 与业务机 | C：多台目标机（测试/生产），同一套 Pipeline 用参数选环境 |
| Job 拆分 | B：三个独立 Pipeline Job（互不影响） |
| 操作系统 | A：Jenkins 构建机与目标机均为 Linux |
| 数据库 / Redis | A：目标机已装外部 MariaDB + Redis，仅配连接串 |
| 对外访问 | A：同域名路径 — `/` → quick-ui，`/h5/` → quick-h5，`/prod-api/` → 后端 |
| 触发方式 | D：手动构建 + 定时构建均可 |
| 实现路径 | 方案 1：仓库内 Declarative Jenkinsfile + SSH/rsync 发布 |

## 2. 目标与非目标

**目标**

- 三个独立 Jenkins Job 可分别构建并发布到 `test` / `prod`。
- 目标机统一目录与 Nginx 路径约定；后端以 `prod` profile + 机上外部配置文件运行。
- 仓库提供可版本化的 Jenkinsfile 与 Nginx/systemd 示例；密钥不进 Git。

**非目标**

- Docker / Compose / Kubernetes。
- 在 Pipeline 中安装 MariaDB、Redis、JDK、Nginx。
- 微信小程序（`build:mp-weixin`）发布与提审。
- 自动回滚、蓝绿/金丝雀、多实例负载均衡。
- Shared Library 抽取（后续可演进，本期不做）。

## 3. 总体架构

### 3.1 单环境拓扑

```text
浏览器
  └─ Nginx :80/443
        ├─ /           → /opt/quickboot/www/ui      (quick-ui)
        ├─ /h5/        → /opt/quickboot/www/h5     (quick-h5)
        └─ /prod-api/  → http://127.0.0.1:9993/    (去掉 /prod-api 前缀)

quickboot.service  → java -jar ... --spring.profiles.active=prod
MariaDB / Redis    → 目标机已有；连接信息仅在机上配置文件
```

### 3.2 发布链路

```text
Jenkins（构建机）
  → Checkout → Build →（可选 Archive）
  → SSH/rsync 到 ENV 对应主机
  → 覆盖 jar 或静态目录 → systemctl restart / nginx reload
  → Smoke（curl）
```

### 3.3 环境映射

| ENV 参数 | Jenkins Credentials（建议 ID） | 用途 |
|----------|--------------------------------|------|
| `test` | `deploy-test`（SSH） | 测试机 |
| `prod` | `deploy-prod`（SSH） | 生产机 |

具体 Host/用户名写在 Jenkins Credentials 中，不写入仓库。

## 4. 仓库目录约定

```text
deploy/
  jenkins/
    Jenkinsfile.quickboot
    Jenkinsfile.quick-ui
    Jenkinsfile.quick-h5
  nginx/
    quickboot.conf.example
  systemd/
    quickboot.service.example
  env/
    README.md    # 说明：真实 yml/密钥放目标机或 Credentials，勿提交
```

## 5. 三个 Job：参数、阶段、构建与发布

### 5.1 公共参数

| 参数 | 说明 |
|------|------|
| `ENV` | `test` / `prod`（必选） |
| `BRANCH` | 默认主干（如 `main`） |
| 触发 | 手动；各 Job 可另配定时（如仅测环境每日凌晨） |

### 5.2 阶段骨架

1. **Checkout** — 指定分支  
2. **Build** — 见下表  
3. **Archive**（可选）— 保留产物便于排查  
4. **Deploy** — SSH 到对应主机，覆盖产物并重启/reload  
5. **Smoke** — 轻量 HTTP 检查  

任一阶段失败即停止；本期不做自动回滚。

### 5.3 分 Job 细节

| Job 名（建议） | Build | 产物 | Deploy |
|----------------|-------|------|--------|
| `deploy-quickboot` | `cd quickboot && mvn -pl quickboot-app -am package -DskipTests` | `quickboot-app/target/*.jar` | 覆盖 `/opt/quickboot/app/` → `systemctl restart quickboot` |
| `deploy-quick-ui` | `cd quick-ui && pnpm i --frozen-lockfile && pnpm build:prod` | `quick-ui/dist/` | rsync → `/opt/quickboot/www/ui/` → `nginx -s reload` |
| `deploy-quick-h5` | `cd quick-h5 && pnpm i --frozen-lockfile && pnpm build:h5` | uni-app H5 产物目录（实现时以一次本地/CI 构建输出为准，通常为 `dist/build/h5/`） | rsync → `/opt/quickboot/www/h5/` → reload |

### 5.4 前端构建约定

- API 基址与同域反代一致：使用相对路径 **`/prod-api`**（避免把内网绝对 URL 打进包）。实现阶段若 `.env.production` 仍为绝对地址，改为 `/prod-api`。
- OAuth `VITE_OAUTH_CLIENT_*`：可用 Jenkins Secret 注入；真实密钥不提交仓库。
- H5 挂载在 `/h5/`：构建 `base`（或等价配置）须为 `/h5/`，与 Nginx `location /h5/` 一致。

### 5.5 后端配置约定

- 目标机：`/opt/quickboot/config/application-prod.yml`（或等价），含 datasource、redis、OAuth issuer 等。
- 关闭嵌入式 MariaDB/Redis（与本地 `dev` 分离）。
- Pipeline **只发布 jar**，默认不覆盖机上 yml。
- 启动：`--spring.profiles.active=prod`，并通过 `SPRING_CONFIG_ADDITIONAL_LOCATION=file:/opt/quickboot/config/`（或等价）加载外部配置。

## 6. Nginx、systemd、目标机与凭据

### 6.1 Nginx 路径（示例逻辑）

- `location /` → `root /opt/quickboot/www/ui;` + SPA `try_files`
- `location /h5/` → `alias /opt/quickboot/www/h5/;` + SPA 回退
- `location /prod-api/` → `proxy_pass http://127.0.0.1:9993/;`（末尾 `/` 去掉前缀）
- 转发头：`Host`、`X-Real-IP`、`X-Forwarded-For`、`X-Forwarded-Proto`
- HTTPS/证书/防火墙由运维维护；仓库仅提供 `.example`

### 6.2 systemd（示例要点）

- 单元名：`quickboot.service`
- `WorkingDirectory=/opt/quickboot/app`
- `ExecStart`：`java -jar /opt/quickboot/app/quickboot-app.jar --spring.profiles.active=prod`
- 环境变量指向 `/opt/quickboot/config/`
- 运行用户：专用 `quickboot`（非 root）

### 6.3 目标机目录（一次性手工）

```text
/opt/quickboot/
  app/
  config/          # application-prod.yml
  www/ui/
  www/h5/
  logs/            # 可选
```

### 6.4 Jenkins Credentials（建议）

| ID | 类型 | 用途 |
|----|------|------|
| `deploy-test` / `deploy-prod` | SSH Username with private key | 发布 |
| `git-creds`（私有仓时） | 相应 Git 凭据 | 拉代码 |
| `oauth-ui-secret` / `oauth-h5-secret`（可选） | Secret text | 构建注入 |

DB/Redis 密码等只存在目标机配置文件中，不进 Git，避免打进构建日志。

### 6.5 构建机前置

- JDK 17、Maven、Node（及 pnpm/corepack）、`ssh`、`rsync`

### 6.6 Smoke

- 后端：经 Nginx `GET /prod-api/actuator/health`（或 SSH 打本机 `9993`；以现网安全放行与 context 为准）
- 前端：`/`、`/h5/` 返回 HTTP 200

### 6.7 运维首次清单（Pipeline 外）

1. 安装 JDK 17、Nginx；创建目录与 `quickboot` 用户  
2. 编写并放置 `application-prod.yml`  
3. 安装 systemd unit、启用 Nginx conf 并 `nginx -t`  
4. Jenkins 配置两套 SSH 凭据与三个 Pipeline Job（脚本指向仓库 `deploy/jenkins/...`）

## 7. 风险与约束

- 后端默认仓库内无独立 `application-prod.yml` 时，必须以目标机外部配置为准，否则易误用 `dev`/嵌入式组件。
- H5 `base` 与 Nginx `/h5/` 不一致会导致静态资源 404。
- `pnpm --frozen-lockfile` 要求 lockfile 与 `package.json` 一致；构建机 Node 主版本建议与本地开发对齐。
- 生产 Job 建议限制触发权限；定时默认只绑测试 Job，避免误刷生产。

## 8. 成功标准（设计验收）

- 文档与示例足以让运维完成：三 Job 配置、目标机目录、Nginx/systemd、SSH 发布。
- 按实现计划落地后：选 `ENV=test` 可分别发布三端；冒烟通过。
- 仓库中无真实 DB/OAuth 生产密钥。
