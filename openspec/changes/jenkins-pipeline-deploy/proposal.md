## Why

仓库内尚无可版本化的 Jenkins Pipeline 与 Linux 传统部署约定，三端（quickboot / quick-ui / quick-h5）难以在测试/生产机上用同一套参数化流程发布。需要把已确认的 jar + Nginx 同域路径方案落到仓库制品与最小前端生产配置修正上，便于运维按 Job 独立发布。

## What Changes

- 新增 `deploy/` 目录：三个 Declarative Jenkinsfile、Nginx/systemd 示例、环境配置说明（密钥不进 Git）。
- 约定目标机目录、`ENV=test|prod` SSH 凭据映射、构建/rsync/重启/冒烟阶段。
- 最小修正前端生产构建：`VITE_APP_BASE_API=/prod-api`；quick-h5 生产 `base` 与 `/h5/` 对齐（若现网不一致）。
- 提供后端生产外部配置说明模板（机上 `application-prod.yml` 要点），不提交真实密钥。
- **非变更**：Docker/K8s、DB/Redis 安装、小程序发布、自动回滚、Shared Library。

## Capabilities

### New Capabilities

- `jenkins-linux-deploy`: Linux 传统机上 Jenkins 三 Job 参数化构建与 SSH 发布、同域 Nginx 路径、systemd 跑 jar，以及前端生产 base/API 与部署路径一致的要求。

### Modified Capabilities

- （无）

## Impact

- 新增：`deploy/jenkins/*`、`deploy/nginx/*`、`deploy/systemd/*`、`deploy/env/README.md`。
- 可能修改：`quick-ui/.env.production`、`quick-h5` 生产 base / `.env.production`（若缺失则新增）。
- 运维：Jenkins Credentials、目标机目录与 Nginx/systemd 首次手工；构建机需 JDK17、Maven、pnpm、ssh、rsync。
- 运行时：后端依赖机上外部 MariaDB/Redis 与 `prod` profile；不改变业务 API 契约。
