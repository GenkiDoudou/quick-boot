## Context

See `proposal.md` for motivation.  
参考设计：`docs/superpowers/specs/2026-08-15-quick-h5-user-mgmt-design.md`。

当前 `quick-h5` 已具备登录、`request()`、工作台 mock 菜单；「用户」入口无页面。后端 `/sys/user/*` 与 quick-ui 已完整可用。

**实现期修正：** 后端 `SysUserVo.roleIds` 在 Add/Update 分组上 `@NotEmpty`，故新增须选角色；编辑静默回传详情中的 `roleIds`（界面可不展示）。

## Goals / Non-Goals

**Goals:**

- H5 侧完成用户列表 / 表单 / 启停 / 重置密码，数据与管理端同源
- 工作台可跳转到真实页面
- 交互适配小屏（独立表单页 + 列表行操作）

**Non-Goals:**

- 不新增后端 API 或 Flyway
- 不做部门树、删除、导入导出、完整角色授权页
- 不做 H5 按钮级权限指令（依赖后端 403）
- 不改造工作台菜单为后端下发（仍 mock + path）

## Decisions

1. **直连现有 `/sys/user/*`，而非 H5 专用 BFF**
2. **列表页 + 独立表单页；重置密码用弹层**
3. **角色处理（方案 1）**
   - 新增：表单增加精简角色多选（必填），数据来自 `POST /sys/role/page`
   - 编辑：不展示角色；提交时带回 `getUser` 的 `roleIds`
   - 仍不传 `deptId`
4. **分页约定对齐 `PageRequest`**：`{ current, size, param }`
5. **UI**：uView Pro + `.qb-page` 现有绿主色

## Risks / Trade-offs

- [角色列表需 `system:role:list`] → 无权限时 toast；新增无法完成
- [mock 菜单与真实权限不一致] → 无权限时 toast 后端文案
- [超管停用] → 前端禁停用 `userId === 1`

## Migration Plan

- 仅前端包变更；发布 `quick-h5` 即可
- 回滚：移除页面注册与菜单 path，恢复 toast

## Open Questions

- （无）
