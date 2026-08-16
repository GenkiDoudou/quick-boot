# 监控体系重设计（Lite Trace 单主线）

日期：2026-08-15  
状态：已被控制台 v2 扩展  
**后续控制台：** 见 [`2026-08-15-monitor-console-v2-design.md`](./2026-08-15-monitor-console-v2-design.md)（删概览、请求链路增强、用户行为、日志中心）。

取代：`2026-08-15-monitor-ia-consolidation-design.md` 中仍含「用户行为 / clientTrack 双轨」的过时条款。

## 决策摘要

| # | 选择 |
|---|------|
| 表 | **DROP** `sys_client_track` |
| 菜单 | 概览 + **请求链路** + 运维明细（慢 SQL/在线/操作日志/登录/任务） |
| 概览访问卡片 | **删除**（不改成链路健康） |
| 查询台 | 并入请求链路顶栏 |
| 范围 | 本轮：DROP + 菜单/概览 + 合并查询条 |

## 目标菜单

```
系统监控
├── 监控概览
├── 请求链路          （Lite Trace：命令查询 + 列表 + 瀑布）
├── 慢 SQL
├── 在线用户
├── 操作日志 / 登录日志
└── 定时任务 / 任务日志
```

已下线：前端监控、事件链路、行为轨迹、全链路监控、链路查询台、用户行为产品线。

## 数据

| 用途 | 表 |
|------|-----|
| 请求链路 | `sys_trace_index` / `sys_trace_span`（+ 可选 `sys_rum_event`） |
| 慢 SQL | `sys_slow_sql` |
| 操作/登录 | `sys_oper_log` / `sys_logininfor` |
| ~~用户行为批次~~ | ~~`sys_client_track`~~ **已删除** |

## 采集

- 唯一前端上报：`POST /monitor/liteTrace/rum/ingest`
- 后端：Filter / 慢 SQL 事件 / 异常 → span 投影

## 验收

1. 库中无 `sys_client_track`
2. 菜单无行为/全链路/独立查询台
3. 概览无「访问与行为」类空卡片
4. 请求链路顶栏支持 `traceId:` / `operationId:` 等命令查询 + 瀑布
5. Network 仅见 Lite RUM 上报
6. 源码无 `clienttrack` / `tracechain` / `userMonitor` 死代码（历史 Flyway V17 建表语句保留，由 V25/V26 退役）
