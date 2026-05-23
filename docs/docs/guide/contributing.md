# 贡献指南

## 仓库约定

- 编码：**UTF-8 无 BOM**（见 `AGENTS.md`）
- 后端规范：`sdd/后端代码规范.md`
- 前端规范：`sdd/前端代码规范.md`、`DESIGN.md`
- 协作流程：`openspec/project.md`

## 功能开发流程（推荐）

1. 在 `openspec/changes/<change-id>/` 创建 proposal / design / tasks / specs  
2. 实现代码并与 spec 对齐  
3. 更新本文档站对应模块页（`docs/docs/backend|frontend/...`）  
4. 验证：`mvn -pl quickboot-web package`、`pnpm build:prod`

## 文档贡献

- 正文目录：`docs/docs/`
- 侧栏：`docs/.vitepress/config/sidebar.ts`
- 顶栏：`docs/.vitepress/config/nav.ts`
- 新增页面后务必注册侧栏，避免 404

## 提交前检查

- [ ] Flyway 脚本可重复执行
- [ ] 未提交 `.env` 中的生产密钥
- [ ] 列表页符合 C7JsonTable 模板
- [ ] 公共能力优先放入 `quickboot-common`

## 相关

- [能力文档大纲](./capabilities-outline)
- [开发规范（后端）](../backend/development-guide)
- [开发规范（前端）](../frontend/development-guide)
