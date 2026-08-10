# 贡献指南

## 仓库约定

- 编码 / 库表 / 分层 / 命名：**UTF-8 无 BOM** 等细则见仓库根 `code_formater.md`（事实源）
- 协作流程（排障、Karpathy、Never）：仓库根 `AGENTS.md`
- 前端视觉：`DESIGN.md`
- 变更流程：`openspec/changes/<change-id>/`

## 功能开发流程（推荐）

1. 在 `openspec/changes/<change-id>/` 创建 proposal / design / tasks / specs  
2. 实现代码并与 spec 对齐  
3. 更新本文档站对应模块页（`docs/docs/backend|frontend/...`）  
4. 验证：`mvn -pl quickboot-app -am package`、`pnpm build:prod`

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
