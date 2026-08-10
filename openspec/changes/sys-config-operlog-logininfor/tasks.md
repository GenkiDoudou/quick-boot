## 1. 数据与权限

- [x] 1.1 Flyway：创建 `sys_config`、`sys_oper_log`、`sys_logininfor`；种子配置子集 + 字典（`sys_oper_status` / `sys_oper_business_type` / `sys_oper_operator_type` / `sys_login_status`）
- [x] 1.2 Flyway：菜单与按钮权限（`system:config:*`、`monitor:operlog:*`、`monitor:logininfor:*` 含 unlock）并挂超管

## 2. 参数配置

- [x] 2.1 Entity/Mapper/VO/ImportRow；`ISysConfigService` + Impl（CRUD 显式赋值、key 唯一、内置禁删、缓存读写与 refresh）
- [x] 2.2 `SysConfigController`：`page`/`{id}`/`add`/`update`/`remove`/`configKey/{key}`/`refreshCache`/`export`/`import`（导出 `@IgnoreLogger(RESULT)`）
- [x] 2.3 前端 `api/system/config.js` + `views/system/config/index.vue`（C7JsonTable、刷新缓存、同步导入导出；操作列 `columnType:'slot'`）

## 3. 操作日志（采集 + 管理）

- [x] 3.1 `quickboot-common`：`IgnoreLogger`、`OperLogMeta`、`OperLogProperties`、`OperLogCapturedEvent`/`Payload`、`OperLogPublishingAspect`、脱敏/序列化、自动配置
- [x] 3.2 `quickboot-system`：Entity/Mapper；`OperLogMetaResolver`；异步/同步落库监听；组装写入 `sys_oper_log`
- [x] 3.3 `SysOperLogController`：`page`/`{id}`/`remove`/`clean`/`export`；类级 `@IgnoreLogger(ALL)`；导出 `RESULT`
- [x] 3.4 关键管理 Controller 补 `@Tag`/`@Operation`；导出类接口加 `@IgnoreLogger(RESULT)`
- [x] 3.5 前端 `api/monitor/operlog.js` + `views/monitor/operlog`（列表/详情/删除/清空/导出）

## 4. 登录日志

- [x] 4.1 Entity/Mapper/VO；`ISysLogininforService` + Impl（page/remove/clean/export）；登录成功/失败写库接入现有登录链路
- [x] 4.2 `SysLogininforController`：`page`/`remove`/`clean`/`export`/`unlock/{userName}`（unlock → `LoginLockSupport`）
- [x] 4.3 前端 `api/monitor/logininfor.js` + `views/monitor/logininfor/index.vue`（删除/清空/导出/解锁选中）

## 5. 验证

- [x] 5.1 `mvn -pl quickboot-system -am compile`（含 common）通过
- [ ] 5.2 手工冒烟：配置 CRUD/缓存/导入失败明细；带 OpenAPI 接口产生有标题的操作日志；导出日志无大段 RESULT；登录失败可见日志且 unlock 生效
