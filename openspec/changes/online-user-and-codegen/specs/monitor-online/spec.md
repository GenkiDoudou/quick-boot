## ADDED Requirements

### Requirement: List online sessions
系统 SHALL 提供在线用户列表 API（`GET /monitor/online/list`），支持按 IP、用户名筛选，并在服务端对有效 Sa-Token 会话做内存分页。

#### Scenario: List after login
- **WHEN** 用户已登录且会话展示字段已写入 Token-Session
- **THEN** 列表中可见该会话的用户名、部门、IP、浏览器、OS、登录时间等相关展示字段

#### Scenario: Filter by username or IP
- **WHEN** 客户端携带用户名和/或 IP 筛选条件请求列表
- **THEN** 系统仅返回匹配的在线会话（分页结果反映筛选后集合）

#### Scenario: Pagination mapping
- **WHEN** 客户端以 C7 风格分页参数（如 `{current,size,param}`）或扁平 `pageNum/pageSize` 请求列表
- **THEN** 系统正确解析分页参数并返回对应页数据（行为对齐现网 job-log 映射约定）

### Requirement: Force logout
系统 SHALL 提供强制下线 API（`POST /monitor/online/forceLogout`），请求体包含 `tokenId`，用于注销对应会话。

#### Scenario: Force logout invalidates token
- **WHEN** 管理员对某在线会话发起强退且 `tokenId` 有效
- **THEN** 该会话被注销；使用旧 token 访问需登录接口时返回 401（或等价未登录响应）

#### Scenario: Missing tokenId rejected
- **WHEN** 强退请求缺少有效 `tokenId`
- **THEN** 系统拒绝该请求并以业务/参数错误反馈

### Requirement: Record session display fields on login
系统 SHALL 在登录成功并完成 `LoginHelper.loginByDevice`（或等价登录落 token）之后，向 Token-Session 写入在线列表所需展示字段（至少：用户名、部门名、IP、浏览器、OS、登录时间；登录地可空）。

#### Scenario: Session fields present after login
- **WHEN** 用户登录成功
- **THEN** 其 Token-Session 中已写入上述展示字段，且后续列表可读取

### Requirement: No online business table
在线用户能力 SHALL NOT 依赖专用业务表持久化会话列表；列表数据来源于 Sa-Token 有效 token 扫描与会话属性。

#### Scenario: Operate without online table
- **WHEN** 库中不存在专用「在线用户」业务表
- **THEN** 列表与强退仍可正常工作

### Requirement: Online permissions
在线用户操作 SHALL 受权限控制：`monitor:online:list`、`monitor:online:forceLogout`（与菜单种子一致）。

#### Scenario: Unauthorized list denied
- **WHEN** 无 `monitor:online:list` 权限的用户请求列表
- **THEN** 系统拒绝该请求

#### Scenario: Unauthorized force logout denied
- **WHEN** 无 `monitor:online:forceLogout` 权限的用户请求强退
- **THEN** 系统拒绝该请求
