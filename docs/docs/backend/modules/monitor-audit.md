# 监控审计（操作日志 / 登录日志 / 在线用户）

## 操作日志

| 项 | 值 |
|----|-----|
| Controller | `SysOperLogController` |
| 路径 | `/monitor/operlog` |
| 前端 | `views/monitor/operlog/index.vue` |
| 采集 | `quickboot-common` `monitor.operlog` AOP |

| 接口 | 说明 |
|------|------|
| GET `/list` | 分页查询 |
| GET `/{operId}` | 详情 |
| POST `/export` | 导出 |
| POST `/remove` | 删除 |
| POST `/clean` | 清空 |

配置（`application.yml`）：

- `qc.monitor.operlog.enabled`：是否采集
- `qc.monitor.operlog.async`：异步落库
- `qc.monitor.operlog.export-limit`：导出上限

Controller 方法可用 `@IgnoreLogger` 排除采集。

---

## 登录日志

| 项 | 值 |
|----|-----|
| Controller | `SysLogininforController` |
| 路径 | `/monitor/logininfor` |
| 前端 | `views/monitor/logininfor/index.vue` |

| 接口 | 说明 |
|------|------|
| GET `/list` | 分页 |
| POST `/export` | 导出 |
| POST `/remove`、`/clean` | 删除/清空 |
| GET `/unlock/{userName}` | 解锁用户（登录失败锁定场景） |

---

## 在线用户

| 项 | 值 |
|----|-----|
| Controller | `SysUserOnlineController` |
| 路径 | `/monitor/online` |
| 前端 | `views/monitor/online/index.vue` |

| 接口 | 说明 |
|------|------|
| GET `/list` | 当前会话列表（Sa-Token） |
| POST `/forceLogout` | 强退 |

会话记录依赖 `OnlineSessionRecorder` 与 Token 存储（local/redis）。

### 异步大数据导出

登录日志、操作日志列表可配置 `export-biz-type`（`monitor:logininfor`、`monitor:operlog`），行数超过阈值时走 [导入导出中心](./import-export-center) 异步导出，详见该文档。

---

## 扩展监控能力

| 文档 | 说明 |
|------|------|
| [慢 SQL 日志](./slow-sql) | Druid 采集、落库、与 trace 关联 |
| [全链路监控](./trace-chain) | operationId / traceId 聚合视图 |
| [导入导出中心](./import-export-center) | 异步 Excel 任务 |
| [文件管理](./file-management) | 上传文件登记与导入导出结果文件 |

## 相关文档

- [安全防护](./security-module)
- [定时任务](./job-scheduler)
- [部署 · 监控告警](../../deploy/monitoring)
