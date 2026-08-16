# 监控控制台 v2 设计

日期：2026-08-15  
状态：已确认（待实现）  
关联：

- `2026-08-14-lite-frontend-rum-design.md`（Lite Trace 能力源）
- `2026-08-15-monitor-lite-trace-only-design.md`（单主线 / DROP clientTrack；本文件在其上扩展控制台）

## 0. 决策摘要

| # | 选择 |
|---|------|
| 监控概览 | **删除**菜单 + 前后端（BI 另做） |
| 行为分析 | 用 Lite RUM（`pv`/`action`）重建页→操作→页；**不**恢复 `sys_client_track` |
| 行为入口 | **独立菜单**「用户行为」 |
| 统一日志 | 操作日志 + 登录日志 + 慢 SQL |
| 日志形态 | 新菜单「日志中心」合并列表 + 维度筛选 |
| 节奏 | **同一迭代四项全做** |
| 请求链路 enrichment | Index 全字段 + Span `attrsJson` 展开 + 关联跳转 |

落地方式：单 OpenSpec change（建议名 `monitor-console-v2`），按依赖顺序交付。

## 1. 目标与非目标

### 1.1 目标

1. 去掉监控概览，菜单收敛为排障与运维明细。  
2. 请求链路可按**时间区间**、**成功/失败**筛选；右侧详情尽可能丰富并支持关联跳转。  
3. 提供「用户行为」：按用户/会话还原 **页面 → 按钮/操作 → 下一页面** 时间线，可下钻 `traceId`。  
4. 提供「日志中心」：三源日志统一检索入口。

### 1.2 非目标

- BI 大盘 / 监控概览指标重建  
- 恢复 `sys_client_track` 或旧行为三页  
- 日志中心纳入 Lite Trace 事件、业务文件日志、ELK  
- Session Replay、全站无埋点 click 热力  
- 日志中心「真 SQL UNION 完美分页」二期优化（一期允许近似分页，见 §6）

## 2. 信息架构

```
系统监控
├── 请求链路          （Lite Trace：命令查询 + 时间/结果筛选 + 瀑布 enrichment）
├── 用户行为          （新建：会话/用户操作时间线）
├── 日志中心          （新建：oper + login + slow_sql 合并）
├── 慢 SQL
├── 在线用户
├── 操作日志 / 登录日志
└── 定时任务 / 任务日志
```

已删除/继续下线：监控概览、前端监控、事件链路、行为轨迹（旧）、全链路监控、链路查询台。

## 3. 下线监控概览

### 3.1 菜单

- Flyway：将 overview 菜单（`menu_id` 2169/2170）`status='1'`（停用）或删除角色关联后停用；与现网 V25 风格一致。

### 3.2 代码删除

| 层 | 路径（示意） |
|----|----------------|
| FE | `quick-ui/src/views/monitor/overview/`、`api/monitor/overview.js` |
| BE | `monitor.internal.overview` 包（controller/service/dto 等） |

不得残留菜单 component 指向已删页面。

## 4. 请求链路增强

### 4.1 查询

在现有命令条 / 来源 chip 之上增加：

| 控件 | 映射 |
|------|------|
| 时间区间 | `beginTime` / `endTime` → `sys_trace_index.started_at`（含起不含止或闭区间，实现时统一并文档化） |
| 结果 | 全部 / 成功 `okFlag=1` / 失败 `okFlag=0` |
| 默认窗 | 近 24 小时（可改） |

后端：`TraceIndexQueryBo` 增加上述字段；列表 SQL/Wrapper 生效。

### 4.2 右侧 enrichment

**详情头（Index）** 尽量展示：`traceId`、`operationId`、`uin`、`rootSource`、`entryName`、`callerName`、`fromPage`→`pagePath`、`actionName`、`okFlag`、`statusCode`、`durationMs`、`startedAt`/`endedAt`、`clientIp`、`ua`、`errorSummary`。

**Span 瀑布**

- 行：名称（可缩写）+ 来源类型 + 耗时条 + ms。  
- **可展开**：`statusCode`、`okFlag`、解析后的 `attrsJson`（SQL 文本、URL、错误摘要等键值）。  
- 无 attrs 时显示「无附加属性」。

**关联跳转（有则显示）**

- 同 `operationId`：刷新列表或打开「相关链路」。  
- `traceId` → 慢 SQL 列表（query）。  
- `traceId` / `operationId` → 操作日志（query）。  
- 可选：跳转「用户行为」并带 `uin`/`session`（若可得）。

## 5. 用户行为

