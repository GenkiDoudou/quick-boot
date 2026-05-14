## ADDED Requirements

### Requirement: 角色分页列表与筛选

系统 SHALL 提供角色分页查询能力，支持按角色名称（模糊）、权限字符 `role_key`（模糊）、状态（精确）、创建时间区间筛选；列表默认排序以实现阶段与现有系统管理表一致（建议按 `role_sort` 升序再按 `role_id`）。成功响应 MUST 使用 `R`，业务载荷为 `PageInfo<SysRoleVo>`，包含 `records` 与 `total`。

#### Scenario: 按角色名称筛选

- **WHEN** 客户端使用合法分页参数调用 `GET /system/role/list` 并传入角色名称模糊条件
- **THEN** 系统返回的 `data.records` 仅包含名称匹配且未被逻辑删除的角色行，`data.total` 为总匹配条数

#### Scenario: 按创建时间区间筛选

- **WHEN** 客户端传入创建起止时间且区间内存在角色数据
- **THEN** 系统返回的记录创建时间均落在闭区间语义内（具体参数名以实现与 OpenAPI 为准）

### Requirement: 角色详情查询

系统 SHALL 提供按 `roleId` 查询角色详情的能力，响应 MUST 包含列表与表单所需字段（含 `data_scope`）；菜单勾选回显可依赖独立接口 `GET /system/menu/roleMenuTreeselect/{roleId}`，详情接口不强制返回 `menuIds`（以实现与前端约定为准，二者不得互相矛盾）。

#### Scenario: 查询存在的角色

- **WHEN** 客户端调用 `GET /system/role/{roleId}` 且角色存在且未删除
- **THEN** 系统返回 `R`，其 `data` 包含该角色完整展示字段

#### Scenario: 查询不存在或已删除角色

- **WHEN** 客户端请求的 `roleId` 不存在或已逻辑删除
- **THEN** 系统返回可识别的业务失败结果及明确中文说明（不得使用 `IllegalArgumentException` 作为业务信号）

### Requirement: 角色新增

系统 SHALL 提供 `POST /system/role/create` 新增角色。请求体 MUST 接入 Jakarta Validation：`role_name`、`role_key`、`role_sort` 为必填；`role_key` 在「未删除」数据范围内 MUST 全局唯一。

#### Scenario: 新增成功

- **WHEN** 客户端提交满足校验且 `role_key` 不与现存记录冲突
- **THEN** 系统持久化新角色并返回成功，`create_by`/`create_time` 按项目约定填充

#### Scenario: 权限字符重复被拒绝

- **WHEN** 客户端提交的 `role_key` 与已存在未删除记录重复
- **THEN** 系统拒绝保存并返回明确中文业务错误

### Requirement: 角色更新

系统 SHALL 提供 `POST /system/role/update` 更新角色。请求体 MUST 包含 `roleId` 并接入 Update 分组校验；`role_key` 唯一性规则 MUST 在更新场景排除自身主键后再判定。

#### Scenario: 更新成功

- **WHEN** 客户端提交合法更新请求且 `roleId` 存在、`role_key` 不与其他角色冲突
- **THEN** 系统更新字段并返回成功，`update_by`/`update_time` 按项目约定填充

### Requirement: 角色逻辑删除

系统 SHALL 提供 `POST /system/role/remove`，请求体为角色 ID 列表，支持批量逻辑删除。系统 MUST 拒绝删除 `role_id = 1`。系统 MUST 在角色仍被 `sys_user_role` 引用时拒绝删除并返回明确中文提示。

#### Scenario: 内置管理员不可删

- **WHEN** 客户端提交的删除列表包含 `1`
- **THEN** 系统不执行删除并返回业务错误说明内置角色不可删除

#### Scenario: 仍绑定用户不可删

- **WHEN** 客户端请求删除某角色且 `sys_user_role` 中仍存在该 `role_id`
- **THEN** 系统拒绝删除并提示需先解除用户关联

### Requirement: 角色状态变更

系统 SHALL 提供 `POST /system/role/changeStatus`，请求体包含 `roleId` 与目标 `status`。系统 MUST 拒绝非法 `roleId` 或不存在角色；对内置角色的状态是否允许变更以实现与设计文档一致（当前设计允许改状态，仅禁删与禁改菜单/数据权限）。

#### Scenario: 状态更新成功

- **WHEN** 客户端对非内置限制场景提交合法状态变更
- **THEN** 系统更新 `status` 字段并返回成功

### Requirement: 数据权限与自定义部门

系统 SHALL 提供 `POST /system/role/dataScope`，请求体包含 `roleId` 与 `dataScope`，当 `dataScope` 为 `2`（自定义）时 MUST 接受部门 ID 列表并写入 `sys_role_dept`；当 `dataScope` 不为 `2` 时 MUST 删除该角色在 `sys_role_dept` 中的全部行。合法取值 MUST 为 `1`、`2`、`3`、`4`、`5`，语义分别为：全部、自定义、本部门、本部门及以下、仅本人。系统 MUST 拒绝对 `role_id = 1` 的数据权限写入。

