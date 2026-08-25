## Context

仓库已有 VitePress 文档站（`docs/`，`base: "/docs"`）与三端 Jenkins 静态/ jar 发布（`deploy/jenkins/Jenkinsfile.quick-*`），但文档内容几乎只有 `docs/guide/`，大纲与首页仍指向不存在的 backend/frontend 等深链；`quick-h5` 缺乏站内入口。权威产品设计见 `docs/superpowers/specs/2026-08-25-docs-sites-and-deploy-design.md`（已批准）。

约束：对齐现有 ui Pipeline 参数与 rsync 模式；不改业务代码；密钥不进 Git。

## Goals / Non-Goals

**Goals:**

- 落地三端实用文档（各 4 页）与导航，修正指南过时信息与坏链。
- 新增 docs Jenkinsfile、Nginx `/docs/`、目标机 `www/docs/` 说明。

**Non-Goals:**

- 全量 capabilities 分区、组件 API 手册、Docker/K8s、独立子域名、docs rollback、编入 `superpowers/`。

## Decisions

1. **文档深度 = 实用向 12 页**  
   - 理由：立刻可导航、可维护；全量大纲成本过高。  
   - 备选：薄页外链 README（站内体验差）；全量铺开（易半成品）。

2. **IA = 保留指南 + 三端并列**  
   - 理由：与用户确认一致，新人仍从指南入门。  
   - 备选：仅三端分区；或按横切主题混排。

3. **发布 = 独立 `Jenkinsfile.docs` + 路径 `/docs/`**  
   - 理由：与现有 Job 拆分一致；`base` 已是 `/docs`。  
   - 备选：仅 shell 脚本；或独立子域名。

4. **产物路径以 `pnpm build` 实际输出为准写入 Pipeline**  
   - 理由：VitePress 版本/配置可能影响 dist 位置。  
   - 备选：写死 `.vitepress/dist` 不做校验（易踩坑）。

5. **内容来源以现网与三端 README 为准，不搬设计稿全文**  
   - 理由：避免与实现脱节；需要处外链 specs。

## Risks / Trade-offs

- [VitePress 产物路径不符假设] → 实现时先本地 build，再写 Archive/rsync 路径。  
- [Nginx `alias` + `try_files` 子路径刷新 404] → 对照 ui/h5 验证 `/docs/` 与深链。  
- [实用向文档不够细] → 大纲标明全量暂缓；后续可增量扩页。

## Migration Plan

1. 合并文档与 deploy 变更后，在 Jenkins 新建 docs Job，指向 `Jenkinsfile.docs`。  
2. 目标机 `mkdir -p /opt/quickboot/www/docs`，更新 Nginx 并 reload。  
3. 首次手动 deploy；用 `/docs/` 冒烟。  
4. Rollback：重新 deploy 上一成功分支/构建（本期无自动 rollback）。

## Open Questions

- 无阻塞项。目标机默认 `DEPLOY_DIR` 取 `/opt/quickboot/www/docs`（与设计一致）；若某环境目录习惯不同，由 Job 参数覆盖。
