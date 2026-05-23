# Quick-Boot 与 Cursor 工作流

## 推荐节奏

1. **澄清需求**：大功能先 `/brainstorming` 或 OpenSpec `proposal`
2. **读规范**：`openspec/project.md`、`sdd/*`、`DESIGN.md`
3. **实现**：小步提交，对齐 `tasks.md`
4. **验证**：`mvn package`、`pnpm build:prod`
5. **文档**：更新 `docs/docs` 对应模块页

## 生成代码时

- 列表页参照 `quick-ui/src/views/system/config/index.vue`
- 后端遵循 `R` + `PostMapping` + Flyway
- 必读 `AGENTS.md` 中的规范源文件

## OpenSpec 命令

见 [OpenSpec 操作指南](./openspec-operation) 与仓库 `.cursor/commands/opsx-*.md`
