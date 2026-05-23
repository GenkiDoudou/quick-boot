## Context

`quickboot` 已具备 Sa-Token 登录、`SysConfig` 参数模块、Spring Cache（`CacheManager` + `DynamicTtlRedisCacheManager` / Caffeine）及 Flyway 迁移习惯。仓库级约束要求删除类语义优先使用 **POST**，对外接口需 Jakarta Validation 与 OpenAPI 注解。详细字段与接口表见变更外设计参考：`docs/superpowers/specs/2026-05-16-logininfor-design.md`。

## Goals / Non-Goals

**Goals:**

- 登录访问可审计：表 `sys_logininfor`、列表/排序/筛选、导出、删除与清空。
- 登录失败达阈值可锁定；成功登录与解锁接口清除锁定相关缓存条目。
- 锁定数据仅经 **`CacheManager#getCache`** 得到的 `Cache` 读写；配置经 **`sys_config` 内置参数**（不可删除）。
- `quick-ui` 提供与权限点一致的页面与 API 封装。

**Non-Goals:**

- 本期不记录登出访问日志。
- 不在业务模块直接调用 Redis 底层 API 存锁定状态。

## Decisions

| 决策 | 选项 | 理由 |
|------|------|------|
| API 前缀 | `/monitor/logininfor` | 与需求文档及 proposal 一致 |
| 删除/清空 HTTP | POST 独立路径 | 与 `AGENTS.md` / 现有 Controller 风格一致 |
| 表字段 | `status` + `msg` 若依语义 | 与需求「状态」「描述」及字典一致 |
| 缓存名 | 如 `qc-login-fail#604800` | 与项目 `cacheName#ttlSeconds` 约定一致；条目内带 `lockUntil` 以覆盖 TTL 与 `lock-minutes` 不完全同步的情况 |
| 配置读取 | `SysConfigService.getConfigValueByKey` | 已带 `@Cacheable`，避免高频打库 |
| 包路径 | `io.github.genkidoudou.web.monitor.logininfor`（可调） | 监控域与 system 域分离，便于权限与路由分组 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 写日志异常影响登录 | 写库 try/catch，错误仅打日志 |
| 缓存与 DB 配置语义不一致 | 解锁全量 `evict` 用户名 key；读配置失败时使用安全默认或明确失败（实现时二选一并写入任务说明） |
| 菜单/字典种子 ID 冲突 | Flyway 选用未占用 `menu_id`/`dict` 主键段 |

## Migration Plan

1. 部署应用触发 Flyway：创建 `sys_logininfor`、插入内置 `sys_config`、菜单与字典（若有）。
2. 已有环境无回滚删除表要求时，可提供手工 `DROP TABLE` 脚本作为灾难回滚（非自动）。

## Open Questions

- （无）实现阶段若 IP 归属地解析无现成组件，可采用空字符串或轻量第三方，在 tasks 中落具体选型。
