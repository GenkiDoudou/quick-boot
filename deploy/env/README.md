# 部署环境说明（密钥勿提交 Git）

本目录配合仓库根下 `deploy/jenkins`、`deploy/nginx`、`deploy/systemd`，用于 **Linux 传统机**：后端 `java -jar` + Nginx 静态托管。

权威设计见：
- 发布流水线：`docs/superpowers/specs/2026-08-17-jenkins-pipeline-deploy-design.md`
- 线上可变配置：`docs/superpowers/specs/2026-08-20-online-env-properties-design.md`  
OpenSpec：`openspec/changes/jenkins-pipeline-deploy/`

## 禁止提交

- 生产 / 测试真实数据库密码、Redis 密码、OAuth client secret、SSH 私钥
- 真实 `.env.properties` / `application-prod.yml`（仅提交 `*.example`）

密钥放在：**目标机部署目录**的 `.env.properties`（与 jar 同级）；SSH 私钥放 **Jenkins Credentials**。

## 目标机目录

```text
/opt/quickboot/
  app/                 # jar + app.sh + .env.properties（运维维护）
  www/ui/              # quick-ui dist
  www/h5/              # quick-h5 H5 产物
  logs/                # 可选
  config/              # 可选：大段 yml 覆盖（一般不必）
```

### 线上 DB / Redis（推荐）

应用已配置 `spring.config.import: optional:file:./.env.properties`。  
**Jenkins 只发 jar，不生成、不覆盖 `.env.properties`。**

1. 首装：将 [`/.env.properties.example`](./.env.properties.example) 或  
   `quickboot/quickboot-app/src/main/resources/.env.properties.example`  
   复制为 `${DEPLOY_DIR}/.env.properties`，填写真实值  
2. 发版：Jenkins `app.sh deploy` 只换 jar 并重启  
3. 改库/改密：SSH 编辑 `.env.properties` 后执行 `./app.sh restart`

```bash
sudo mkdir -p /opt/quickboot/{app,www/ui,www/h5,logs}
sudo cp deploy/env/.env.properties.example /opt/quickboot/app/.env.properties
# 编辑 DB_URL / DB_USERNAME / DB_PASSWORD / REDIS_* ：
sudo chown -R quickboot:quickboot /opt/quickboot
```

可选大段覆盖仍可用 [`application-prod.yml.example`](./application-prod.yml.example) + `SPRING_CONFIG_ADDITIONAL_LOCATION`。  
systemd 示例：`deploy/systemd/quickboot.service.example`。  
Nginx 示例：`deploy/nginx/quickboot.conf.example`（`/`、`/h5/`、`/prod-api/`）。

## Jenkins Credentials（建议 ID）

| ID | 类型 | 用途 |
|----|------|------|
| `deploy-test` | SSH Username with private key | 发布到测试机 |
| `deploy-prod` | SSH Username with private key | 发布到生产机 |
| `git-creds` | 按需 | 私有仓拉代码 |

`ENV=test|prod` 默认映射到 `deploy-test` / `deploy-prod`。可用节点环境变量覆盖凭据 ID：`DEPLOY_CRED_TEST`、`DEPLOY_CRED_PROD`。

## Jenkins 构建机 / Job 环境变量

在节点或 Job 中配置（主机名勿写死进仓库）：

| 变量 | 说明 |
|------|------|
| `QUICKBOOT_SSH_USER` | 可选，默认 `root` |
| `QUICKBOOT_SSH_OPTS` | 可选，默认 `-o StrictHostKeyChecking=no` |

构建机需：JDK 17、Maven、Node + pnpm、`ssh`、`rsync`、`curl`。

> quickboot Job：`DEPLOY_HOSTS` 填 IP，凭据 ID 与 IP 相同；不再使用 `QUICKBOOT_HOST_*` / `DEPLOY_CRED_*` 映射。

## deploy-quickboot

### 固定变量（`environment`）

| 变量 | 值 | 说明 |
|------|-----|------|
| `JAR_NAME` | `quickboot-app.jar` | 远程 jar 文件名 |
| `port` | `9993` | 健康检查端口 → `http://127.0.0.1:${port}/actuator/health` |

### 构建参数（有则保留，无则自动初始化）

**策略：** `Jenkinsfile.quickboot` **不使用** Declarative `parameters {}`（避免每次构建用仓库默认值覆盖你在 Configure 改的配置）。

| 场景 | 行为 |
|------|------|
| Job **已有**全部必需参数 | **不覆盖**，沿用 Configure 里的定义与默认值 |
| Job **完全没有**必需参数 | **首次构建自动写入**下表默认参数，构建会提示失败并请你 **再点一次 Build with Parameters** |
| Job **只缺部分**参数 | 不自动合并（防止覆盖已有项），报错列出缺失名，请手工补或删光参数后重建 |

