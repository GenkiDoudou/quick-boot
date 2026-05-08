## Why

系统管理需要可维护的部门数据与树形展示能力；当前后端尚无部门持久化与接口，前端仅有 API 封装与部分系统页占位，无法完成「部门管理」闭环。在已定稿的 Superpowers 设计（`docs/superpowers/specs/2026-05-08-dept-management-design.md`）指导下落地本变更，可与现有认证与统一响应契约对齐并减少返工。

## What Changes

- 新增 Flyway 迁移：创建 **`sys_dept`** 表（含逻辑删除与审计字段约定），**不**在本变更中创建用户表。
- 新增后端：`/system/dept/list`（**成功 `data` 为嵌套树**，与常见若依扁平 list 不同）、`treeselect`、`GET/POST/PUT/DELETE` 等，与 `quick-ui/src/api/system/dept.js` 路径一致；删除时**仅**禁止「存在未删除子部门」，**不**校验部门下用户（延后至用户表含 `dept_id` 后另变更补齐）。
- 新增前端：部门管理路由页——筛选（部门名称、负责人、状态）+ `el-table` 树表 + 增删改查/查看；上级部门树来自 `treeselect`。
- **BREAKING（契约语义）**：`GET /system/dept/list` 的 **`data` 为树形根数组**（节点含 `children`，无子推荐空数组）；依赖「扁平 `rows`」的调用方不得再假定扁平行，应改用 `treeselect` 或其它专用接口。

## Capabilities

### New Capabilities

- `sys-dept`：部门数据模型、树形列表与剪枝筛选语义、`treeselect` 下拉树、CRUD 与删除子部门校验、与 `R.data` 及前端 `dept.js` 的契约；延后项（用户占用删除校验）在需求中单独标明。

### Modified Capabilities

- （无）本变更为新增业务能力，不修改 `openspec/specs/` 下既有规范文件中的需求条文。

## Impact

- **后端**：`quickboot-web`（Controller/Service/Mapper/Entity/DTO）、`db/migration` 新增版本脚本。
- **前端**：`quick-ui` 路由/菜单/新页面；继续复用 `src/api/system/dept.js`（必要时仅调整页面内对 `list` 返回形态的解析，**不**改变 URL）。
- **依赖**：MyBatis-Plus、Flyway、Sa-Token 权限占位；字典 `sys_normal_disable` 与用户页一致。
- **文档**：实现以 Superpowers 设计说明为单源细节；OpenSpec delta 以 `specs/sys-dept/spec.md` 固化可验收需求。
