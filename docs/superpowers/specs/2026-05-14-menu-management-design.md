# 菜单管理设计文档

## 1. 背景与目标

在系统管理域内提供「菜单管理」能力：维护目录 / 菜单 / 按钮三类节点的树形数据，作为 **动态路由与权限标识** 的权威来源；并与登录后 **`GET /getRouters`** 打通，替换当前后端占位空列表。原始需求见 `原始需求/系统管理/菜单管理-需求文档.md`。本文档为经 `/brainstorming` 澄清与方案确认后的**定稿设计**；与原始需求不一致处（尤其 HTTP 方法、删除拦截范围、接口路径子资源）以本文为准。

**架构方案（已确认）**：方案一 — **菜单域单模块内聚**，由 `MenuService`（及必要的同包 VO 组装辅助）承担管理端树、`treeselect`、`roleMenuTreeselect`、CRUD 校验及 **动态路由 VO 构建**；`AuthController.getRouters` 仅作入口委托。

## 2. 需求澄清结论（已确认）

| 序号 | 结论 |
|------|------|
| 1 | 写操作 HTTP 形态对齐仓库约定：**新增/修改/删除统一 `POST` 子路径**（不使用 `PUT`/`DELETE`），与 `SysDeptController` / `SysConfigController` 风格一致；同步修订原始需求 §6 与前端 API。 |
| 2 | **同一迭代**打通 **`GET /getRouters`**：菜单表（及角色-菜单）为路由与权限的源数据，登录后按用户角色过滤返回若依兼容路由树。 |
| 3 | 纳入 **`GET /system/menu/roleMenuTreeselect/{roleId}`**；需具备真实 **`sys_role`** 与 **`sys_role_menu`**（及使当前用户能关联到角色的最小方案，见 §3.2 / §6）。 |
| 4 | 删除拦截（本迭代）：**仅**当存在未逻辑删除的**子节点**时拒绝；**不**校验「角色仍引用该菜单」。 |
| 5 | 树主键与父级：**`BIGINT` 主键**、**`parent_id` 顶级 = -1**（与 `sys_dept` 一致）。 |

## 3. 范围与非范围

### 3.1 实施范围

- 后端：`sys_menu`、`sys_role`、`sys_role_menu` 的 Flyway 迁移与领域模型；`MenuService`、`SysMenuController`；`getRouters` 委托实现；校验与项目统一异常。
- 前端：`quick-ui` 菜单管理路由与页面、API 模块、树表与表单（按类型显隐）、权限指令与 `system:menu:*` 对齐。
- 种子数据：至少内置角色、菜单管理相关菜单节点、角色-菜单关联，保证可验收列表与 `roleMenuTreeselect`。

### 3.2 明确不做（本迭代）

- 删除菜单时因 **角色仍引用** 而拦截（可后续变更扩展）。
- 完整的「用户管理 / 用户分配角色」界面（若用户表尚无 `user_role`，可采用 **最小 `sys_user_role` 或开发期默认绑定策略** 满足 `getRouters` 验收，见 §6 开放式问题落地后关闭）。

## 4. 数据模型

### 4.1 表 `sys_menu`（字段与 RuoYi 常见模型对齐，实现时可微调类型）

| 字段 | 类型（建议） | 说明 |
|------|----------------|------|
| `menu_id` | BIGINT PK | 与现有 `sys_*` 主键策略一致 |
| `parent_id` | BIGINT NOT NULL | 顶级 **-1** |
| `menu_type` | CHAR(1) | `M` 目录、`C` 菜单、`F` 按钮（或项目统一枚举，与前端字典一致） |
| `menu_name` | VARCHAR(50) | 显示名称 |
| `order_num` | INT | 排序 |
| `path` | VARCHAR(200) | 路由地址 |
| `component` | VARCHAR(255) | 组件路径，目录/按钮可空 |
| `query` | VARCHAR(255) | 可选，路由 query |
| `perms` | VARCHAR(100) | 权限标识 |
| `icon` | VARCHAR(100) | 图标 |
| `visible` | CHAR(1) | 是否显示（如 0 显示 1 隐藏） |
| `status` | CHAR(1) | 与 **`sys_normal_disable`** 或项目统一状态字典一致 |
| `is_frame` | CHAR(1) | 是否外链 |
| `is_cache` | CHAR(1) | 是否缓存 keep-alive |
| `remark` | VARCHAR(500) | 可选 |
| `del_flag` | CHAR(1) | 逻辑删除，与部门表语义一致 |
| 审计字段 | create_by / create_time / update_by / update_time | 与项目约定一致 |

索引建议：`(parent_id, del_flag)`；按需 `(status, del_flag)`。

### 4.2 表 `sys_role`（最小）

| 字段 | 说明 |
|------|------|
| `role_id` | BIGINT PK |
| `role_name`、`role_key` | 展示与唯一键 |
| `status`、`del_flag`、排序、备注、审计字段 | 与项目惯例一致 |

### 4.3 表 `sys_role_menu`

| 字段 | 说明 |
|------|------|
| `role_id`、`menu_id` | 联合主键或唯一约束 |

