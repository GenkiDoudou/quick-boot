## ADDED Requirements

### Requirement: Docs Jenkins pipeline
仓库 MUST 提供 `deploy/jenkins/Jenkinsfile.docs`，参数与阶段对齐现有 `Jenkinsfile.quick-ui`（含 `ENV`、`BRANCH`、`DEPLOY_HOSTS`、`DEPLOY_DIR`、`operate`、冒烟相关参数），在 `docs` 目录使用 pnpm@9 安装依赖并执行 `pnpm build`，将静态产物 rsync 到目标机 `DEPLOY_DIR`（默认 `/opt/quickboot/www/docs`）。

#### Scenario: Deploy builds and syncs docs
- **WHEN** Jenkins docs Job 以 `operate=deploy` 构建
- **THEN** 成功产出 VitePress 静态文件并同步到各 `DEPLOY_HOSTS` 的 `DEPLOY_DIR`

#### Scenario: Rollback unsupported
- **WHEN** `operate=rollback`
- **THEN** Pipeline 明确失败或提示不支持，并指导重新 deploy

### Requirement: Nginx docs path hosting
`deploy/nginx/quickboot.conf.example` MUST 增加 `/docs/` 静态托管规则，指向目标机 docs 目录，且不破坏现有 `/`、`/h5/`、`/prod-api/` 规则。

#### Scenario: Docs location in example conf
- **WHEN** 运维阅读 Nginx 示例配置
- **THEN** 可见 `location /docs/`（或等价）将请求映射到 docs 静态根目录

### Requirement: Deploy env docs mention www/docs
`deploy/env/README.md` MUST 说明目标机 `www/docs/`（或等价默认路径）以及 docs Job 的用途与用法摘要。

#### Scenario: README lists docs directory
- **WHEN** 运维阅读 `deploy/env/README.md` 中的目标机目录约定
- **THEN** 列表中包含 docs 静态目录，并提及对应 Jenkinsfile
