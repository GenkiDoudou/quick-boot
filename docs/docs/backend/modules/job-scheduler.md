# 定时任务（Quartz）

| 项 | 值 |
|----|-----|
| Controller | `SysJobController`、`SysJobLogController` |
| 路径 | `/monitor/job`、`/monitor/jobLog` |
| 前端 | `views/monitor/job/`、`job-log/` |
| 表 | `sys_job` 及 Quartz 表（Flyway V19+） |

## 任务管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/monitor/job/list` | 任务列表 |
| GET | `/invokeTargets` | 可选执行目标（Bean 方法） |
| GET | `/{jobId}` | 详情 |
| POST | `/edit` | 新增/修改 |
| POST | `/remove` | 删除 |
| POST | `/changeStatus` | 启停 |
| POST | `/run` | 立即执行一次 |
| POST | `/export` | 导出 |

## 任务日志

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/monitor/jobLog/list` | 执行记录 |
| GET | `/{jobLogId}` | 详情 |
| POST | `/remove`、`/clean` | 清理 |
| POST | `/export` | 导出 |

## 执行机制

- `AbstractQuartzJob` 封装执行与异常记录
- `ScheduleUtils` 维护 Cron 触发器
- `JobInvokeTargetRegistry` 注册可调用的 Spring Bean 方法
- 示例任务：`QcDemoTask`

## 配置

- `qc.monitor.job.export-limit`：导出条数上限
- 多实例部署时需保证 Quartz 集群表锁正确（见 Flyway `V21`、`V22`）

## 前端

任务编辑表单使用 `components/Crontab` 生成 Cron 表达式；列表页为 C7JsonTable 模式。

## 相关文档

- [监控审计](./monitor-audit)
