# Cursor Commands 详解

命令位于 `.cursor/commands/`，在聊天框输入 `/命令名` 触发。

## OpenSpec（opsx-*）

| 命令 | 作用 |
|------|------|
| `opsx-new` | 新建变更 |
| `opsx-continue` | 继续下一产物 |
| `opsx-apply` | 按 tasks 实现 |
| `opsx-verify` | 验证实现 |
| `opsx-archive` | 归档变更 |
| `opsx-propose` | 快速生成全套产物 |
| `opsx-explore` | 探索模式 |

## 使用注意

- 先 `openspec init`（若未初始化）见项目 Skill `openspec-init`
- 变更目录：`openspec/changes/<change-id>/`

完整说明见各命令 md 文件正文。
