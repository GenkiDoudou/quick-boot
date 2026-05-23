# Spec-Kit 安装与初始化

## 前置

- Node.js 18+
- 仓库已克隆

## OpenSpec 初始化

```bash
# 在仓库根目录
openspec init
openspec update
```

或使用 Cursor Command / Skill：`openspec-init`（见 `.cursor/skills/openspec-init`）。

## 验证

```bash
openspec list
ls openspec/changes
```

## 与 Spec-Kit 关系

本仓库以 **OpenSpec** 为主；若同时使用 GitHub spec-kit，保持变更 ID 与文档路径一致，避免双份冲突。

## 相关

[新功能与需求变更](./spec-kit-change-workflow)
