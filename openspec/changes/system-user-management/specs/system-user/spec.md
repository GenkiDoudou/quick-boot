## ADDED Requirements

### Requirement: 用户管理 REST 契约与鉴权

系统 SHALL 暴露前缀为 `/system/user` 的 REST 接口；除导出与导入相关文件流外，**读操作使用 HTTP GET，写操作使用 HTTP POST**（与 `AGENTS.md` 及《2026-05-14-user-management-design》一致）。每个接口 SHALL 使用 `SaCheckPermission` 绑定设计文档规定的 `system:user:*` 权限标识；响应统一为 `R`，分页列表返回 `PageInfo`。对外 SHALL 具备 OpenAPI `@Tag`、`@Operation`、关键 `@Parameter`；请求体 SHALL 使用 Jakarta Validation；业务失败 SHALL 使用项目统一业务异常，禁止以 `IllegalArgumentException` 作为业务失败信号。

#### Scenario: 列表接口使用 GET 与 list 权限

- **WHEN** 客户端以 GET 调用 `/system/user/list` 且用户持有 `system:user:list`
- **THEN** 服务端返回 200 及分页数据结构

#### Scenario: 无写方法字面的删除

- **WHEN** 客户端尝试以 DELETE 调用用户删除语义
- **THEN** 服务端不提供该契约；删除 SHALL 仅通过 `POST /system/user/remove` 完成

### Requirement: 用户分页列表与筛选

系统 SHALL 支持按用户名（模糊）、手机号（模糊）、状态、创建时间区间、`deptId`（部门树选中节点及其**全部子孙**部门）筛选用户分页列表。列表项 SHALL 包含用户编号、用户名、昵称、部门名称、手机号、状态、创建时间、角色聚合展示字段（如 `roleNames`）。`dept_id` 为空时部门列展示 SHALL 在实现中固定为一种约定（空字符串或固定文案「未分配」）并在全站用户列表保持一致。

#### Scenario: 部门树含子孙筛选

- **WHEN** 查询参数包含某非叶子部门的 `deptId`
- **THEN** 结果集包含 `dept_id` 等于该部门及其任意子孙部门的用户

#### Scenario: 回车或按钮触发查询

- **WHEN** 前端在查询输入框按下回车或点击查询
- **THEN** 使用当前筛选条件请求列表并刷新表格

### Requirement: 用户详情

系统 SHALL 提供 `GET /system/user/{userId}` 返回用户详情（含回显所需字段及角色 id 列表等），且须校验 `userId` 有效；权限为 `system:user:list`。

#### Scenario: 有效用户返回详情

- **WHEN** 请求存在的 `userId`
- **THEN** 返回该用户完整回显字段

### Requirement: 新增用户

系统 SHALL 提供 `POST /system/user/create` 创建用户。请求 SHALL 包含：`userName`、`nickName`、`deptId`（必填）、`password`（必填）、`roleIds`（至少一个元素）、以及设计文档允许的其它字段（手机、邮箱、性别、状态、备注等，无岗位）。服务端 SHALL 校验 `user_name` 全局唯一（与逻辑删除规则一致）、`roleIds` 每个 id 对应存在且有效的 `sys_role`、`deptId` 对应存在部门；密码 SHALL 按项目统一策略校验强度/长度并加密存储。

#### Scenario: 无角色则拒绝

- **WHEN** `roleIds` 为空或未传
- **THEN** 请求失败并返回明确中文业务错误

#### Scenario: 创建成功

- **WHEN** 请求满足全部校验
- **THEN** 持久化 `sys_user` 与 `sys_user_role` 关联，并返回成功

### Requirement: 修改用户

系统 SHALL 提供 `POST /system/user/update` 修改用户。`roleIds` SHALL 至少包含一个元素。密码字段为可选（不传或空表示不修改密码，具体由实现约定并在接口文档一致描述）。其它校验与新增一致（除密码必填外）。

#### Scenario: 内置用户禁止改用户名

- **WHEN** 目标 `user_id` 为 `1` 且请求体尝试修改 `userName`
- **THEN** 请求失败并返回明确中文业务错误

### Requirement: 批量删除用户

系统 SHALL 提供 `POST /system/user/remove` 接受用户 id 列表并逻辑删除（与项目 `del_flag` 语义一致）。当请求 id 列表**包含** `user_id == 1` 时，SHALL **整单拒绝**并返回明确提示，不部分删除。

