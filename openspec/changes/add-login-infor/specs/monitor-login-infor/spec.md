## ADDED Requirements

### Requirement: 登录访问日志持久化

系统 MUST 将每次账号密码登录尝试（成功或失败）持久化到 `sys_logininfor`，字段至少包含：`info_id`、`user_name`、`ipaddr`、`login_location`（可空）、`browser`、`os`、`status`、`msg`、`login_time`；成功时 `user_id` 可写入，失败可空。写库异常 MUST NOT 阻止登录请求完成（成功或失败响应仍按原语义返回），异常 MUST 记录错误日志。

#### Scenario: 登录成功写入日志

- **WHEN** 用户凭据校验通过并完成会话建立
- **THEN** 系统在 `sys_logininfor` 中插入一条成功记录且 `login_time` 为当前时间

#### Scenario: 登录失败写入日志

- **WHEN** 用户凭据校验失败（含账号不存在、密码错误等统一语义）
- **THEN** 系统在 `sys_logininfor` 中插入一条失败记录且 `status`/`msg` 与失败原因一致

#### Scenario: 写库失败不阻断登录

- **WHEN** 插入登录日志时发生数据访问异常
- **THEN** 系统记录错误日志且登录成功或失败响应仍按业务规则返回

### Requirement: 登录失败锁定与 CacheManager

系统 MUST 支持基于 `sys_config` 内置键 `qc.login.fail-lock-enabled`、`qc.login.max-retry`、`qc.login.lock-minutes` 的失败计数与临时锁定。锁定状态 MUST 仅通过 Spring `org.springframework.cache.CacheManager` 获取的 `org.springframework.cache.Cache` 进行 `get`/`put`/`evict` 维护；业务代码 MUST NOT 使用 `RedisTemplate`、`StringRedisTemplate` 或 `RedisConnectionFactory` 操作锁定数据。`qc.login.fail-lock-enabled` 为关闭时，系统 MUST NOT 执行失败计数与锁定检查，解锁接口 MUST 仍为幂等成功。

#### Scenario: 达阈值后拒绝登录

- **WHEN** 锁定开关为开启且同一规范化用户名连续失败次数达到 `qc.login.max-retry`
- **THEN** 系统在缓存中标记锁定并在锁定期内拒绝新的登录尝试（中文业务提示）

#### Scenario: 成功登录清除失败状态

- **WHEN** 用户登录成功
- **THEN** 系统清除该用户名在锁定缓存中的条目（失败计数与锁定标记）

#### Scenario: 解锁清除缓存

- **WHEN** 管理员调用解锁接口且用户名为合法非空字符串
- **THEN** 系统对该用户名执行缓存 `evict` 并返回明确成功提示

### Requirement: 监控侧登录日志 REST 接口

系统 MUST 暴露以下接口并接入 Sa-Token 权限校验：`GET /monitor/logininfor/list`（`monitor:logininfor:list`）、`POST /monitor/logininfor/remove`（`monitor:logininfor:remove`，支持批量）、`POST /monitor/logininfor/clean`（`monitor:logininfor:remove`）、`POST /monitor/logininfor/export`（`monitor:logininfor:export`）、`GET /monitor/logininfor/unlock/{userName}`（`monitor:logininfor:unlock`）。列表 MUST 支持按登录地址（IP）、用户名、状态、登录时间区间筛选，默认按访问时间倒序，并 MUST 支持按用户名与访问时间排序。对外写接口 MUST 使用 Jakarta Validation；业务失败 MUST 使用项目自定义异常，不得使用 `IllegalArgumentException` 作为业务失败信号。

#### Scenario: 分页列表

- **WHEN** 持有 `monitor:logininfor:list` 的客户端请求列表并传入分页与筛选参数
- **THEN** 系统返回分页数据且字段包含访问编号、用户名、IP、登录地点、操作系统、浏览器、状态、描述、访问时间

#### Scenario: 批量删除

- **WHEN** 持有 `monitor:logininfor:remove` 的客户端提交待删除的日志主键集合
- **THEN** 系统删除对应记录并返回成功语义

#### Scenario: 导出

- **WHEN** 持有 `monitor:logininfor:export` 的客户端在 POST 导出请求中携带与列表一致的筛选条件
- **THEN** 系统生成 Excel 文件且列与列表展示一致或可为其超集

### Requirement: 内置参数与参数模块约束

系统 MUST 通过 Flyway 在 `sys_config` 中插入 `qc.login.fail-lock-enabled`、`qc.login.max-retry`、`qc.login.lock-minutes`，且 `config_type` 为系统内置（`1`）。这些记录 MUST 遵循现有参数服务规则：不可删除；编辑时仅允许修改允许的键值字段，禁止篡改键名与内置标记以绕过限制。

#### Scenario: 内置参数不可删除

- **WHEN** 客户端尝试批量删除包含上述任一配置主键的请求
- **THEN** 系统拒绝并返回中文业务错误提示

### Requirement: 前端登录日志页

`quick-ui` MUST 提供登录日志列表页面：支持查询、默认倒序列表、用户名与时间排序、多选删除、清空、导出、对选中行用户名去重后解锁。按钮与路由 MUST 受 `monitor:logininfor:list|remove|unlock|export` 控制。状态列 MUST 与字典及后端 `status` 字段展示一致。

#### Scenario: 无权限隐藏操作

- **WHEN** 当前用户缺少 `monitor:logininfor:remove`
- **THEN** 页面不展示删除与清空入口（或等价禁用）
