## ADDED Requirements

### Requirement: 菜单表与角色关联表持久化

系统 MUST 通过 Flyway 提供 **`sys_menu`** 表，支持目录/菜单/按钮类型及需求文档所列业务字段（含逻辑删除 `del_flag`、审计字段）；**`parent_id` 顶级 MUST 为 -1**（与 `sys_dept` 约定一致）。系统 MUST 提供 **`sys_role`** 最小表与 **`sys_role_menu(role_id, menu_id)`** 关联表，以支持按角色勾选菜单。索引 MUST 支持按 `parent_id` 与 `del_flag` 查询子菜单及按 `role_id` 查询关联菜单。

#### Scenario: 迁移在空库上可执行

- **WHEN** 在干净数据库上应用本变更所含 Flyway 版本
- **THEN** `sys_menu`、`sys_role`、`sys_role_menu` 表存在且主键/唯一约束定义完整

---

### Requirement: 菜单列表树与筛选剪枝

`GET /system/menu/list` MUST 成功返回 **`data` 为嵌套树根数组**；支持按 **菜单名称**（模糊）、**状态**（精确）筛选；剪枝语义 MUST 与部门列表一致：有筛选时保留命中节点及其祖先链，无匹配时 `data` 为空数组；无筛选时返回未逻辑删除的全树。树表展示所需字段（菜单名称、图标、排序、权限标识、组件路径、状态、创建时间等）MUST 在列表 VO 中可用。

#### Scenario: 无筛选返回全树

- **WHEN** 调用 `GET /system/menu/list` 且无筛选参数（或等价无筛选）
- **THEN** `data` 为从 `parent_id = -1` 可达的嵌套树，子节点缺失时 **`children` 为空数组**

#### Scenario: 有筛选时剪枝

- **WHEN** 调用 `GET /system/menu/list` 且带有非空菜单名称或状态条件
- **THEN** `data` 仅包含命中节点及其祖先，结构为嵌套树；无匹配时 `data` 为空数组

---

### Requirement: 菜单下拉树 `treeselect`

`GET /system/menu/treeselect` MUST 返回全量未删菜单树于 `data`，节点 MUST 至少包含 **`id`**（等于 `menu_id`）、**`label`**（菜单显示名）、**`children`**（空为数组），且 MUST **不受** 列表筛选 query 影响。

#### Scenario: 表单可选上级菜单

- **WHEN** 调用 `GET /system/menu/treeselect`
- **THEN** 返回树可被「上级菜单」树选择器绑定且具备 `id`/`label`/`children`

---

### Requirement: 角色菜单勾选树 `roleMenuTreeselect`

`GET /system/menu/roleMenuTreeselect/{roleId}` MUST 在角色存在时返回载荷，其中包含 **全量菜单树** 与 **该角色已勾选菜单 id 集合**（字段名与前端约定在实现阶段与 `tasks` 对齐常见若依形态，如 `menus` + `checkedKeys`）；**WHEN** `roleId` 不存在或角色已删，MUST 返回业务失败与可读 `msg`。

#### Scenario: 合法角色返回树与勾选

- **WHEN** 请求存在的 `roleId`
- **THEN** 响应成功且包含菜单树与已分配菜单 id 列表

#### Scenario: 非法角色失败

- **WHEN** 请求不存在或无效 `roleId`
- **THEN** 响应为业务失败且带明确 `msg`

---

### Requirement: 菜单详情

`GET /system/menu/{menuId}` MUST 返回单条菜单于 `data`，字段满足编辑回显（含类型、路由、组件、权限、图标、显示/状态、缓存、外链等）；**WHEN** 记录不存在或已逻辑删除，MUST 业务失败。

#### Scenario: 读取有效菜单

- **WHEN** `menuId` 对应未删除记录
- **THEN** 成功且 `data` 含 `menuId`

---

### Requirement: 新增、修改、删除菜单

系统 MUST 提供 **`POST /system/menu`**（或 `/create`）新增、**`POST /system/menu/update`** 修改、**`POST /system/menu/remove`** 删除（路径以 `tasks` 与既有 `SysConfigController` 风格对齐）；请求体 MUST 经 Jakarta Validation 校验。**MUST** 校验父节点存在且未删；**MUST** 禁止将父级设为自身；**MUST** 禁止改父后成环；**MUST** 按菜单类型校验字段组合（如按钮不要求路由/组件等，与已定稿设计一致）。

#### Scenario: 删除有子节点被拒绝

- **WHEN** 目标菜单存在未逻辑删除的子菜单
- **THEN** 删除接口返回业务失败且子菜单仍存在

#### Scenario: 删除无子节点成功

- **WHEN** 目标菜单无未删子节点
- **THEN** 删除成功且该菜单逻辑删除（或项目约定删除语义）

---

### Requirement: 动态路由 `getRouters`

`GET /getRouters` MUST 在已登录上下文中返回 **`data` 为路由数组**（形状与 `quick-ui` 现有 `filterAsyncRouter` 兼容），内容来自 **当前用户可见菜单**（经角色过滤）；**MUST** 替换此前恒为空列表的占位实现。

#### Scenario: 登录用户获得与角色匹配的菜单路由

- **WHEN** 已登录用户具备至少一个角色且角色已分配菜单
- **THEN** `getRouters` 的 `data` 为非空数组且每项具备前端动态注册所需关键字段（至少含 `path` 与 `meta` 或项目约定的等价结构）

#### Scenario: 未分配菜单时返回可接受结果

- **WHEN** 用户无菜单权限
- **THEN** `data` 为空数组且前端不因结构缺失而抛错

---

### Requirement: 权限标识与前端权限点

菜单数据中的权限字符串 MUST 可用于后端接口鉴权与前端 `v-hasPermi`；菜单管理页面对应权限点 MUST 包含 **`system:menu:list`**、**`system:menu:add`**、**`system:menu:edit`**、**`system:menu:remove`**（与需求文档 §5 一致）。

#### Scenario: 无 list 权限不可见列表操作

- **WHEN** 用户缺少 `system:menu:list`
- **THEN** 前端隐藏列表入口或后端拒绝列表接口（与项目统一鉴权策略一致）
