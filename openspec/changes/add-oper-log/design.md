## Context

`quickboot2` 主工程尚无操作日志表与管理端；需求见 `原始需求/系统管理/操作日志-需求文档.md`。已有一份工程内设计真源 `docs/superpowers/specs/2026-05-16-operlog-design.md` 与实现计划 `docs/superpowers/plans/2026-05-16-operlog.md`。采集风格对齐 `原始需求/old/quick-boot`：**宽切面 + Spring 事件 + `IgnoreLogger`**。链路 id 与现有 `TraceIds`（MDC 键 `traceId`）及统一响应 `R` 对齐。

## Goals / Non-Goals

**Goals:**

- 持久化若依语义 `sys_oper_log`，含 **`trace_id`**；支持按条件分页、详情、删除/批量删除、清空、同步导出（带行数上限）。
- 宽切面在请求线程 `finally` 中发布事件，载荷**携带** `traceId`（来自 `TraceIds.current()`），监听器同步脱敏写库；写库异常**不抛出**到业务线程。
- 管理 API 使用 **`POST` 表达删除/清空**、**`GET` 列表/详情**、**`POST` 导出**；权限 `monitor:operlog:*`。
- `quick-ui` 提供与《登录日志》监控模块一致风格的页面与权限。

**Non-Goals:**

- 异步导出、日志归档到对象存储、修改全局 Micrometer 传播实现。
- 修改 `openspec/specs/` 下既有 capability 的规范正文（仅复用 common 能力）。

## Decisions

| 决策 | 选项 | 理由 |
|------|------|------|
| 采集触发 | **宽切面 + 事件** 而非仅 `@Log` 打点 | 与已拍板旧栈一致；覆盖面大，`IgnoreLogger` 控噪 |
| `trace_id` 来源 | 切面写入 **事件 DTO**；监听器以 DTO 为准 | 避免未来异步化时 MDC 丢失；与 `TraceIds` 同源 |
| `status` 语义 | **0 正常 / 1 异常** | 对齐若依与需求字典；摒弃旧栈 HTTP 码混用 |
| 模块标题兜底 | **`@OperLogMeta` > `@Tag`+`@Operation` > 固定「未分类」** | 宽切面无 RuoYi `@Log` 元数据时的可展示性 |
| 管理端自记 | **`@IgnoreLogger(ALL)`** 于 `OperLogController` | 防止查询/导出日志产生日志风暴 |
| 导出上限 | **`qc.monitor.operlog.export-max-rows`**，默认 `10000` | 与已确认 4A 一致，可配置防 OOM |
| 模块放置 | 切面/注解优先 **`quickboot-common`** + 自动配置，业务在 **`quickboot-web`…`monitor.operlog`** | 减少 web↔common 循环依赖风险（若切面必须读 web 专属 Bean，再局部调整） |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 切面过宽导致写库量与延迟上升 | `IgnoreLogger`、URL 前缀排除、敏感接口显式 `ALL`；长字段截断 |
| 参数/响应 JSON 过大 | 长度上限 + 大文件参数仅记文件名 |
| 脱敏规则与审计完整性冲突 | 键名黑名单 + 与 `common-field-desensitization` 对齐；文档注明审计边界 |
| Flyway 版本或 `menu_id` 与并行变更冲突 | 实现前执行 `ls`/`glob` 取当前最大 `V*` 与菜单 id，顺延编号 |

## Migration Plan

1. 部署应用前执行 Flyway：新建 `sys_oper_log` 及索引；插入菜单/角色菜单/字典（若需要）。
2. 回滚：保留迁移文件不删除；若需紧急回滚数据，可 `TRUNCATE sys_oper_log` 并移除菜单（生产慎用，以运维策略为准）。
3. 无存量数据迁移假设（新表）。

## Open Questions

- （无）实现阶段若 `logininfor` 已占用某 `menu_id` 区间，以当时仓库 Flyway 为准顺延。
