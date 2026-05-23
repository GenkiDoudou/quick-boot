# Cursor Skills 详解

Skills 在对话开始时由 Agent **自动匹配**加载，避免每次手写长提示。

## 本项目常用 Skill

| Skill | 场景 |
|-------|------|
| `openspec-apply-change` | 按 OpenSpec tasks 实现 |
| `openspec-propose` | 一次性生成 proposal/design/tasks |
| `openspec-verify-change` | 实现与 spec 对齐检查 |
| `generate-frontend-code-spec` | 生成/更新前端规范 |
| `table-split` | DDL 按表拆分 |

路径：`.cursor/skills/`、`.codex/skills/`、`~/.cursor/skills-cursor/`

## 编写原则

- 写清**何时触发**与**步骤**，不要复制整本规范正文
- 单文件 SKILL.md，UTF-8 无 BOM

详见 Cursor 官方 create-skill 文档。
