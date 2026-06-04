# 慢 SQL 日志

## 概述

通过 Druid JDBC 过滤器统一采集 **业务 MyBatis**、**积木报表 MiniDao** 以及无 HTTP 上下文（如 Flyway）执行的慢 SQL，落库 `sys_slow_sql`，管理端提供查询、详情、导出与清理。

| 项 | 值 |
|----|-----|
| Controller | `SysSlowSqlController` |
| 路径前缀 | `/monitor/slowSql` |
| 前端 | `quick-ui/src/views/monitor/slowSql/index.vue` |
| 菜单 | 系统监控 → 慢SQL日志（`menu_id=2264`，`perms=monitor:slowSql:query`） |
| 迁移 | `V51__sys_slow_sql.sql` |

## 数据模型（`sys_slow_sql`）

| 字段 | 说明 |
|------|------|
| `slow_id` | 主键 |
| `sql_source` | `BUSINESS` 业务接口；`JIMU` 积木（`/jmreport/**` 等）；`SYSTEM` 无 HTTP |
| `mapper_id` | MyBatis 语句 ID（业务 SQL 常见；积木/系统可能为空） |
| `sql_text` | SQL 文本（长度受 `max-sql-length` 截断） |
| `cost_time` | 耗时（毫秒） |
| `trace_id` | 请求线程 MDC（`TraceIds`） |
| `client_operation_id` | 前端一次用户操作 ID（与全链路监控关联） |
| `client_id` | 客户端标识 |
| `request_method` / `request_uri` | HTTP 上下文（无 HTTP 时为空串） |
| `oper_name` | 操作人 |
| `create_time` | 记录时间 |

索引含 `create_time`、`trace_id`、`sql_source`、`cost_time`；V54 为全链路增加 `client_operation_id` 索引。

## 接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/list` | `monitor:slowSql:query` | 分页查询 |
| GET | `/{slowId}` | `monitor:slowSql:query` | 详情 |
| POST | `/export` | `monitor:slowSql:export` | 导出 Excel |
| POST | `/remove` | `monitor:slowSql:remove` | 批量删除 |
| POST | `/clean` | `monitor:slowSql:remove` | 清空 |

## 配置（`qc.monitor.slow-sql`）

`application.yml` 示例：

```yaml
qc:
  monitor:
    slow-sql:
      capture-enabled: true    # 是否采集落库
      threshold-ms: 1000       # 慢 SQL 阈值（与 Druid 日志阈值可并存）
      log-enabled: true        # 是否打应用日志
      async-enabled: true      # 异步落库，避免拖慢请求
      export-max-rows: 10000
      max-sql-length: 4000
```

Druid 连接池另可配置 `spring.datasource.druid.filter.stat.slow-sql-millis`（dev 常 `500`、prod `2000~3000`），用于 **控制台慢 SQL 日志**，与业务表采集阈值相互独立。

实现要点（`quickboot-common`）：

- `SlowSqlDruidFilter`：挂到 Druid，统计执行耗时
- `SlowSqlMapperIdInnerInterceptor`：写入 `mapper_id`

## 与全链路监控

慢 SQL 记录的 `client_operation_id`、`trace_id` 可作为 [全链路监控](./trace-chain) 查询条件，在 Network 视图中与操作日志、前端行为一并展示。

## 相关文档

- [监控审计](./monitor-audit)（操作/登录日志）
- [全链路监控](./trace-chain)
- [部署 · 监控告警](../../deploy/monitoring)
