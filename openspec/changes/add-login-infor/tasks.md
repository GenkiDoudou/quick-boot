## 1. 数据库与种子

- [x] 1.1 新增 Flyway：创建 `sys_logininfor` 表及必要索引（含 `login_time`）
- [x] 1.2 新增 Flyway：插入三条系统内置 `sys_config`（`qc.login.fail-lock-enabled`、`qc.login.max-retry`、`qc.login.lock-minutes`，`config_type=1`）
- [x] 1.3 新增 Flyway：登录状态相关 `sys_dict_type` / `sys_dict_data`（若尚无），与 `status` 取值一致
- [x] 1.4 新增 Flyway：`sys_menu`「登录日志」及 `sys_role_menu` 授权（menu_id 与现网迁移不冲突）

## 2. 登录锁定（CacheManager）

- [x] 2.1 实现 `LoginLockService`（或等价名）：仅依赖 `CacheManager`，缓存分区名遵循 `xxx#ttlSeconds` 约定；`get`/`put`/`evict` 管理失败计数与锁定截止时间
- [x] 2.2 从 `SysConfigService` 读取三项配置；`fail-lock-enabled=false` 时跳过计数与 `isLocked`；配置解析失败策略在代码注释中写明
- [x] 2.3 为 `LoginLockService` 编写单元测试（可配合 Caffeine `CacheManager` 或测试切片）

## 3. 登录日志持久化与登录接入

- [x] 3.1 新增 `SysLogininfor` 实体、Mapper、插入与分页查询方法
- [x] 3.2 实现 `SysLogininforLogService`（或内聚于 `AuthLoginService`）：组装 IP、UA、地点、`status`、`msg`；插入失败不抛到登录主流程
- [x] 3.3 修改 `AuthController#login`：trim 用户名；`isLocked` 检查；成功/失败写日志；失败调用 `recordFailure`；成功 `StpUtil.login` 后 `clear` 失败缓存

## 4. 监控接口

- [x] 4.1 新增 `LogininforController`：`GET .../list`、`POST .../remove`、`POST .../clean`、`POST .../export`、`GET .../unlock/{userName}`，权限与 OpenAPI 注解齐全
- [x] 4.2 实现分页查询 BO、校验与排序参数；导出使用 `ExcelUtils` 与现有导出模式一致
- [x] 4.3 解锁调用 `LoginLockService` 并返回明确中文成功消息

## 5. 前端

- [x] 5.1 新增 `quick-ui/src/api/monitor/logininfor.ts`（或 `.js` 与工程一致）
- [x] 5.2 新增列表页（对齐 DESIGN.md 与现有系统管理列表交互）：查询、排序、多选删、清空、导出、解锁
- [x] 5.3 注册路由与侧栏菜单路径，与 Flyway 菜单 `component` 一致

## 6. 验证

- [x] 6.1 后端：`mvn -pl quickboot-web test` 或通过模块编译验证
- [x] 6.2 前端：`pnpm -C quick-ui build:prod`
- [x] 6.3 手工验收：连续失败锁定、解锁恢复、内置参数不可删、导出非空
