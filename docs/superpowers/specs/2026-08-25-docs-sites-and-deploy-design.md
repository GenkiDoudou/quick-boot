# VitePress 三端实用文档补全与 docs 部署

日期：2026-08-25  
状态：已批准（对话确认）  
范围：`docs/`（VitePress 内容与导航）、`deploy/jenkins/Jenkinsfile.docs`、`deploy/nginx/quickboot.conf.example`、`deploy/env/README.md`

## 1. 已确认决策

| 项 | 选择 |
|----|------|
| 文档深度 | A：实用向（每端概述 + 启动 + 结构 + 约定） |
| docs 部署 | A：`Jenkinsfile.docs` + rsync；Nginx 路径 `/docs/` |
| 信息架构 | A：保留「指南」；新增后端 / 管理端 / 移动端三组 |
| 实现路径 | 方案 1：12 页落盘 + 对齐现有 Jenkins 静态发布模式 |

## 2. 目标与非目标

**目标**

- 在 VitePress 中为 `quickboot`、`quick-ui`、`quick-h5` 提供可导航的实用文档。
- 修正现有 `guide` 中与现网不符的描述（端口、嵌入式依赖等）。
- 提供与 quick-ui 同模式的 docs 构建与发布流水线，以及 Nginx `/docs/` 托管约定。

**非目标**

- 按 `capabilities-outline` 历史全量分区铺开（components / design / skill 等）。
- Docker / K8s / 独立子域名。
- docs 产物 rollback。
- 修改三端业务功能代码。
- 将 `docs/superpowers/` 编入站点（保持现有 `srcExclude`）。

## 3. 文档树与导航

### 3.1 新增页面

```text
docs/docs/
  backend/          # quickboot
    index.md
    getting-started.md
    structure.md
    conventions.md
  frontend/         # quick-ui
    index.md
    getting-started.md
    structure.md
    conventions.md
  h5/               # quick-h5
    index.md
    getting-started.md
    structure.md
    conventions.md
```

每端四页职责：

| 页 | 内容 |
|----|------|
| `index` | 定位、技术栈、与另外两端关系 |
| `getting-started` | 安装依赖、构建/启动命令、默认端口与账号、联调前提 |
| `structure` | 目录/模块划分（Maven 或前端目录） |
| `conventions` | 该端关键约定（后端 R/权限/OpenAPI；管理端路由签名与 C7；H5 多端 env 与 OAuth client） |

### 3.2 导航与首页

- **nav**：指南、后端、管理端、移动端四个入口。
- **sidebar**：指南保留；三端各一组（概述 → 上手 → 结构 → 约定）。
- **首页 features**：坏链改到真实页。
- **capabilities-outline**：改为「实用向已完成 / 全量大纲暂缓」，去掉虚假 ✅。

### 3.3 内容原则

- 以现网为准：后端默认端口 **9993**、开发嵌入式 MariaDB/Redis、Spring Boot 4.x 等；同步修正 `guide`。
- 不写组件/API 全手册；需要处可外链仓库 README 或 `docs/superpowers/specs/`。
- VitePress `base: "/docs"` 已存在，内链使用站点相对路径，与部署前缀一致。

## 4. 部署约定

### 4.1 目标机目录

```text
/opt/quickboot/
  app/
  www/ui/
  www/h5/
  www/docs/          # 新增
```

### 4.2 Jenkinsfile.docs

对齐 `Jenkinsfile.quick-ui`：

| 项 | 约定 |
|----|------|
| 参数 | `ENV` / `BRANCH` / `DEPLOY_HOSTS` / `DEPLOY_DIR` / `operate` / `SKIP_SMOKE` / `SMOKE_BASE_URL` |
| 默认 `DEPLOY_DIR` | `/opt/quickboot/www/docs` |
| Build | `docs` 目录下 pnpm@9 install + `pnpm build` |
| 产物 | VitePress 实际输出目录（实现时以构建结果为准，常见 `.vitepress/dist`） |
| Deploy | `rsync -az --delete` |
| Smoke | `SMOKE_BASE_URL` 或 SSH 本机 `curl` `/docs/` |
| Rollback | 暂不支持 |

### 4.3 Nginx

在 `deploy/nginx/quickboot.conf.example` 增加：

```nginx
location /docs/ {
    alias /opt/quickboot/www/docs/;
    try_files $uri $uri/ /docs/index.html;
}
```

不改动 `/`、`/h5/`、`/prod-api/`。

### 4.4 运维说明

更新 `deploy/env/README.md`：补充 `www/docs/` 与 docs Job 用法。本期不以新建完整「部署文档分区」为必须项。

## 5. 验收标准

1. `cd docs && pnpm build` 成功；侧栏可打开三端全部新页。
2. `guide` 中端口/启动/依赖描述与现网一致。
3. 存在 `deploy/jenkins/Jenkinsfile.docs`；Nginx 示例含 `/docs/`。
4. `deploy/env/README.md` 写明 `www/docs/`。
5. 首页与大纲不再指向不存在的深链（或已改为真实页）。

## 6. 风险

- VitePress 产物路径需以实际构建为准写入 Jenkinsfile。
- Nginx `alias` + `try_files` 在子路径需验证深链刷新。