必需参数名：`ENV`、`BRANCH`、`DEPLOY_HOSTS`、`DEPLOY_DIR`、`SPRING_PROFILE`、`operate`。

| 参数名 | 类型 | 自动初始化默认值 | 说明 |
|--------|------|------------------|------|
| `ENV` | Choice | `test` | 选项：`test` / `prod` / `dev` |
| `BRANCH` | String | `main` | Git 分支 |
| `DEPLOY_HOSTS` | String | （空） | 目标机 **IP/域名**，逗号分隔；**Jenkins SSH 凭据 ID 须与主机名相同** |
| `DEPLOY_DIR` | String | `/opt/quickboot/app` | 部署目录 |
| `SPRING_PROFILE` | String | `prod` | 传给 `app.sh --profile` |
| `operate` | Choice | `deploy` | 选项：`deploy` / `rollback` |
| `SKIP_SMOKE` | Boolean | 不勾选 | 跳过健康检查 |
| `RELEASE_NOTES` | Text | （空） | 发版说明；自动附带 git log |
| `WECOM_WEBHOOK_URL` | String | （空） | 企业微信机器人 Webhook；空则跳过通知 |
| `DEPLOY_CALLBACK_TOKEN` | String | （空） | 与目标机 `.env` 的 `DEPLOY_CALLBACK_TOKEN` 一致；空则跳过写库 |

**推荐流程：**

1. Jenkins Credentials 中创建 SSH 凭据，**ID = 主机 IP**（如 `192.168.50.105`），用户与 `QUICKBOOT_SSH_USER` 一致（默认 `root`）
2. Job Configure 中设 `DEPLOY_HOSTS=192.168.50.105`（多台逗号分隔）
3. 目标机 `.env.properties` 配置 `DEPLOY_CALLBACK_TOKEN`；Job 填相同 Token 与企微 Webhook
4. 使用 **Build with Parameters** 构建（可填 `RELEASE_NOTES`）

也可跳过自动初始化，在 Configure → **参数化构建过程** 手工按上表添加。

SSH 用户：节点环境变量 `QUICKBOOT_SSH_USER`，默认 `root`。

流程：`deploy` → 收集发版说明 → 构建 → 上传 jar → Smoke → **成功**：callback 写库 + 企微；**失败**：仅企微。  
**不**上传 `.env.properties`；目标机须事先放好该文件（见上文「线上 DB / Redis」）。

管理端：**系统监控 → 发布记录**（`monitor/deployRecord`）。

## 创建三个 Pipeline Job

在 Jenkins 新建 **Pipeline** Job（各一个）：

| Job 名（建议） | Script Path |
|----------------|-------------|
| `deploy-quickboot` | `deploy/jenkins/Jenkinsfile.quickboot` |
| `deploy-quick-ui` | `deploy/jenkins/Jenkinsfile.quick-ui` |
| `deploy-quick-h5` | `deploy/jenkins/Jenkinsfile.quick-h5` |

共同点：

1. Definition：Pipeline script from SCM  
2. SCM：本仓库；Script Path 如上  
3. **三端统一**：`DEPLOY_HOSTS` 填 IP，凭据 ID 与 IP 相同；参数「有则保留、无则自动初始化」（见上表）  
4. 触发：默认手动「Build with Parameters」  
5. **定时建议仅绑测试 Job**（如 `H 2 * * *` 且默认 `ENV=test`），生产 Job 限制触发权限、勿配自动定时

## 冒烟 URL

| 端 | 实例探活 | 入口探活 |
|----|----------|----------|
| 后端 | 每台 `http://127.0.0.1:${port}/actuator/health`（`port` 默认 9993） | — |
| quick-ui | — | `GET {SMOKE_BASE_URL}/`；空则 SSH `curl -sfI http://127.0.0.1/` |
| quick-h5 | — | `GET {SMOKE_BASE_URL}/h5/`；空则 SSH `curl -sfI http://127.0.0.1/h5/` |

后端 health 若需登录，勾选 `SKIP_SMOKE`。

## H5 构建产物路径

uni-app Vite H5 默认产物目录：`quick-h5/dist/build/h5/`。  
`Jenkinsfile.quick-h5` 已按此路径 rsync；若升级 uni 后路径变化，以一次本地 `pnpm build:h5` 输出为准并改 Jenkinsfile。

## 首次运维清单

1. 目标机安装 JDK 17、Nginx；创建 `DEPLOY_DIR` 并保证 SSH 用户可写  
2. 放置 `.env.properties`（关嵌入式由 `application-prod.yml` 保证；填外部库/Redis）  
3. 前端仍须手工启用 Nginx conf，`nginx -t && systemctl reload nginx`  
4. Jenkins 配置 SSH 凭据与三个 Job、主机环境变量  
5. 前端发布若仍用 sudo reload Nginx，按现有 ui/h5 Jenkinsfile 权限模型配置  
6. 先 `ENV=test` 分别跑通三 Job，再手动 `prod`