### 5.1 产品形态

- 独立菜单：「用户行为」，component 如 `monitor/userBehavior/index`。  
- 左：按查询条件列出会话（或用户近期会话）。  
- 右：时间线节点序列，语义为：

```text
[pv] 页 /system/user
  └─ [action] 点击「新增」
[pv] 页 /system/user/form   (fromPage = 上一页)
  └─ [action] 提交
  └─ （可选）关联 traceId → 请求链路
```

### 5.2 数据源

- 主表：`sys_rum_event`，过滤 `event_type in ('pv','action')`（及协议内等价类型）。  
- 排序：`event_time` / `create_time`。  
- 节点展示：`page_path`、`from_page`、`payload` 内 `action`、`trace_id`、`operation_id`。

### 5.3 采集补齐（本迭代必做）

现状缺口：`sys_rum_event` 无 `uin`；ingest 未必写入 `sys_trace_index.uin`。

| 项 | 要求 |
|----|------|
| DDL | `V27`：`sys_rum_event` 增加 `uin VARCHAR(64)` + 索引 `(uin, event_time)`、`(session_id, event_time)`（若尚无） |
| Ingest | 已登录则服务端写入当前用户标识到 `rum_event.uin` 与投影 `trace_index.uin` |
| SDK | 继续上报 `sessionId` / `fromPage` / `page` / `action`；不强制前端传 uin |

**埋点范围：** 仅现有 Lite RUM `pv`（路由）+ 显式 `trackAction`；未埋点的按钮不会出现在时间线——页面需简短说明。

### 5.4 API（示意）

- `GET/POST .../monitor/userBehavior/sessions`：按 `uin` / `userName` / `sessionId` + 时间窗列会话。  
- `GET .../monitor/userBehavior/timeline`：按 `sessionId`（或 uin+窗）返回有序事件节点。

权限字建议：`monitor:userBehavior:query`。

## 6. 日志中心

### 6.1 统一行模型

| 字段 | 说明 |
|------|------|
| source | `oper` / `login` / `slow_sql` |
| occurredAt | 发生时间 |
| title | 摘要（标题/URL/SQL 截断） |
| actor | 用户名（慢 SQL 可空或取关联） |
| status | 成功/失败等归一化 |
| refId | 原表主键 |
| traceId / operationId | 有则带 |
| extra | 可选短字段（ip、耗时 ms） |

### 6.2 筛选

时间区间、来源（多选）、用户、关键字、成功/失败、可选 `traceId`。

### 6.3 实现策略（一期）

- 后端聚合：monitor 经 `system::api` 查操作/登录摘要，本模块查 `sys_slow_sql`。  
- **近似分页**：各源按时间倒序各取 `limit`（如 100～200），内存按 `occurredAt` 归并后截断为 `pageSize`；响应标明 `approximate=true`。  
- 二期（非本迭代）：真 union / 搜索引擎。

### 6.4 交互

- 新菜单「日志中心」，`monitor/logHub/index`。  
- 点击行：抽屉详情或跳转原页（`/monitor/operlog`、`logininfor`、`slowSql`）并带 id/query。  
- **保留**原操作日志 / 登录日志 / 慢 SQL 明细菜单（日志中心是汇总入口，不替代运维删清等能力）。

权限字建议：`monitor:logHub:query`（只读聚合）；写删除仍走原菜单权限。

## 7. 任务顺序（同一迭代）

1. Flyway：停用概览菜单；`rum_event.uin`；用户行为 / 日志中心菜单。  
2. 删除 overview 前后端。  
3. 请求链路：查询字段 + FE 筛选 + 右侧 enrichment + 关联跳转。  
4. Ingest 写 `uin`；用户行为 API + 页面。  
5. 日志中心聚合 API + 页面。  
6. 冒烟：菜单、筛选、时间线、三源日志、跳转。

## 8. 验收

1. 无「监控概览」菜单；overview 源码已删除。  
2. 请求链路：时间 + 成功/失败生效；右侧可展开 attrs；关联跳转可用。  
3. 用户行为：按用户/会话可见页→操作→页；有 `traceId` 可进请求链路。  
4. 日志中心：三源合并可查；可回到原明细。  
5. 新事件 ingest 后 `sys_rum_event.uin` / index.uin 在登录态非空（匿名除外）。

## 9. 风险与备注

- 行为完整度依赖 `trackAction` 覆盖率。  
- 日志中心一期分页为近似，大数据量下勿当作精确 offset 分页。  
- 与「Lite Trace 单主线」一致：仍不恢复 clientTrack 表。
