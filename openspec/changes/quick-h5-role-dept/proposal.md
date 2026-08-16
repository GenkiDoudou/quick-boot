## Why

quick-h5 工作台「角色」「部门」仍为占位 toast，无法在移动端做常用维护。需在用户管理一期之后，补齐同档 H5 能力并复用现有后端接口。

## What Changes

- 新增 H5 角色列表/表单：搜索分页、新增编辑、启停（`roleId=1` 限制）
- 新增 H5 部门树列表/表单：上级选择、新增编辑、删除确认
- 工作台菜单为角色、部门配置 `path` 并跳转
- API：扩展 `role.ts`，新增 `dept.ts`；直连 `/sys/role/*`、`/sys/dept/*`
- 不做：菜单权限、分配用户、导入导出

## Capabilities

### New Capabilities

- `quick-h5-role-dept`: H5 角色管理与部门管理一期常用操作

### Modified Capabilities

- （无）

## Impact

- 代码：仅 `quick-h5`（pages、API、工作台 mock）
- API：现有 system 角色/部门接口（需对应 `system:role:*` / `system:dept:*` 权限）
- 后端 / quick-ui：无强制改动
- 参考：`docs/superpowers/specs/2026-08-15-quick-h5-role-dept-design.md`
