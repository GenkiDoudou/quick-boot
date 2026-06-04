# 监控告警

## Spring Boot Actuator

`application.yml` 已暴露：

- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus`

生产勿将 Actuator 匿名暴露到公网；可单独端口或 IP 白名单。

## 链路追踪

```yaml
management:
  otlp:
    tracing:
      endpoint: http://your-collector:4318/v1/traces
```

对接 Jaeger / Tempo 等 OTLP 兼容后端。

## 业务监控

| 能力 | 入口 |
|------|------|
| 操作日志 | 管理端「操作日志」 |
| 登录日志 | 「登录日志」 |
| 在线用户 | 「在线用户」 |
| 定时任务日志 | 「调度日志」 |
| 慢 SQL 日志 | 「慢SQL日志」（Druid JDBC 采集，含 traceId；来源 BUSINESS / JIMU / SYSTEM） |
| 全链路监控 | 「全链路监控」（按 operationId/traceId 聚合前后端链路） |

模块说明见 [慢 SQL 日志](../backend/modules/slow-sql)、[全链路监控](../backend/modules/trace-chain)。

## 慢 SQL（`qc.monitor.slow-sql`）

| 项 | 说明 |
|----|------|
| 采集 | `SlowSqlDruidFilter` 挂到 Druid 连接池，覆盖 MyBatis 与积木 MiniDao |
| `trace_id` | 请求线程 MDC（`TraceIds`） |
| `sql_source` | `BUSINESS` 业务接口；`JIMU`（`/jmreport/**` 等）；`SYSTEM` 无 HTTP（如 Flyway） |
| `mapper_id` | MyBatis `SlowSqlMapperIdInnerInterceptor` 写入，仅业务 SQL 常有值 |
| 阈值 | 默认 1000ms；dev `500`；prod `2000`（与 Druid `slow-sql-millis` 日志可并存） |

## 告警建议

- 健康检查失败 → 重启/扩容  
- 登录失败率突增 → 安全告警  
- 磁盘（上传目录、H2 勿用于生产）  
- Redis / MySQL 连接数  

## Druid

开发可开 StatView；**生产关闭** `stat-view-servlet.enabled`。

## 相关

- [系统架构](../design/architecture)
