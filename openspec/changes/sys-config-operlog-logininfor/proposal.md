## Why

系统缺少参数配置与运维审计能力：无 `sys_config` 管理端，无操作日志宽切面落库，登录成功/失败未写入可查询的登录日志。bak 已有完整实现但依赖异步导出中心与旧 `/system/*` 路径，与当前 `SysOauthClient` / `SysRole` 契约不一致，需按本项目规范迁入。

## What Changes

- 新增**参数配置**（`sys/config`）：CRUD、按 key 查值、刷新缓存、同步 Excel 导入导出；内置参数禁止删除。
- 新增**操作日志采集**：`quickboot-common` 宽切面切全部 `@RestController`；`@IgnoreLogger`（ALL/PARAMS/RESULT）；标题优先 OpenAPI `@Tag`/`@Operation`；事件异步落库 `sys_oper_log`。
- 新增**操作日志管理**（`monitor/operlog`）：分页、详情、批删、清空、同步导出。
- 新增**登录日志**：登录成功/失败写 `sys_logininfor`；管理端分页、批删、清空、导出、解锁（对接现有 `LoginLockSupport`）。
- Flyway：三表、相关字典种子、菜单与按钮权限。
- 前端：`system/config`、`monitor/operlog`、`monitor/logininfor`（C7JsonTable + 同步导入导出）。
- **BREAKING（相对 bak）**：不兼容 bak `/system/config` 与异步导出中心；管理列表统一 `POST .../page`。

参考设计：`docs/superpowers/specs/2026-08-06-sys-config-operlog-logininfor-design.md`。  
静态原型：`docs/demo/sys-config-operlog-logininfor-prototype.html`。

## Capabilities

### New Capabilities

- `sys-config`: 系统参数 CRUD、缓存、按 key 查询、同步导入导出。
- `monitor-operlog`: 宽切面采集与操作日志管理（查询/详情/删除/清空/导出）。
- `monitor-logininfor`: 登录访问日志写库与管理（查询/删除/清空/导出/解锁）。

### Modified Capabilities

- （无既有主 specs 目录能力需改写；登录模块仅扩展写登录日志与 unlock 对接，不另立 delta。）

## Impact

- 后端：`quickboot-common` 新增 operlog 采集组件；`quickboot-system` 新增 Config/OperLog/LoginInfor 管理与落库；登录服务写入登录日志；Flyway 迁移。
- 前端：`quick-ui` 新增 config / operlog / logininfor 页面与 API。
- 依赖：复用 `ExcelUtils`、现有 Cache/Sa-Token、`LoginLockSupport`；AOP 依赖 spring-aop。
- 行为：几乎所有 REST 接口将被记录操作日志（URI 黑名单与 `@IgnoreLogger` 除外）。