#### Scenario: 含内置用户则整单失败

- **WHEN** `remove` 请求体包含 `1` 与其它 id
- **THEN** 不删除任一行并返回业务错误

### Requirement: 修改用户状态

系统 SHALL 提供 `POST /system/user/changeStatus`，Body 含 `userId` 与 `status`。当 `user_id == 1` 时，SHALL 禁止将状态置为停用（与设计文档一致）。

#### Scenario: 内置用户禁止停用

- **WHEN** 对 `userId=1` 请求停用态
- **THEN** 请求失败并返回明确中文业务错误

### Requirement: 重置密码

系统 SHALL 提供 `POST /system/user/resetPwd`，Body 含 `userId` 与新密码；新密码 SHALL 经强度/长度校验后加密存储。`user_id == 1` SHALL 允许重置密码（不在禁止改密之外额外禁止）。

#### Scenario: 重置成功

- **WHEN** 合法用户 id 与符合策略的新密码
- **THEN** 更新密码字段并返回成功

### Requirement: 分配角色（用户侧）

系统 SHALL 提供 `GET /system/user/authRole/{userId}` 返回分配角色页所需数据（含可选角色集合与当前用户已绑定角色）。系统 SHALL 提供 `POST /system/user/authRole` 以请求体中的 `userId` 与 `roleIds` 对该用户执行 **`sys_user_role` 全量替换**（事务内先删后插或等价幂等策略）。权限均为 `system:user:edit`。`roleIds` SHALL 至少包含一个元素（保存时）。

#### Scenario: 保存后回显一致

- **WHEN** 用户勾选角色并保存后再次进入分配页
- **THEN** 已勾选角色与 `sys_user_role` 表内容一致

### Requirement: Excel 导入与失败明细

系统 SHALL 提供 `POST /system/user/importData`（`multipart/form-data`：文件 + `updateSupport` 布尔）。当 `updateSupport` 为 true 时，SHALL 按 `user_name` 命中已存在用户则更新（遵守内置用户字段限制），未命中则插入。响应 SHALL 为 JSON，包含总条数、成功条数、失败条数及失败摘要列表。当失败条数大于零时，响应 SHALL 携带短时有效的 `errorKey`（或等价字段）。系统 SHALL 提供 `GET /system/user/importError?errorKey=` 下载失败行 xlsx；`errorKey` 过期后 SHALL 返回明确业务错误。系统 SHALL 提供 `GET /system/user/importTemplate` 下载导入模板。

#### Scenario: 导入统计准确

- **WHEN** 导入文件包含成功与失败行
- **THEN** JSON 中成功/失败计数与实际处理结果一致

### Requirement: Excel 导出

系统 SHALL 提供 `POST /system/user/export`，筛选条件与列表查询一致，导出 **xlsx**，列与列表主要字段一致且无岗位列；使用 `quickboot-common` 既有 Excel 能力。

#### Scenario: 导出需 export 权限

- **WHEN** 用户持有 `system:user:export` 并发起导出
- **THEN** 返回可打开的 xlsx 文件流

### Requirement: 菜单与超级管理员绑定

系统 SHALL 通过 Flyway 插入用户管理菜单及按钮（`perms` 为 `system:user:list`、`add`、`edit`、`remove`、`export`、`import`、`resetPwd` 等与设计文档一致），并 SHALL 将新菜单 id 写入 `sys_role_menu` 使 `role_id = 1` 拥有用户管理入口与按钮权限。菜单主键 SHALL 不与现有 `sys_menu` 冲突。

#### Scenario: 超级管理员可见用户菜单

- **WHEN** 以 `role_id=1` 用户登录并拉取菜单路由
- **THEN** 可见用户管理菜单及已配置按钮权限

### Requirement: 前端用户管理页面与 API

前端 SHALL 将 `quick-ui/src/api/system/user.js` 中用户管理相关请求**全部**指向 `/system/user/*` 定稿路径。用户列表 SHALL 支持设计文档所述查询项、多选、状态开关二次确认、批量操作按钮禁用策略。新增/编辑 SHALL 无岗位字段且提交前校验至少一角色。SHALL 提供独立子路由页面完成分配角色并保存后返回列表刷新。样式 SHALL 遵循根目录 `DESIGN.md`。

#### Scenario: 未授权按钮不可见

- **WHEN** 当前用户缺少某 `system:user:*` 权限
- **THEN** 对应按钮不展示（`v-hasPermi` 行为与项目一致）
