## Why

用户管理页尚不能按部门筛选列表；后端对 `deptId` 仅精确匹配，不符合「选上级部门应含下级用户」的常见组织筛选习惯。部门维护已有独立页面，本变更补齐用户侧筛选与含下级语义。

## What Changes

- 用户管理页搜索区增加「归属部门」树选择（`el-tree-select`），可清空/重置。
- 用户分页与导出：传入 `deptId` 时，查询范围改为**本部门及全部子孙部门**（**BREAKING** 相对原 `eq` 精确匹配；此前前端搜索未暴露该字段）。
- 部门管理页、用户表单选部门、API 路径与权限码不变；不加 `ancestors`、不做数据权限。

## Capabilities

### New Capabilities

- `sys-user-dept-filter`: 用户列表/导出按归属部门筛选，且含下级部门用户；前端搜索树选与后端 ID 展开约定。

### Modified Capabilities

- （无；主库 `openspec/specs/` 尚无已归档的 `sys-user-mgmt` 要求可改。）

## Impact

- 前端：`quick-ui/src/views/system/user/index.vue`
- 后端：`SysUserServiceImpl` 的 `page` / `listForExport`（及共享的部门 ID 展开逻辑）
- API：仍传单个 `deptId`，无新端点
- 库表 / 部门模块 / `C7JsonTable` 公共组件：无改动
