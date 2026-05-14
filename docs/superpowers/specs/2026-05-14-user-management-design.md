# 用户管理设计文档

## 1. 背景与目标

在系统管理域内实现「用户」全生命周期管理：多条件分页查询、列表展示（含部门与角色聚合展示）、多选/单选控制批量操作、状态开关（切换前二次确认）、新增/编辑（弹窗）、单条与批量删除、重置密码、**独立子路由「分配角色」**（勾选角色后保存）、Excel 导入（支持按用户名更新已存在用户）、按当前筛选导出、导入模板下载；错误提示中文、文案清晰。

原始需求见 `原始需求/系统管理/用户管理-需求文档.md`。本文档为经 `/brainstorming` 澄清并确认后的**定稿设计**；与原始需求不一致处（尤其 HTTP 方法、路径字面、岗位能力）以本文为准。

## 2. 需求澄清结论（已确认）

| 序号 | 结论 |
|------|------|
| 1 | 写操作统一使用 **`POST`**；读操作使用 **`GET`**（与 `AGENTS.md`、《2026-05-14-role-management-design》一致，不对齐原始需求 §7 的 `PUT`/`DELETE`）。路径前缀 **`/system/user`**，子路径命名风格与 `SysRoleController` 的 `create` / `update` / `remove` 等一致。 |
| 2 | **本迭代不做岗位**：表单、数据模型、导入导出模板均**不包含**岗位字段。 |
| 3 | **分配角色**为**独立子路由页面**（非抽屉内完成）；从用户列表进入，保存后返回列表。 |
| 4 | 导入时「更新已存在用户」按 **`user_name`** 与库表唯一约束 `uk_sys_user_name` 一致匹配。 |
| 5 | **内置超级管理员用户**（种子 **`user_id = 1`**，当前迁移中为 `admin`）：**禁止删除**；**禁止修改 `user_name`**；**禁止将 `status` 置为停用**；其余字段（昵称、部门、手机、邮箱、性别、备注、**角色**等）允许按权限编辑。 |
| 6 | 范围仅限**系统管理—用户管理**前后端、菜单权限种子与验收标准；**不包含**个人中心 profile 大改造；**不包含**列表行级数据权限 SQL 切面（若项目已有切面可后续衔接）。 |
| 7 | **新增与编辑用户**：须**至少选择一个角色**（`roleIds` 非空）；校验失败返回明确业务异常文案。 |

## 3. 范围与非范围

### 3.1 实施范围

- 后端：`io.github.genkidoudou.web.system.user` 包下 Controller / Service / Mapper（`SysUserMapper` 已存在可复用）/ 已有 `SysUser` 实体；补充 Bo、QueryBo、Vo、导入导出 Excel 行对象等 DTO。
- 与 **`sys_user`**、**`sys_user_role`**、**`sys_role`**（只读校验）、**`sys_dept`**（部门名称与树筛选）衔接。
- 权限标识与原始需求 §6 一致：`system:user:list`、`add`、`edit`、`remove`、`export`、`import`、`resetPwd`；分配角色与修改共用 **`system:user:edit`**（与原始需求一致）；鉴权与前端 `v-hasPermi` 对齐。
- Flyway：补充**用户管理**菜单及按钮（`perms` 如上），并为 **`role_id = 1`** 绑定新菜单 id（与 `V6` 角色菜单迁移方式一致）；菜单 id 取值在实现阶段分配，避免与现有 `sys_menu` 主键冲突。
- 前端：`quick-ui` 列表页、新增/编辑弹窗、分配角色子页、统一 `api/system/user.js` 指向 **`/system/user/*`**；视觉与交互遵循仓库根目录 **`DESIGN.md`**。

### 3.2 明确不做

- 岗位、用户-岗位关联表与相关 UI。
- 与原始需求 §7 **路径与方法字面完全一致**的对外契约。
- 个人中心（头像、自主改密等）在本变更中一并重构（已有接口可继续独立演进，不在本文验收范围内）。

## 4. 数据模型与查询约定

### 4.1 表 `sys_user`

沿用现有 Flyway 与实体字段（含 `user_id`、`dept_id`、`user_name`、`nick_name`、`email`、`phonenumber`、`sex`、`password`、`status`、`del_flag`、备注与审计字段等）。**不新增**岗位列。

