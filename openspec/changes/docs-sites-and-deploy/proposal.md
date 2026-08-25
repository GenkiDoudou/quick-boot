## Why

VitePress 站点目前只有指南入门页，大纲中声称的 backend / frontend 等分区实际未落盘，首页与正文存在大量坏链；`quick-h5` 几乎无站内文档。同时 `deploy/` 已有三端 Jenkinsfile，但缺少文档站构建与 `/docs/` 托管约定，无法与同域名静态发布对齐。

## What Changes

- 为 `quickboot` / `quick-ui` / `quick-h5` 各新增实用向文档（概述、上手、结构、约定），并更新 nav / sidebar / 首页 / 大纲。
- 修正 `guide` 中与现网不符的端口、依赖与启动说明。
- 新增 `deploy/jenkins/Jenkinsfile.docs`，扩展 Nginx 示例与 `deploy/env/README.md`（目标机 `www/docs/` + 路径 `/docs/`）。
- 不修改三端业务功能代码；不编入 `superpowers/`；不做 Docker / 独立域名 / docs 回滚。

## Capabilities

### New Capabilities

- `vitepress-project-docs`：三端实用文档内容与站点导航（指南保留 + 后端 / 管理端 / 移动端分区）。
- `docs-static-deploy`：文档站 Jenkins 构建发布、目标机目录与 Nginx `/docs/` 托管约定。

### Modified Capabilities

- （无）现有 `openspec/specs/` 无文档站或 docs 部署相关能力需改需求级描述。

## Impact

- 影响目录：`docs/`（Markdown、nav、sidebar、首页）、`deploy/jenkins/`、`deploy/nginx/`、`deploy/env/README.md`。
- 不影响：业务 API、数据库、三端运行时行为；VitePress `base: "/docs"` 已存在，保持不变。
- 运维：新增独立 Jenkins Job（docs），与 ui/h5 同模式 SSH/rsync。
