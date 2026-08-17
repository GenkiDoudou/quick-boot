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

Pipeline 参数 `ENV=test|prod` 映射到上述 SSH 凭据；未知 `ENV` 会直接失败。

## Jenkins 构建机环境变量

在节点或 Job 中配置（主机名勿写死进仓库）：

| 变量 | 说明 |
|------|------|
| `QUICKBOOT_HOST_TEST` | 测试机 SSH 主机名或 IP |
| `QUICKBOOT_HOST_PROD` | 生产机 SSH 主机名或 IP |
| `QUICKBOOT_SSH_USER` | 可选，默认 `quickboot` |

构建机需：JDK 17、Maven、Node + pnpm、`ssh`、`rsync`、`curl`。

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
3. 参数由 Jenkinsfile 声明：`ENV`、`BRANCH`、`SMOKE_BASE_URL`（可选）  
4. 触发：默认手动「Build with Parameters」  
5. **定时建议仅绑测试 Job**（如 `H 2 * * *` 且默认 `ENV=test`），生产 Job 限制触发权限、勿配自动定时

## 冒烟 URL

| 端 | 优先（填 Job 参数 `SMOKE_BASE_URL`，如 `https://app.example.com`） | 回退（参数为空） |
|----|------------------------------------------------------------------|------------------|
| 后端 | `GET {SMOKE_BASE_URL}/prod-api/actuator/health` | SSH 目标机 `curl -sf http://127.0.0.1:9993/actuator/health` |
| quick-ui | `GET {SMOKE_BASE_URL}/` | SSH `curl -sfI http://127.0.0.1/`（本机 Nginx） |
| quick-h5 | `GET {SMOKE_BASE_URL}/h5/` | SSH `curl -sfI http://127.0.0.1/h5/` |

若生产关闭匿名 `/actuator/health`，请改用回退方式或调整安全放行后再冒烟。

## H5 构建产物路径

uni-app Vite H5 默认产物目录：`quick-h5/dist/build/h5/`。  
`Jenkinsfile.quick-h5` 已按此路径 rsync；若升级 uni 后路径变化，以一次本地 `pnpm build:h5` 输出为准并改 Jenkinsfile。

## 首次运维清单

1. 目标机安装 JDK 17、Nginx；创建用户 `quickboot` 与目录  
2. 放置 `application-prod.yml`（关嵌入式 DB/Redis，连外部库）  
3. 安装 systemd unit、启用 Nginx conf，`nginx -t && systemctl reload nginx`  
4. Jenkins 配置 SSH 凭据与三个 Job、主机环境变量  
5. 部署用户（默认 `quickboot`）对 `systemctl restart quickboot`、`nginx -t`/`nginx -s reload`、移动 jar 具备免密 sudo（或按你们权限模型调整 Jenkinsfile）  
6. 先 `ENV=test` 分别跑通三 Job，再手动 `prod`