## 5. 接口契约

统一响应 **`R`**。下列路径为能力说明；**具体 POST 子路径**（`/create`、`/update`、`/remove` 或 `/remove/{id}`）在实现阶段与 `tasks.md` 对齐现有 `SysConfigController` / `SysDeptController` 风格并保持全仓一致。

| 能力 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 树形列表 | GET | `/system/menu/list` | Query：菜单名称（模糊）、状态（精确）；**成功 `data` 为嵌套树根数组**，`children` 无子为空数组 |
| 详情 | GET | `/system/menu/{menuId}` | 编辑回显 |
| 下拉树 | GET | `/system/menu/treeselect` | 全量树，`id`/`label`/`children`，**忽略**列表筛选条件 |
| 角色菜单树 | GET | `/system/menu/roleMenuTreeselect/{roleId}` | 全量树 + 已勾选 menuId 列表（字段名与前端联调确定） |
| 新增 | POST | `/system/menu` 或 `/system/menu/create` | Body 校验；类型与字段组合见 §6 |
| 修改 | POST | `/system/menu/update` | Body 含 `menuId` |
| 删除 | POST | `/system/menu/remove` 等 | 有未删子节点则失败 |
| 动态路由 | GET | `/getRouters` | 已登录；`data` 为前端 `filterAsyncRouter` 兼容的路由数组 |

**与原始需求文档 §6 的差异**：不以 `PUT`/`DELETE` 暴露修改与删除；`roleMenuTreeselect` 路径保持 REST 风格子资源。

## 6. 后端设计

### 6.1 分层与包结构

- 包路径：`io.github.genkidoudou.web.system.menu`（与 `dept`、`config` 并列）。
- 分层：`controller` / `service` / `service.impl` / `mapper` / `domain` / `dto`（SaveRequest、Query、Vo、TreeSelectVo 等）。

### 6.2 树与筛选

- 实现策略参考 **`DeptServiceImpl`**：加载未删全量 → 内存建树；有筛选时计算保留节点 id 集合并剪枝保留祖先链。
- **`treeselect`** 始终全量，不受列表 query 影响。

### 6.3 校验规则

- 父节点存在且未删除；`parent_id = -1` 表示顶级。
- 禁止父级为自身；修改父级时 **防环**（父级不得落在当前节点及其子孙 id 集合内）。
- **按 `menu_type` 显隐/必填**：例如按钮不要求 `component`/路由等（与需求 §4 一致）。
- 删除：**仅**子节点存在则拒绝；返回明确 **`msg`**（`WarningException` 或项目等价物）。

### 6.4 `getRouters` 与 VO

- 从 **当前用户 → 角色 → sys_role_menu → sys_menu** 得到可见菜单树；过滤停用、隐藏策略与若依语义对齐（隐藏目录是否仍参与路由由实现注释说明）。
- 输出字段需包含前端已有逻辑依赖项：`path`、`name`（若需）、`component`、`redirect`、`meta`（`title`、`icon`、`noCache` 等）、`children`、`hidden` 等；**与 `quick-ui/src/store/modules/permission.js` 中 `filterAsyncRouter` 对齐**，必要时单测或联调截图固化。

### 6.5 权限与会话

- 菜单行上 **`perms`** 汇总为权限字符串列表，供 **`getInfo`** 或全局鉴权使用（与现有 Sa-Token 用法衔接；若当前 `getInfo` 为占位，本迭代一并接好最小闭环）。

## 7. 前端设计

- 实现前读取 **`DESIGN.md`** 与 **`sdd/前端代码规范.md`**。
- 页面：筛选（菜单名称、状态）、`el-table` 树表（展开/折叠）、操作列新增/修改/删除；表单含需求 §4 全部字段，**按菜单类型切换显隐**。
- API：`quick-ui/src/api/system/menu.js`，与后端 POST 路径一致。
- 权限：`system:menu:list`、`system:menu:add`、`system:menu:edit`、`system:menu:remove`。
- **路由入口**：需保证首次登录后仍可通过种子菜单进入「菜单管理」页；若完全依赖动态路由，种子数据必须包含该页对应菜单与组件路径。

## 8. 验收标准（与原始需求 §7 对齐并细化）

- 树结构与父子关系正确；筛选剪枝语义正确。
- 菜单类型切换后表单字段联动正确。
- 删除有子时拦截，**提示明确**。
- `getRouters` 非占位，侧边栏与动态路由与库中数据一致（在选定用户-角色绑定策略下）。
- `roleMenuTreeselect` 对有效 `roleId` 返回树与勾选 id。

## 9. 测试建议

- Service：建树、无参全树、名称/状态剪枝、删子拦截、改父成环。
- 可选：固定种子数据下 `getRouters` JSON 结构断言。

## 10. 开放式问题（实现阶段必须收口）

- **用户与角色的绑定**：若尚无 `sys_user`/`sys_user_role`，本迭代采用 **最小 `sys_user_role` 表** 还是 **开发配置默认用户→管理员角色**；选定后更新 `openspec/changes/system-menu-management/design.md` 并完成任务 1.3。
