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

## 相关文档

- [安全防护](./security-module)
- [定时任务](./job-scheduler)
