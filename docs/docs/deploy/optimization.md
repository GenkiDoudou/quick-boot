# 性能优化

## 后端

| 项 | 建议 |
|----|------|
| 连接池 | Druid `max-active` 按并发调优 |
| 缓存 | 生产 `spring.cache.type=redis`，字典/配置走缓存 |
| Token | `qc.oauth2.token-store=redis` 多实例 |
| 操作日志 | `operlog.async-enabled=true` |
| JVM | `-Xms`/`-Xmx` 与 GC 日志监控 |
| SQL | 关闭 dev 的 `StdOutImpl`；生产用慢 SQL 日志 |

## 前端

| 项 | 建议 |
|----|------|
| 构建 | `pnpm build:prod`，路由懒加载已由 `import()` 完成 |
| 静态资源 | Nginx `gzip` / 长期缓存 `assets/*` |
| 列表 | 合理 `pageSize`，避免一次拉取过大 |
| 字典 | `useDict` 缓存，减少重复请求 |

## 数据库

- 菜单、日志表按时间归档  
- 操作/登录日志定期清理任务  

## 相关

- [监控告警](./monitoring)
- [配置说明](./configuration)
