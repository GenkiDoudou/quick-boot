# 部署环境说明（密钥勿提交 Git）

本目录配合仓库根下 `deploy/jenkins`、`deploy/nginx`、`deploy/systemd`，用于 **Linux 传统机**：后端 `java -jar` + Nginx 静态托管。

权威设计见：`docs/superpowers/specs/2026-08-17-jenkins-pipeline-deploy-design.md`  
OpenSpec：`openspec/changes/jenkins-pipeline-deploy/`

## 禁止提交

- 生产 / 测试真实数据库密码、Redis 密码、OAuth client secret、SSH 私钥
- 真实 `application-prod.yml`（仅提交本目录 `application-prod.yml.example`）

密钥放在：**目标机** `/opt/quickboot/config/` 与 **Jenkins Credentials**。

## 目标机目录

```text
/opt/quickboot/
  app/                 # quickboot-app-*.jar
  config/              # application-prod.yml（由 example 复制后改）
  www/ui/              # quick-ui dist
  www/h5/              # quick-h5 H5 产物
  logs/                # 可选
```

配置模板：[`application-prod.yml.example`](./application-prod.yml.example)  
复制：

```bash
sudo mkdir -p /opt/quickboot/{app,config,www/ui,www/h5,logs}
sudo cp application-prod.yml.example /opt/quickboot/config/application-prod.yml
# 编辑 datasource / redis / issuer 等后：
sudo chown -R quickboot:quickboot /opt/quickboot
```

systemd 示例：`deploy/systemd/quickboot.service.example`（`prod` profile + `SPRING_CONFIG_ADDITIONAL_LOCATION`）。  
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
| `QUICKBOOT_HOSTS_TEST` | 测试机列表，逗号分隔（优先于下一档） |
| `QUICKBOOT_HOST_TEST` | 单台测试机（兼容旧配置） |
| `QUICKBOOT_HOSTS_PROD` / `QUICKBOOT_HOST_PROD` | 生产机，同上 |
| `QUICKBOOT_HOST_<凭据ID>` | 按凭据 ID 映射目标机，如 `QUICKBOOT_HOST_105=192.168.1.10` |
| `QUICKBOOT_SSH_USER` | 可选，默认 `quickboot` |
| `DEPLOY_CRED_TEST` / `DEPLOY_CRED_PROD` | 可选，覆盖 SSH 凭据 ID |
| `QUICKBOOT_SSH_OPTS` | 可选，默认 `-o StrictHostKeyChecking=no` |

构建机需：JDK 17、Maven、Node + pnpm、`ssh`、`rsync`、`curl`。

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
| `DEPLOY_HOSTS` | String | （空） | **Jenkins SSH 凭据 ID**（如 `105`），逗号分隔；**不是服务器 IP** |
| `DEPLOY_TARGET_HOSTS` | String | （空） | 目标机 **IP/域名**，逗号分隔，顺序与凭据对应；单台时填一个即可 |
| `DEPLOY_CRED_ID` | String | （空） | 可选，非空则全部目标共用此凭据 ID（忽略 DEPLOY_HOSTS 中的 ID） |
| `DEPLOY_DIR` | String | `/opt/quickboot/app` | 部署目录 |
| `SPRING_PROFILE` | String | `prod` | 传给 `app.sh --profile` |
| `operate` | Choice | `deploy` | 选项：`deploy` / `rollback` |
| `SKIP_SMOKE` | Boolean | 不勾选 | 跳过健康检查 |

**推荐流程：**

1. 新建 Job 后直接 **Build** 一次 → 自动写入默认参数（本次会失败并提示）
2. 打开 **Configure** 按需改默认值，例如：
   - `DEPLOY_HOSTS=105`（Jenkins 里 SSH 凭据 ID）
   - `DEPLOY_TARGET_HOSTS=192.168.1.105`（真实服务器 IP）
   - 或在 Jenkins 节点环境变量设 `QUICKBOOT_HOST_105=192.168.1.105`（可省略 DEPLOY_TARGET_HOSTS）
   → 保存
3. 使用 **Build with Parameters** 正式构建

也可跳过自动初始化，在 Configure → **参数化构建过程** 手工按上表添加。

SSH 用户：节点环境变量 `QUICKBOOT_SSH_USER`，默认 `root`（须与 Jenkins SSH 凭据里配置的用户一致，如凭据 `105` 为 `root`）。

目标机地址解析顺序（每台凭据）：

1. `DEPLOY_TARGET_HOSTS`（与凭据顺序对应）
2. 节点环境变量 `QUICKBOOT_HOST_<凭据ID>`（如 `QUICKBOOT_HOST_105`）
3. 仅单台时：`QUICKBOOT_HOST_TEST` / `QUICKBOOT_HOST_PROD`（按 `ENV`）

流程：`deploy` → 构建 → 每台上传 `app.sh` 和新 jar → 执行 `./app.sh deploy <newJar> --profile …`（内部负责备份覆盖）→ Smoke；`rollback` → 同步 `app.sh` → `./app.sh rollback --profile …` → Smoke。

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
3. **quickboot**：参数「有则保留、无则自动初始化」（见上表）；ui/h5 仍以 Jenkinsfile 声明为准  
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

1. 目标机安装 JDK 17、Nginx；创建 `DEPLOY_DIR`（如 `/opt/quickboot/app`）并保证 SSH 用户可写  
2. 放置 `application-prod.yml`（关嵌入式 DB/Redis，连外部库）  
3. 前端仍须手工启用 Nginx conf，`nginx -t && systemctl reload nginx`  
4. Jenkins 配置 SSH 凭据与三个 Job、主机环境变量  
5. 前端发布若仍用 sudo reload Nginx，按现有 ui/h5 Jenkinsfile 权限模型配置  
6. 先 `ENV=test` 分别跑通三 Job，再手动 `prod`
