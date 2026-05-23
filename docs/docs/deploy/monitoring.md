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

## 告警建议

- 健康检查失败 → 重启/扩容  
- 登录失败率突增 → 安全告警  
- 磁盘（上传目录、H2 勿用于生产）  
- Redis / MySQL 连接数  

## Druid

开发可开 StatView；**生产关闭** `stat-view-servlet.enabled`。

## 相关

- [系统架构](../design/architecture)
