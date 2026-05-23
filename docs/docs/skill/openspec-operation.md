# OpenSpec 操作指南（速查）

## 目录结构

```text
openspec/changes/<change-id>/
  proposal.md
  design.md
  tasks.md
  specs/**/*.md
```

## 常用流程

```text
opsx-new → 填写需求 → opsx-continue（生成 design/specs/tasks）
→ opsx-apply → 编码 → opsx-verify → opsx-archive
```

## 与文档站关系

- 设计归档可复制到 `docs/superpowers/specs/`
- 用户手册更新 `docs/docs/backend|frontend/`

## 详细 SDD

[OpenSpec 详细指南（SDD）](../sdd/openspec)

仓库：`openspec/project.md`
