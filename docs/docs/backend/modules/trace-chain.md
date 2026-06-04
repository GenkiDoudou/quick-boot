# 全链路监控

## 概述

按 **operationId**、**traceId**、页面访问 ID、会话等维度，将 **前端页面跳转与行为明细**、**HTTP 请求**、**操作日志**、**慢 SQL** 聚合为一张可交互的 Network 视图，用于排查一次用户操作的前后端完整路径。

| 项 | 值 |
|----|-----|
| Controller | `SysTraceChainController` |
| 路径前缀 | `/monitor/traceChain` |
| 前端 | `quick-ui/src/views/monitor/traceChain/index.vue` |
| 网络图逻辑 | `useTraceChainNetwork.js` |
| 菜单 | 系统监控 → 全链路监控（`menu_id=2267`，`perms=monitor:traceChain:query`） |
| 迁移 | `V54__sys_trace_chain_menu.sql`（含 `sys_slow_sql.client_operation_id` 索引） |

接口标注 `@IgnoreLogger`，避免查询本身写入操作日志。

## 接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/graph` | `monitor:traceChain:query` | 返回 `TraceChainGraphVo`（节点与边） |

### 查询参数（`TraceChainQueryBo`）

至少提供下列之一组合以缩小范围：

| 参数 | 说明 |
|------|------|
| `operationId` | 一次用户操作（前端 SDK 生成） |
| `traceId` | 单次 HTTP 链路 ID |
| `batchId` | 批次 |
| `pageVisitId` | 页面访问 |
| `sessionId` | 会话 |
| `browserVisitId` | 浏览器访问 |
| `userName` | 用户名模糊 |
| `createTimeRange` | 日期范围 |

## 数据来源

| 类型 | 来源 |
|------|------|
| 页面跳转 / 行为 | 客户端埋点上报（见 [用户行为监控](../../frontend/modules/user-behavior-monitor)） |
| 操作日志 | `sys_oper_log` |
| 慢 SQL | `sys_slow_sql`（`client_operation_id` / `trace_id`） |
| HTTP | 与 trace、操作日志关联的请求摘要 |

## 页面能力

- 顶部筛选表单 + 摘要告警（无数据 / 部分缺失等）。
- **Network 视图**：节点类型区分前端行为、后端接口、SQL 等；支持缩放与选中查看详情。
- 与 [慢 SQL 日志](./slow-sql) 列表可交叉使用相同 `traceId` / `operationId` 检索。

## 原型参考

仓库 `原型/` 目录含多版 HTML 原型（如 `monitor-trace-chain-prototype-v3-network.html`），与现网 Vue 页交互思路一致，仅供设计对照。

## 相关文档

- [慢 SQL 日志](./slow-sql)
- [监控审计](./monitor-audit)
- [用户行为监控（前端采集）](../../frontend/modules/user-behavior-monitor)
