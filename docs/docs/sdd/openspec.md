# OpenSpec 详细指南（SDD）

## 是什么

OpenSpec 在本仓库用于管理**可评审的变更**：proposal → design → specs → tasks → 实现 → verify → archive。

## 变更 ID

使用 kebab-case，如 `add-oauth2-integration`。

## 产物说明

| 文件 | 内容 |
|------|------|
| `proposal.md` | 为什么做、做什么 |
| `design.md` | 技术方案、权衡 |
| `tasks.md` | 可勾选任务清单 |
| `specs/**/spec.md` | 可测试的需求条目 |

## CLI

```bash
openspec init    # 初始化（见 openspec-init Skill）
openspec update
```

## 实现约束

实现必须与 **delta spec** 一致；完成后运行 verify  skill 或 `opsx-verify`。

## 延伸阅读

- [操作速查](../skill/openspec-operation)
- 仓库 `openspec/project.md`
