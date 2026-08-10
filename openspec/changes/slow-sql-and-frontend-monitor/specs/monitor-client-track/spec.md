## ADDED Requirements

### Requirement: Client track batch report
系统 SHALL 在 `POST /monitor/clientTrack/report` 接收已登录用户的前端行为批次并写入 `sys_client_track`；该接口 MUST 校验登录，MUST NOT 要求菜单权限字。

#### Scenario: Logged-in report succeeds
- **WHEN** 已登录客户端提交合法批次 body
- **THEN** 系统落库一条批次记录并返回成功

#### Scenario: Anonymous report rejected
- **WHEN** 未登录客户端调用 `/report`
- **THEN** 系统拒绝该请求（未认证）

### Requirement: Client track management API
系统 SHALL 提供批次分页列表、行为轨迹概览与单页明细、批量删除与清空，权限字为 `monitor:clientTrack:list|remove`。

#### Scenario: List and timeline with permission
- **WHEN** 持有 `monitor:clientTrack:list` 的用户请求 list / timeline / timeline/page
- **THEN** 返回对应批次或轨迹数据

#### Scenario: Remove and clean
- **WHEN** 持有 `monitor:clientTrack:remove` 的用户删除或清空
- **THEN** 对应批次从 `sys_client_track` 移除

### Requirement: Frontend monitor plugin default on
`quick-ui` SHALL 迁入埋点插件并默认开启（`VITE_APP_MONITOR_ENABLED` 缺省为 true），向 `/monitor/clientTrack/report` 上报；MUST 在请求中携带操作关联头（如 `X-Client-Operation-Id`）；监控管理页路径 MUST 可配置排除以免自刷噪声。

#### Scenario: Plugin reports after login
- **WHEN** 已启用埋点且用户登录后在非排除页操作
- **THEN** 客户端将行为批次上报且管理端列表可见新数据

#### Scenario: Plugin disabled
- **WHEN** `VITE_APP_MONITOR_ENABLED` 为 false
- **THEN** 客户端不发起新的行为监控上报

### Requirement: Client track admin UI
系统 SHALL 提供前端监控批次、事件链路与行为轨迹管理页及 API 封装。

#### Scenario: Admin browses track pages
- **WHEN** 管理员打开前端监控相关菜单
- **THEN** 可查看批次列表、事件链路与行为轨迹（受 list 权限控制）
