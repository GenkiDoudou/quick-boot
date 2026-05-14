## Why

系统需要可维护的菜单与权限元数据，并与前端动态路由、按钮鉴权对齐；当前 `getRouters` 仍为占位空列表，且无 `sys_menu` 持久化与 `/system/menu` 管理接口，无法完成「菜单管理」闭环。在已定稿 Superpowers 设计（`docs/superpowers/specs/2026-05-14-menu-management-design.md`）与方案一（菜单域单模块内聚）指导下落地本变更，可与部门管理等既有树形模块模式对齐并减少返工。

## What Changes

- 新增 Flyway：创建 **`sys_menu`**（逻辑删除、审计字段、`parent_id` 顶级 **-1**）；为支持 **`roleMenuTreeselect`** 与后续角色分配，同迭代引入最小 **`sys_role`** 与 **`sys_role_menu`**（联合唯一/主键策略见设计文档）；种子数据含内置角色与菜单管理相关菜单节点，保证可登录验收。
- 新增后端：`MenuService` 内聚管理端树、剪枝查询、`treeselect`、**`roleMenuTreeselect/{roleId}`**、CRUD 校验；**`AuthController.getRouters` 委托菜单域**从库按当前用户角色组装若依式路由 JSON，**替换占位空列表**。
- **HTTP 契约**：与仓库约定一致——新增/修改/删除使用 **`POST` 子路径**（不使用 `PUT`/`DELETE`）；与原始需求文档 §6 差异在 delta spec 与 Superpowers 设计中显式记录。
- 删除规则（本迭代）：**仅**当存在未删除子节点时拒绝；**不**校验「角色仍引用该菜单」（后续变更可扩展）。
- 新增前端：系统管理「菜单管理」页（筛选、树表、表单按类型显隐）、`src/api/system/menu.js`、权限点 **`system:menu:list/add/edit/remove`**。
- **BREAKING**：`GET /getRouters` 成功时 **`data` 由恒为空数组变为按库与权限过滤的路由树**；依赖「无动态路由」的调用方或本地假数据需迁移。

## Capabilities

### New Capabilities

- `sys-menu`：菜单表模型、树形列表与剪枝、`treeselect`、角色菜单勾选树、CRUD 与删除子节点校验、与 `getRouters` 的路由 VO 组装及与 `quick-ui` 动态路由契约对齐。

### Modified Capabilities

- （无）`openspec/specs/` 下尚无已归档的菜单或认证路由规范条目；本变更为新增业务能力。

## Impact

- **后端**：`quickboot-web`（`system.menu` 包、Flyway、Mapper/Entity/DTO/VO）、`AuthController` 的 `getRouters` 行为；Sa-Token 侧需能解析「当前用户 → 角色 → 菜单/权限」以填充会话权限（具体实现见 `design.md`）。
- **前端**：`quick-ui` 路由/菜单入口、新页面与 API；`permission.js` 对 `getRouters` 返回结构的兼容性验证。
- **依赖**：MyBatis-Plus、Flyway、与部门管理类似的建树模式；**角色表最小落地**为 `roleMenuTreeselect` 提供有效 `roleId`。
- **文档**：实现细节以 Superpowers 设计为单源；OpenSpec delta 以 `specs/sys-menu/spec.md` 固化可验收需求。
