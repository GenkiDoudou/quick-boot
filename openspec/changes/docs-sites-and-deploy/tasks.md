## 1. 导航与站点骨架

- [x] 1.1 更新 `docs/.vitepress/config/nav.ts`：指南 / 后端 / 管理端 / 移动端四个入口
- [x] 1.2 更新 `docs/.vitepress/config/sidebar.ts`：保留指南；新增 backend / frontend / h5 三组侧栏
- [x] 1.3 修正 `docs/index.md` 首页 features 坏链，指向真实文档页

## 2. 三端实用文档

- [x] 2.1 新增 `docs/docs/backend/` 四页（index / getting-started / structure / conventions），内容对齐现网 quickboot
- [x] 2.2 新增 `docs/docs/frontend/` 四页，内容对齐 quick-ui
- [x] 2.3 新增 `docs/docs/h5/` 四页，内容对齐 quick-h5

## 3. 指南与大纲修正

- [x] 3.1 修正 `docs/docs/guide/` 中过时端口、依赖与启动说明（含 introduction / quick-start / installation 等受影响页）
- [x] 3.2 更新 `capabilities-outline.md`：实用向已完成、全量分区暂缓，去掉虚假 ✅

## 4. docs 部署脚本

- [x] 4.1 本地 `cd docs && pnpm build`，确认产物目录路径
- [x] 4.2 新增 `deploy/jenkins/Jenkinsfile.docs`（对齐 quick-ui：build / rsync / smoke；rollback 不支持）
- [x] 4.3 更新 `deploy/nginx/quickboot.conf.example` 增加 `/docs/`
- [x] 4.4 更新 `deploy/env/README.md` 补充 `www/docs/` 与 docs Job 说明

## 5. 验收

- [x] 5.1 `cd docs && pnpm build` 成功；侧栏可打开三端全部新页
- [x] 5.2 对照验收清单核对指南准确性、Nginx/README/Jenkinsfile 齐全
