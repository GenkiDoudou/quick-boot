# 角色管理设计文档

## 1. 背景与目标

在系统管理域内实现「角色」全生命周期与授权：分页查询与筛选、列表展示、状态切换（需确认）、新增/编辑、菜单权限（树形勾选，全选/全不选/父子联动）、数据权限（若依式范围 + 自定义部门）、单条与批量删除、分配用户（已分配/未分配流转）、导出角色列表。

原始需求见 `原始需求/系统管理/角色管理-需求文档.md`。本文档为经 `/brainstorming` 澄清后的**定稿设计**；与原始需求不一致处（尤其 HTTP 方法、用户表缺失前提下的落地方式）以本文为准。

## 2. 需求澄清结论（已确认）

| 序号 | 结论 |
|------|------|
| 1 | 写操作统一使用 **`POST`** 表达新增、修改、删除、改状态、数据权限、菜单保存、用户授权变更、导出；**读操作**使用 **`GET`**（与 `AGENTS.md` 一致，不对齐原始需求中的 `PUT`/`DELETE`）。 |
| 2 | 数据权限采用**若依式枚举** + **`sys_role_dept`** 存储自定义部门；非自定义范围时清空该角色的部门关联行。枚举取值见 §4.2。 |
| 3 | **分配用户**：角色侧提供已分配/未分配分页与批量授权/取消接口；用户主数据来自用户域；本迭代需具备 **`sys_user` 最小表**及分页查询以支撑验收（若与用户管理变更合并建表则单次迁移、避免重复）。 |
| 4 | 导出为 **`xlsx`**，使用 `quickboot-common` 既有 Excel 能力，与项目内其他导出风格一致。 |
| 5 | 前端在 **`quick-ui`** 新建页面，视觉与交互遵循仓库根目录 **`DESIGN.md`**；业务交互可参考 `原始需求/quick-ui/src/views/system/role/`。 |
| 6 | **内置超级管理员角色**（种子 `role_id = 1`）：**禁止删除**；**禁止修改其菜单权限与数据权限**（名称、排序、状态等非权限字段不在此硬限制内，与澄清阶段默认一致）。 |

## 3. 范围与非范围

### 3.1 实施范围

- 后端：扩展 `sys_role`、新增 `sys_role_dept`、Flyway 迁移；`sys_user` 最小表及查询（若尚不存在）；`io.github.genkidoudou.web.system.role`（或与 `dept`/`config` 并列命名习惯）下的 Controller / Service / Mapper / domain / dto（Bo、QueryBo、Vo、Excel 行对象）。
- 与 **`sys_role_menu`**、**`sys_user_role`** 的读写衔接；复用 **`MenuService`** 已有「角色菜单树 + 已勾选 menuId」读能力；菜单勾选结果保存由角色域事务写入 `sys_role_menu`（通过调用现有菜单侧能力或共享 Mapper，避免循环依赖：**RoleService → MenuService** 单向）。
- 权限标识与原始需求 §6 一致：`system:role:list`、`add`、`edit`、`remove`、`export`、`dataScope`；鉴权与前端 `v-hasPermi` 对齐。
- 前端：路由、菜单、API 模块、列表页、表单抽屉/弹窗、菜单树、数据权限弹窗、分配用户子页或抽屉、导出。

### 3.2 明确不做

- 行级数据权限在 SQL 中的自动拼接切面（若项目已有 DataScope 切面可后续衔接；本文仅定义**角色上配置的范围与部门**持久化与维护 UI）。
- 角色复制、角色分级继承、多租户隔离。
- 与原始需求文档 §7 **路径与方法字面一致**的 `PUT`/`DELETE` 对外契约（以本文 §5 为准）。

## 4. 数据模型

### 4.1 表 `sys_role`（扩展）

在现有 `V5__sys_menu.sql` 已建表基础上，由新迁移追加字段：

