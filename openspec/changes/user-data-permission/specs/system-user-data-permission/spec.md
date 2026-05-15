## ADDED Requirements

### Requirement: 用户读操作受角色数据范围约束

系统 MUST 在用户管理模块中，对所有从持久层读取 `sys_user` 主表数据的请求，按当前登录用户所绑定角色的 `data_scope` 与 `sys_role_dept` 计算可见范围，并通过 MyBatis-Plus `DataPermissionInterceptor` 将等价条件注入 SQL。语义 MUST 与若依 RuoYi 常见 `data_scope` 含义一致：`1` 全部、`2` 自定义、`3` 本部门、`4` 本部门及以下、`5` 仅本人。

#### Scenario: 全部数据范围不追加过滤

- **WHEN** 当前登录用户所绑定角色中至少一个角色的 `data_scope` 为 `1`
- **THEN** 对该请求的用户读 SQL MUST NOT 因数据权限追加限制性 `WHERE` 条件（或仅追加恒真条件）

#### Scenario: 自定义范围仅见绑定部门用户

- **WHEN** 用户仅持有 `data_scope` 为 `2` 的角色，且这些角色在 `sys_role_dept` 中绑定部门集合为 D
- **THEN** 用户读 SQL MUST 限制为 `sys_user.dept_id` 属于 D 中部门（并集）

#### Scenario: 本部门仅见本部门用户

- **WHEN** 用户持有 `data_scope` 为 `3` 的角色且登录上下文 `dept_id` 为 X
- **THEN** 用户读 SQL MUST 包含 `sys_user.dept_id = X`（与其它角色范围 OR 合并后仍须满足并集语义）

#### Scenario: 本部门及以下见子树用户

- **WHEN** 用户持有 `data_scope` 为 `4` 的角色且登录上下文 `dept_id` 为 X
- **THEN** 用户读 SQL MUST 限制 `sys_user.dept_id` 属于以 X 为根的部门子树（与系统内部门树计算语义一致）

#### Scenario: 仅本人仅见自己

- **WHEN** 用户持有 `data_scope` 为 `5` 的角色且登录用户 id 为 U
- **THEN** 用户读 SQL MUST 包含 `sys_user.user_id = U`（与其它角色范围按并集 OR 合并）

#### Scenario: 多角色并集

- **WHEN** 用户同时持有多种 `data_scope` 的角色且不存在 `data_scope=1`
- **THEN** 可见用户集合 MUST 为各角色单独计算可见集合的并集

### Requirement: 数据权限与前端部门筛选 AND

系统 MUST 将数据权限条件作为基线；当前端在用户列表或导出请求中传入 `deptId`（含子孙部门）筛选时，最终结果 MUST 同时满足数据权限条件与部门树筛选条件（逻辑与 / AND）。

#### Scenario: 前端部门筛选不放宽权限

- **WHEN** 数据权限允许的用户集合为 A，按 `deptId` 树筛选得到的集合为 B
- **THEN** 返回结果 MUST 仅包含 A 与 B 的交集

### Requirement: 用户 dept_id 写入必填

系统 MUST 在校验通过的用户创建、修改与导入路径中要求 `dept_id` 非空；若缺失 MUST 拒绝并返回可预期的业务错误（如 `WarningException`），不得写入空 `dept_id` 的新用户。

#### Scenario: 创建用户缺少部门

- **WHEN** 调用创建用户接口且 `dept_id` 为空或未传
- **THEN** 系统 MUST 拒绝创建并返回业务错误

### Requirement: 登录用户缺少 dept_id 时的行为

当登录用户 `dept_id` 缺失时：系统 MUST 拒绝依赖该上下文的用户管理写操作（创建/修改/删除/状态/密码/导入等）并返回业务错误；对用户读列表请求，若无法依据部门类 `data_scope` 计算范围，则 MUST 返回空结果集；对读取指定 `userId` 的详情类请求，若不在任何允许集合内，则 MUST 视为用户不存在。

#### Scenario: 无部门用户尝试修改他人

- **WHEN** 登录用户 `dept_id` 缺失且请求修改或删除另一用户
- **THEN** 系统 MUST 返回业务错误或在数据权限下无法选中该用户（不得成功持久化越权修改）

### Requirement: 超级管理员不绕过数据权限

系统 MUST NOT 基于「超级管理员」或固定 admin 账号在拦截器或 Mapper 层跳过数据权限。具有全部可见能力的账号 MUST 仅通过角色 `data_scope=1` 配置获得。

#### Scenario: 高权限账号无全部角色时受限

- **WHEN** 某账号具有用户管理菜单权限但其角色 `data_scope` 均不为 `1`
- **THEN** 其可见用户集合 MUST 仍受对应 `data_scope` 限制

### Requirement: 拦截器注册顺序

系统 MUST 在 `MybatisPlusInterceptor` 中注册 `DataPermissionInterceptor`，且其顺序 MUST 位于 `PaginationInnerInterceptor` 之前，以保证分页统计与结果集一致。

#### Scenario: 分页列表计数一致

- **WHEN** 用户请求分页列表且存在数据权限过滤
- **THEN** `total` 与当前页记录 MUST 与带数据权限的 SQL 一致，不得出现先分页后过滤导致的计数失真