### 4.2 表 `sys_user_role`

用户与角色多对多；以下写路径须在实现中**逻辑收敛**，避免与角色域「按角色批量授权用户」长期分叉：

- **用户侧**：`POST /system/user/authRole` 全量替换该 `user_id` 下的角色关联（事务：先删后插或等价策略）。
- **角色侧**：已有 `POST /system/role/authUser/*` 系列仍维护同一表；实现时推荐抽取**单一写入辅助方法**（同模块内私有或极薄共享层），两处仅作为入口，不复制两套不一致的规则。

### 4.3 列表与部门筛选

- 列表需展示**部门名称**：关联 `sys_dept`（按 `dept_id`）解析；`dept_id` 可为空时部门列展示约定为空或「未分配」，实现时写死一种并在本文保持一致。
- **部门树筛选**：选中节点 `deptId` 时，筛选 **`sys_user.dept_id` 落在该节点及其全部子孙部门**范围内的用户。当前 `sys_dept` 无 `ancestors` 字段，**在服务端基于全量部门列表计算子孙 `dept_id` 集合**再拼 `IN` 条件（与 `DeptServiceImpl` 树构建思路一致，避免 N+1）。

### 4.4 角色展示

列表「角色」列：通过 `sys_user_role` 关联 `sys_role` 聚合为 `roleNames`（或等价字段）；实现可选单次 join 或二次查询，以可读性与性能权衡为准。

## 5. 接口契约

统一响应 `R`；分页列表使用 `PageInfo<T>`；Query 含 `pageNum`、`pageSize` 及筛选字段；Body 使用 Jakarta Validation；对外 `@Tag`、`@Operation`、`@Parameter` 齐全；禁止用 `IllegalArgumentException` 表达业务失败。

| 能力 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 分页列表 | GET | `/system/user/list` | `system:user:list` |
| 详情 | GET | `/system/user/{userId}` | `system:user:list` |
| 新增 | POST | `/system/user/create` | `system:user:add` |
| 修改 | POST | `/system/user/update` | `system:user:edit` |
| 删除（批量） | POST | `/system/user/remove` | `system:user:remove` |
| 改状态 | POST | `/system/user/changeStatus` | `system:user:edit` |
| 重置密码 | POST | `/system/user/resetPwd` | `system:user:resetPwd` |
| 分配角色-回显 | GET | `/system/user/authRole/{userId}` | `system:user:edit` |
| 分配角色-保存 | POST | `/system/user/authRole` | `system:user:edit` |
| 导入 | POST | `/system/user/importData` | `system:user:import` |
| 导入模板 | GET | `/system/user/importTemplate` | `system:user:import` |
| 导入失败明细下载 | GET | `/system/user/importError` | `system:user:import` |
| 导出 | POST | `/system/user/export` | `system:user:export` |

**说明**

- **查询条件**（列表 GET）：用户名模糊、手机号模糊、状态、创建时间区间起止、`deptId`（树选中，含子孙，见 §4.3）。
- **重置密码**：Body 含 `userId` 与**新密码**（明文由 HTTPS 保护；服务端按登录模块相同算法加密存储）；内置用户 `user_id=1` 允许重置密码（不在 §2「禁止停用/改用户名」限制内）。
- **改状态**：Body 含 `userId`、`status`；内置用户禁止改为停用态。
- **导入**：`multipart/form-data` 上传文件 + 参数 `updateSupport`（布尔）；`updateSupport=true` 时按 `user_name` 命中则更新（遵守内置用户字段限制），未命中则插入。**响应 JSON** 含：总条数、成功条数、失败条数、失败摘要列表（便于 toast）；若 `失败条数 > 0`，响应内携带**短时有效**的 `errorKey`（或等价字段），前端再调 **`GET /system/user/importError?errorKey=`** 下载失败行 xlsx（服务端可内存/缓存限时存储，过期返回明确错误码）。
- **导出**：与列表筛选条件一致，文件格式 **xlsx**，使用 `quickboot-common` 既有 Excel 能力，与角色导出风格一致。

## 6. 后端设计

### 6.1 分层与包结构

- 包路径：`io.github.genkidoudou.web.system.user`（与 `system.dept`、`system.role`、`system.config` 并列）。
- 分层：`controller` / `service` / `service.impl` / `mapper` / `domain` / `dto`（含 Bo、QueryBo、Vo、Excel 行对象、导入结果 VO）。