| 字段 | 类型（建议） | 说明 |
|------|----------------|------|
| `data_scope` | CHAR(1) NOT NULL DEFAULT '1' | 数据权限范围，取值见 §4.2 |

其余字段保持与现有一致：`role_id`、`role_name`、`role_key`、`role_sort`、`status`、`remark`、`del_flag`、审计字段等；`role_key` 唯一索引已存在。

### 4.2 数据权限枚举 `data_scope`

| 值 | 含义 |
|----|------|
| `1` | 全部数据权限 |
| `2` | 自定义（依赖 `sys_role_dept`） |
| `3` | 本部门 |
| `4` | 本部门及以下 |
| `5` | 仅本人 |

当 `data_scope != '2'` 时，服务端应删除该 `role_id` 在 `sys_role_dept` 中的全部行，避免脏数据。

### 4.3 表 `sys_role_dept`（新建）

| 字段 | 类型 | 说明 |
|------|------|------|
| `role_id` | BIGINT NOT NULL | FK 逻辑关联 `sys_role.role_id` |
| `dept_id` | BIGINT NOT NULL | 逻辑关联 `sys_dept.dept_id` |

主键：`(role_id, dept_id)`。仅当 `data_scope = '2'` 时维护；保存数据权限时采用**先删后插**或等价事务策略。

### 4.4 表 `sys_user`（本迭代最小集）

若仓库尚无 Flyway 定义的 `sys_user`，本变更新增最小表以支撑「分配用户」列表与关联，字段建议至少包含：

- `user_id`（PK）、登录账号、用户昵称、部门 id（可空）、状态、`del_flag`、审计字段。

具体列名与类型与项目既有用户管理规范（`sdd/数据库设计规范.md`）对齐；若用户管理模块已提供同表，则**不重复建表**，仅引用。

### 4.5 既有表

- **`sys_role_menu`**：角色与菜单多对多；菜单保存接口全量替换该角色的关联行（或差异更新，实现阶段选一种并保证幂等与性能可接受）。
- **`sys_user_role`**：用户与角色多对多；分配用户接口维护此表。

## 5. 接口契约

统一响应 `R`；分页列表使用 `PageInfo<T>`（与 `SysConfigController`、通知公告等一致），Query 参数包含 `pageNum`、`pageSize` 及筛选字段。

