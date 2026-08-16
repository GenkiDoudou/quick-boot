## Why

quick-h5 工作台已有「用户」入口，但仅 toast 占位，无法做用户查询与维护。一期需要在 H5 侧打通与管理端一致的 `/sys/user/*` 常用能力，便于移动端运维。

## What Changes

- 新增 H5 用户列表页：按账号搜索、下拉刷新、上拉分页
- 新增 H5 用户表单页：新增 / 编辑（精简字段：账号、昵称、密码仅新增、手机、状态）
- 列表支持启停、重置密码（超管禁停用）
- 工作台「用户」菜单配置 `path` 并支持 `navigateTo`
- API 封装 `quick-h5/src/api/system/user.ts`，复用现有后端，不新增 H5 专用接口
- 不做：部门/角色、删除、导入导出、前端按钮级权限指令

## Capabilities

### New Capabilities

- `quick-h5-user-mgmt`: H5 用户管理一期（列表、表单、启停、重置密码、工作台入口）

### Modified Capabilities

- （无）本期不修改已归档主 specs 中的既有需求条文

## Impact

- 代码：`quick-h5`（`pages.json`、工作台、API、用户页）
- API：只读/写现有 `POST/GET /sys/user/*`（需登录用户具备 `system:user:*` 权限）
- 依赖：uView Pro、现有 `request()` / OAuth client
- 后端 / quick-ui：无强制改动