### 6.2 内置用户 `user_id = 1`

- `remove`：包含 `1` 则拒绝（可整单拒绝或跳过 `1` 并提示，实现选一种并在接口文档注明；**推荐整单拒绝**若列表含 `1`）。
- `update` / `changeStatus`：拦截改 `user_name`、将 `status` 改为停用。
- 其余字段与角色分配在通过校验前提下允许。

### 6.3 业务校验（摘要）

- `user_name` 全局唯一（与逻辑删除语义及 `del_flag` 规则一致）。
- 新增：`password` 必填；**`roleIds` 至少 1 个**；`deptId` 必填（与原始需求一致；若业务允许未分配部门，须另开变更修订本文）。
- 编辑：`roleIds` 至少 1 个；密码非必填（不改则不带或空规则由实现约定）。
- 手机、邮箱格式校验；密码强度/长度与项目统一策略一致。
- `roleIds` 中每个 id 须在 `sys_role` 存在且未删除；`deptId` 须在 `sys_dept` 存在。

### 6.4 依赖方向

- `SysUserService` 可依赖 `DeptService`（树筛选 id 集合、部门存在性）、对 `sys_role` 的只读查询。
- **避免** `UserService` ↔ `RoleService` 双向循环；角色写入用户关联时若需校验用户存在，保持 **Role → UserMapper 只读** 或已有模式。

### 6.5 异常与审计

- 使用项目统一业务异常与 `ErrorCodes`；关键写操作审计字段由现有 MetaObjectHandler 填充。

## 7. 前端设计

- **路由**：主列表 `/system/user`（与动态路由、后台菜单 `path`/`component` 一致）；分配角色子路由例如 `system/user/auth-role`（具体 `path` 与 `route_name` 与 `router` 表结构对齐），通过 `query` 或动态参数携带 `userId`。
- **列表**：查询区（用户名、手机、状态、创建时间范围、部门树 `treeselect`）；回车查询、重置；表格多选；状态 `el-switch` 变更前 `MessageBox` 二次确认；操作列：修改、删除、重置密码、分配角色等按权限显示。
- **新增/编辑**：弹窗组件；无岗位字段；角色多选；提交前前端校验至少一角色。
- **分配角色页**：展示可选角色列表（或分组）与已勾选；保存后返回用户列表并刷新。
- **导入导出**：导入选文件 + `updateSupport` 勾选；结果展示总数/成功/失败；失败时提供下载失败明细按钮（消费 `errorKey`）。导出按钮调用 `POST /system/user/export`。
- **API**：`quick-ui/src/api/system/user.js` **全部**改为本文 §5 路径；删除对 `/sys/user/*` 等与定稿不一致的调用。
- 样式与间距遵循 **`DESIGN.md`**。

## 8. 测试与验收

- 所有按钮与接口与 §2、§5 权限点一致；未授权按钮不可见；接口无权限返回与全局约定一致。
- 新增/编辑/删/改状态/重置密码/分配角色/导入导出后，列表数据与库表一致；**新增/编辑无角色时前后端均拒绝**。
- 内置用户：`user_id=1` 删除与禁用、改用户名被拒绝；其他允许字段修改成功。
- 导入：`updateSupport` 开/关行为与 §5 一致；失败条数与下载内容一致。
- 导出：xlsx 可打开，列与列表主要字段一致（无岗位列）。

单元测试优先：内置用户规则、`user_name` 唯一、`roleIds` 非空、部门树筛选 id 集合；集成测试视 CI 选做。

## 9. 文档与实现衔接

- 实现前须阅读：`openspec/project.md`、`sdd/后端代码规范.md`、`sdd/前端代码规范.md`、`sdd/数据库设计规范.md`、根目录 **`DESIGN.md`**。
- 定稿评审通过后，使用 **`writing-plans`** 产出实现任务清单；**不在本文档批准前**启动业务编码。

## 10. 修订记录

| 日期 | 说明 |
|------|------|
| 2026-05-14 | 初版定稿（头脑风暴澄清：HTTP 契约、无岗位、子路由分配角色、导入键、内置用户规则、范围边界；确认新增/编辑须至少一角色） |
