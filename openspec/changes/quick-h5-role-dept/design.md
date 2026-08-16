## Context

See `proposal.md` for motivation.  
产品设计：`docs/superpowers/specs/2026-08-15-quick-h5-role-dept-design.md`。  
模式对齐已实现的用户管理（列表 + 独立表单、`request()`、工作台 path）。

## Goals / Non-Goals

**Goals:**

- H5 角色：分页列表、增改、启停
- H5 部门：树列表、上级选择、增改删
- 工作台可跳转；风格与用户管理一致

**Non-Goals:**

- 角色菜单权限、分配用户
- 导入导出、H5 BFF、按钮级权限指令
- 部门邮箱/备注（可选后补）

## Decisions

1. **直连现有 `/sys/role/*`、`/sys/dept/*`** — 与用户管理一致  
2. **列表 + 独立 form 页** — 小屏表单更稳  
3. **部门树**：`list` 返回树；前端缩进展平展示；上级用 `treeselect` 扁平可选列表，禁选自身（及可实现时禁选子孙）  
4. **角色 `roleId=1`**：禁停用；`roleKey` 编辑只读  
5. **`pages.json` 变更后须重启 dev** — 写入 README/任务验收提示

## Risks / Trade-offs

- [无 role/dept 权限] → toast 后端文案  
- [部门成环] → 至少禁选自身；尽量禁选子孙  
- [路由未刷新] → 验收强调重启 `pnpm dev:h5`

## Migration Plan

- 仅 `quick-h5` 前端；回滚移除页面注册与菜单 path

## Open Questions

- （无）