#### Scenario: 自定义部门保存

- **WHEN** 客户端对某非内置角色提交 `dataScope=2` 及非空 `deptIds`
- **THEN** 系统更新 `sys_role.data_scope` 且 `sys_role_dept` 中仅保留本次提交的部门关联

#### Scenario: 从自定义切换为全部

- **WHEN** 客户端将某角色从 `dataScope=2` 改为 `dataScope=1`
- **THEN** 系统更新 `data_scope` 且该角色在 `sys_role_dept` 中无任何行

#### Scenario: 内置角色拒绝改数据权限

- **WHEN** 客户端对 `roleId=1` 调用数据权限接口
- **THEN** 系统拒绝并返回明确中文说明

### Requirement: 菜单权限保存与读回显

系统 SHALL 提供 `POST /system/role/menu`，请求体包含 `roleId` 与 `menuIds`（菜单主键列表），并在事务内全量替换该角色的 `sys_role_menu` 关联。系统 MUST 拒绝对 `role_id = 1` 的菜单写入。

系统 MUST 继续支持通过 `GET /system/menu/roleMenuTreeselect/{roleId}` 获取菜单树及 `checkedKeys`；保存后客户端再次调用该读接口时，`checkedKeys` MUST 与最近一次保存的 `menuIds` 语义一致（顺序不要求一致）。

#### Scenario: 保存后树回显一致

- **WHEN** 客户端对某非内置角色调用菜单保存提交一组 `menuIds` 后，再次调用 `roleMenuTreeselect`
- **THEN** 返回的 `checkedKeys` 集合与保存的菜单 ID 集合相同

#### Scenario: 内置角色拒绝改菜单

- **WHEN** 客户端对 `roleId=1` 调用菜单保存
- **THEN** 系统拒绝并返回明确中文说明

### Requirement: 分配用户列表与授权变更

系统 SHALL 提供 `GET /system/role/authUser/allocatedList` 与 `GET /system/role/authUser/unallocatedList`，支持分页及必要筛选（如用户名/账号），且 MUST 以 `roleId` 区分已分配与未分配用户。系统 SHALL 提供 `POST /system/role/authUser/selectAll`（批量授权）、`POST /system/role/authUser/cancel`（单用户取消）、`POST /system/role/authUser/cancelAll`（批量取消），并维护 `sys_user_role` 表一致性。

#### Scenario: 批量授权后用户出现在已分配列表

- **WHEN** 客户端对某 `roleId` 提交一批尚未关联的用户 ID 调用批量授权
- **THEN** 系统在 `sys_user_role` 中为每对 `(user_id, role_id)` 建立关联，且这些用户出现在 `allocatedList` 中而不出现在 `unallocatedList` 中（在相同筛选条件下）

#### Scenario: 取消授权后关联移除

- **WHEN** 客户端对某用户调用取消授权
- **THEN** 对应 `sys_user_role` 行被删除，该用户在 `unallocatedList` 中可见

### Requirement: 角色列表导出

系统 SHALL 提供 `POST /system/role/export`，导出格式为 `xlsx`，筛选条件与列表查询语义一致（以实现约定 Query 或 Body 为准），列 MUST 覆盖列表主要字段。

#### Scenario: 导出文件可打开

- **WHEN** 客户端在具备 `system:role:export` 权限时调用导出且数据非空
- **THEN** 系统返回可下载的 Excel 文件流，文件可被常见表格软件打开且包含预期列头

### Requirement: 对外接口与文档约定

相关 Controller MUST 使用 `@Tag`、`@Operation`，关键参数使用 `@Parameter`。业务失败 MUST 使用项目统一业务异常体系，不得使用 `IllegalArgumentException` 作为业务失败信号。公开 Java 类型及 public 成员 MUST 具备 JavaDoc（简体中文为主）。

#### Scenario: OpenAPI 分组可见

- **WHEN** 开发者打开 Swagger/OpenAPI 中的角色管理分组
- **THEN** 能看到各接口摘要及主要参数说明

### Requirement: 管理端权限标识

系统 MUST 为角色管理能力配置权限标识（与菜单/Spring Security 配置一致），至少包含：`system:role:list`、`system:role:add`、`system:role:edit`、`system:role:remove`、`system:role:export`、`system:role:dataScope`。前端 MUST 使用 `v-hasPermi`（或项目等价指令）与上述字符串对齐。

#### Scenario: 无权限拒绝写操作

- **WHEN** 已认证用户缺少 `system:role:remove` 但调用删除接口
- **THEN** 系统拒绝请求（HTTP 403 或项目统一无权限响应）