| 能力 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/system/role/list` | Query：角色名称、权限字符（`role_key`）、状态、创建时间区间；返回 `R<PageInfo<SysRoleVo>>` |
| 详情 | GET | `/system/role/{roleId}` | 含菜单 id 列表或独立菜单树接口回显（与前端约定二选一：详情中带 `menuIds` **或** 仅依赖菜单树 GET） |
| 新增 | POST | `/system/role/create` | Body：Bo；校验分组 Add；`role_key` 唯一（与 `SysConfigController` 子路径风格一致） |
| 修改 | POST | `/system/role/update` | Body：Bo，含 `roleId`；分组 Update |
| 删除 | POST | `/system/role/remove` | Body：`roleId` 列表；拦截 `role_id=1` |
| 状态 | POST | `/system/role/changeStatus` | Body：`roleId`、`status`；若需拦截内置角色则与产品一致（默认允许改状态，仅禁删与禁改菜单/数据权限） |
| 数据权限 | POST | `/system/role/dataScope` | Body：`roleId`、`dataScope`、可选 `deptIds`；`role_id=1` 拒绝 |
| 菜单保存 | POST | `/system/role/menu` | Body：`roleId`、`menuIds`；`role_id=1` 拒绝 |
| 角色菜单树（读） | GET | `/system/menu/roleMenuTreeselect/{roleId}` | 返回 `RoleMenuTreeselectVo`：全量树 + `checkedKeys`（`SysMenuController` 已实现） |
| 已分配用户 | GET | `/system/role/authUser/allocatedList` | Query：`roleId`、用户筛选、分页 |
| 未分配用户 | GET | `/system/role/authUser/unallocatedList` | 同上 |
| 批量授权 | POST | `/system/role/authUser/selectAll` | Body：`roleId`、用户 id 列表 |
| 取消授权 | POST | `/system/role/authUser/cancel` | Body：`roleId`、用户 id |
| 批量取消 | POST | `/system/role/authUser/cancelAll` | Body：`roleId`、用户 id 列表 |
| 导出 | POST | `/system/role/export` | Query 或 Body 与列表筛选一致；返回文件流 |

所有 Body 使用 Jakarta Validation；对外 OpenAPI `@Tag`、`@Operation`、`@Parameter` 齐全。

## 6. 后端设计

### 6.1 分层与包结构

- 推荐包路径：`io.github.genkidoudou.web.system.role`（与 `system.dept`、`system.config` 等并列）。
- 分层：`controller` / `service` / `service.impl` / `mapper` / `domain` / `dto`。

### 6.2 超级管理员规则

- `role_id == 1`：**不允许** `remove`；**不允许** `dataScope`、`menu` 写接口成功（返回业务异常码，文案清晰）。
- 其他写操作是否限制：按 §2 序号 6，**不强制**禁止改名/排序/状态；若后续需收紧，单开变更修订本文档。

### 6.3 业务校验

- `role_name`、`role_key`、`role_sort` 必填；`role_key` 全局唯一（含逻辑删除语义与项目 `del_flag` 规则一致）。
- 删除前可校验是否仍绑定用户（可选，按产品：强删关联或禁止删；默认建议：**仍绑定用户则禁止删除**并提示）。

### 6.4 异常与日志

- 禁止用 `IllegalArgumentException` 表达业务失败；使用项目统一业务异常与 `ErrorCodes`。
- 关键写操作记录操作人（审计字段由现有 MetaObjectHandler 填充）。

## 7. 前端设计

- 路由：`/system/role`（与 `quick-ui` 路由表、后台菜单配置一致）。
- 列表：查询区（角色名称、`role_key`、状态、创建时间范围）、`C7JsonTable` 或项目统一表格组件、状态开关 + 二次确认、操作列（编辑、数据权限、分配用户、删除）。
- 新增/编辑：抽屉或对话框；校验与 §6.3 一致。
- 菜单权限：Element Plus 树，`check-strictly` 与父子联动行为与需求「全选/全不选/父子联动」一致（实现时固定一套配置并写入组件注释）。
- 数据权限：单选 `data_scope`；值为自定义时展示部门树多选，提交 `deptIds`。
- 分配用户：内嵌表格双列表 + 批量按钮，调用 §5 分配用户相关接口。
- 样式与间距遵循 `DESIGN.md`（主色 `#0a2463`、背景 `#f5f7fa`、高亮 `#409eff` 等）。

## 8. 测试与验收

- **权限树**：保存菜单后再次进入，`checkedKeys` 与树勾选一致。
- **数据权限**：切换 `data_scope` 时 `sys_role_dept` 与枚举一致；自定义部门保存与回显正确。
- **分配用户**：已分配/未分配列表与批量操作后 `sys_user_role` 一致。
- **导出**：文件可打开，列与列表主要字段一致。
- **内置角色**：删除与菜单/数据权限修改被拒绝。

单元测试优先：`role_key` 唯一、`role_id=1` 保护、数据权限切换清理自定义部门行；集成测试视 CI 情况选做。

## 9. 文档与实现衔接

- 实现前须阅读：`openspec/project.md`、`sdd/后端代码规范.md`、`sdd/前端代码规范.md`、`sdd/数据库设计规范.md`。
- 定稿评审通过后，使用 **`writing-plans`** 产出实现任务清单；**不在本文档批准前**启动编码。

## 10. 修订记录

| 日期 | 说明 |
|------|------|
| 2026-05-14 | 初版定稿（头脑风暴澄清与方案 1：独立 role 包） |
