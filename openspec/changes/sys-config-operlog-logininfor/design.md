## Context

权威产品设计见 `docs/superpowers/specs/2026-08-06-sys-config-operlog-logininfor-design.md`。本文件为 OpenSpec 实现向设计：如何按 OauthClient 契约落地参数配置，并移植 bak 宽切面操作日志与登录日志。

现状：无 `sys_config` / `sys_oper_log` / `sys_logininfor`；无 operlog AOP；登录仅有锁定支持（`LoginLockSupport`），无登录访问日志表；bak 有完整实现但含异步导出中心。

## Goals / Non-Goals

**Goals:**

1. 参数配置管理端完整可用（CRUD、缓存、按 key 查、同步导入导出）。
2. 所有 `@RestController` 公共方法自动采集操作日志（黑名单与 `@IgnoreLogger` 除外）；OpenAPI 丰富标题；导出忽略 RESULT。
3. 操作日志 / 登录日志管理端：查询、删除、清空、导出；登录日志支持 unlock。
4. 登录成功/失败写入登录日志。
5. 实现顺序：Config → OperLog（采集+管理）→ LoginInfor → 前端。

**Non-Goals:**

- bak 异步导入导出中心、慢 SQL、在线用户、定时任务。
- bak `/system/config` URL 兼容层。
- 「仅 `@Log` 注解才记录」模式。

## Decisions

### 1. 路径

- 配置：`sys/config`（与本项目 `sys/*` 一致）。
- 日志管理：`monitor/operlog`、`monitor/logininfor`（与 bak 监控域一致，便于权限字 `monitor:*`）。
- 列表统一 `POST .../page`（对齐 OauthClient，**不**用 bak 的 GET list）。

### 2. 操作日志宽切面放在 `quickboot-common`

- 采集与注解、Properties、Event 与 bak 同构，便于多模块复用。
- 落库监听与 `OperLogMetaResolver` 放在 `quickboot-system`（依赖实体/Mapper）。
- 备选：全部放 system → 否决，common 无业务表依赖更清晰。

### 3. `@IgnoreLogger` 与 URI 黑名单并用

- 类级 `ALL`：`SysOperLogController`（避免自递归噪声）。
- 方法级 `RESULT`：所有导出/下载接口。
- `ignore-url-patterns`：actuator、Swagger、验证码、`/monitor/operlog/**` 等。

### 4. 标题与业务类型

- 解析顺序：`@OperLogMeta` → `@Tag` + `@Operation.summary` → `Class.method`。
- 业务类型推断对齐 bak `OperLogMetaResolver`（export/import/add/update/remove/clean…）。
- 实现期：关键管理 Controller 补齐 `@Tag`/`@Operation`；导出补 `@IgnoreLogger(RESULT)`。

### 5. 异步落库默认开启

- `qc.monitor.operlog.async-enabled=true`；失败仅日志，不回滚业务。
- 可切同步便于本地排查。

### 6. 配置缓存

- 使用现有 Cache 抽象或 Spring Cache，key=`configKey`；增删改刷新；`refreshCache` 全量。
- 内置 `configType=1`：禁止删除；编辑时禁止改 key 与内置标记。

### 7. 登录日志 unlock

- `GET monitor/logininfor/unlock/{userName}` 调用现有 `LoginLockSupport` 清锁定；不改变锁定策略本身。

### 8. 权限字

- `system:config:list|query|add|edit|remove|export|import`
- `monitor:operlog:list|query|remove|export`（清空用 remove）
- `monitor:logininfor:list|query|remove|export|unlock`

## Risks / Trade-offs

- [全量 RestController 噪声] → URI 黑名单 + `@IgnoreLogger`；operlog 自身 ALL
- [异步落库丢失] → 监听异常打日志；可切同步
- [Sa-Token 未登录] → operName/userId 可空
- [体量大] → tasks 严格分阶段；先通 Config 再切面再登录日志再前端

## Migration Plan

1. Flyway 建表 + 字典 + 菜单；重启后超管可见菜单。
2. 先上 Config，再开 `capture-enabled`（默认 true）。
3. 回滚：关 `qc.monitor.operlog.capture-enabled`；菜单可留；表可保留。

## Open Questions

- 无（设计文档已确认）。
