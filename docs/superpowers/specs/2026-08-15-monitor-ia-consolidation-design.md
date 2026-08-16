# 监控信息架构收敛设计

日期：2026-08-15  
状态：**已过时 — 请改读** [`2026-08-15-monitor-lite-trace-only-design.md`](./2026-08-15-monitor-lite-trace-only-design.md)

> 本文仍描述「用户行为 / clientTrack 双轨」与「暂不 DROP 表」。产品已确认：DROP `sys_client_track`、无行为产品线、概览删除访问卡片、查询台并入请求链路。

---

（以下原文保留作历史，勿再按此实施。）

# 监控信息架构收敛设计（历史稿）

日期：2026-08-15  
状态：**已修订（clientTrack 整套下线）**  
关联：`docs/superpowers/specs/2026-08-14-lite-frontend-rum-design.md`（Lite Trace 能力源）  
动机：前端监控 / 事件链路 / 行为轨迹 / 全链路监控 / 链路 Trace / 链路查询台命名相近、职责重叠，排障心智混乱。

> **2026-08-15 修订：** 按产品决策 **删除 clientTrack 上报与相关前后端/菜单**（含依赖它的「全链路监控」）。前端仅保留 Lite RUM → `/monitor/liteTrace/rum/ingest`。`sys_client_track` 表暂不 DROP。控制台以「请求链路」（原链路 Trace，可并入查询条）为唯一链路入口；「用户行为」合成页取消。

## 1. 目标与非目标

### 1.1 目标

- 监控侧「链路/轨迹」收敛为 **两条产品线**：
  - **请求链路**：以 `traceId` 为中心的 FE/BE/SQL/异常瀑布（研发默认排障入口）。
  - **用户行为**：以操作/会话为中心的批次与页面轨迹还原。
- 同一轮落地：**入口收敛（菜单+合成页+旧路由 redirect）** + **采集/存储收敛声明与可执行动作（不停写默认仍开 clientTrack，但主路径与废弃清单明确）**。
- 去掉第三条「全链路拼图」叙事，避免与请求链路抢名。

### 1.2 非目标

- 不重做慢 SQL、在线用户、操作日志、定时任务等运维明细菜单。
- 不做 PV/健康分/ISP 运营大盘。
- **本轮不 DROP 表**；物理删表另开变更。
- 不在本轮默认关闭 clientTrack 上报（见 §5）。

## 2. 已确认决策摘要

| 项 | 选择 |
|----|------|
| 优化形态 | 信息架构 + 页面合并；表可双轨过渡 |
| 默认排障入口 | 请求链路（Lite Trace / A′+E） |
| 全链路监控 | 菜单隐藏，redirect 到请求链路；API 可留但废弃 |
| 行为三页 | 合成一个「用户行为」页（列表 + 轨迹 Tab + 事件抽屉） |
| Trace + 查询台 | 合成一个「请求链路」页 |
| 落地节奏 | ①入口与③收敛声明/动作同一轮；clientTrack **默认仍开启** |
| 菜单树 | 方案 1：双入口平铺（不增加「排障」父目录） |

## 3. 产品线对照

| 产品线 | 回答的问题 | 主关联 ID | 目标态存储 | 过渡态 |
|--------|------------|-----------|------------|--------|
| 请求链路 | 这次请求卡在哪？同链是否有 SQL/异常？ | `traceId`（辅 `operationId`） | `sys_trace_index` + `sys_trace_span` | 已实现 Ingest/投影 |
| 用户行为 | 用户点了什么、怎么跳页？ | `operationId` / session / pageVisit | 长期可收敛为行为摘要或 RUM action 投影 | 本轮仍读 `sys_client_track` |

慢 SQL / oper_log 等继续作为**运维明细**，可被投影进 span，但不单独构成「第三条链路产品」。

## 4. 菜单与路由

### 4.1 目标菜单（系统监控下）

```
系统监控
├── 监控概览          （保留）
├── 请求链路          path: requestTrace → component: monitor/liteTrace/index
├── 用户行为          path: userBehavior → component: monitor/userBehavior/index
├── 慢 SQL / 在线用户 / 操作日志 / 定时任务…（保留）
└── （停用/隐藏）原六入口
```

### 4.2 旧入口处置

| 原菜单 | menu 处置 | 路由 |
|--------|-----------|------|
| 前端监控 `clientTrack` | 隐藏/停用 | redirect → `/monitor/userBehavior`（或带 tab=batches） |
| 事件链路 `clientTrackEvents` | 隐藏/停用 | redirect → `/monitor/userBehavior`（tab=batches，可深链打开明细） |
| 行为轨迹 `clientTrackTimeline` | 隐藏/停用 | redirect → `/monitor/userBehavior`（tab=timeline） |
| 全链路监控 `traceChain` | 隐藏/停用 | redirect → `/monitor/requestTrace` |
| 链路 Trace `liteTrace` | 改名为「请求链路」或隐藏后由新 path 取代 | 兼容：`liteTrace` → `requestTrace` |
| 链路查询台 `liteTraceQuery` | 隐藏/停用 | redirect → `/monitor/requestTrace`（可带 `q=`） |

权限字符：

- 请求链路：沿用 `monitor:liteTrace:query`（避免大面积改角色授权）；菜单显示名改为「请求链路」。
- 用户行为：沿用 `monitor:clientTrack:list` / `remove`。
- `monitor:traceChain:query`：菜单隐藏后可保留权限码，不再授予新角色文档说明。

实现方式：Flyway 更新 `sys_menu`（`menu_name` / `path` / `component` / `status` / `visible` 按库表实际字段），并为旧 path 在前端 router 增加 redirect（双保险）。

## 5. 页面设计

### 5.1 请求链路（合成 A′ + E）

单页结构（自上而下）：

1. **命令条**：解析 `traceId:` / `operationId:` / `uin:` / `page:` / `action:` / `entry:`，Run 触发列表刷新。
2. **来源筛选 chip**：全部 / 浏览器 / 纯 API / 任务（`root_source`）。
3. **主体**：左 Trace 列表 + 右详情头 + Span 瀑布（含类名.方法名展示优化）。
4. **空态 / 错误态**：无数据、无权限、接口失败文案明确。

不再单独提供「链路查询台」菜单。查询能力全部并入本页顶栏。

数据 API：现有 `/monitor/liteTrace/**` 只读与 ingest，不改契约（除非发现缺陷）。

### 5.2 用户行为（合成三页）

单页 Tab：

| Tab key | 能力 | 复用 |
|---------|------|------|
| `batches` | 批次列表 + 行内打开事件明细（抽屉/Dialog） | `clientTrack/index` + `events` 能力 |
| `timeline` | 页面跳转轨迹 | `clientTrack/timeline` |

跨产品跳转：

- 批次或事件上若存在 `operationId` / `traceId`，提供 **「查看请求链路」**，跳转  
  `/monitor/requestTrace?q=operationId:xxx` 或 `traceId:xxx`。

### 5.3 全链路监控旧页

- 菜单隐藏；直链 redirect 到请求链路。
- 后端 `traceChain` API：**本轮不删**，代码与文档标注 deprecated；无新功能。
- **不**保留「高级拼图」菜单入口，避免第三条叙事回流。

## 6. 采集与存储收敛（本轮③的范围）

### 6.1 主路径（必须保持）

- Web：**Lite RUM** → `/monitor/liteTrace/rum/ingest` → `sys_rum_event`（可选）+ 投影 `fe_*` spans / index。
- 后端：Filter 透传/生成 `X-Trace-Id`；access / 慢 SQL 事件 / 异常 → `service` / `sql` / `be_error` spans。

### 6.2 clientTrack（本轮默认仍开启）

- `VITE_APP_MONITOR_ENABLED`（或等价）**默认 true**，行为 Tab 继续有新数据。
- 与 Lite RUM **允许双写**；文档写明：排障以请求链路为准，行为还原以用户行为为准。
- 增加或沿用显式开关；关闭后行为页主要查历史，不阻塞业务页（失败静默）。

### 6.3 废弃清单（本轮必须落地的「声明 + 菜单动作」）

| 资产 | 本轮动作 | 后续（另变更） |
|------|----------|----------------|
| 六旧菜单 | 隐藏/改名/redirect | 可删除菜单行 |
| traceChain 控制台 | 下线入口 | 可删前端页 |
| traceChain API | `@Deprecated` 注释 + 设计/AGENTS 提及 | 可删 API |
| `sys_client_track` | 继续读写 | 评估停写 → 只读 → DROP |
| clientTrack 上报 | 默认开，可关 | 行为能力迁到 RUM action 后再默认关 |
| Lite 投影表 | 主存储 | TTL Job（可另开） |

### 6.4 与「6C 同一轮」的对齐说明

用户要求①与③同一轮：本设计将③落实为 **入口消灭歧义 + 主路径写清 + 废弃清单可执行（菜单/redirect/文档/注解）**，而非本轮物理删表或默认停 clientTrack。若需「默认关 clientTrack」，须另批确认（曾列为选项 B，未采纳）。

## 7. 实现任务拆分（供后续 plan / OpenSpec）

1. Flyway：菜单改名「请求链路」「用户行为」；隐藏旧菜单；授权保持可用。
2. 前端 router：旧 path redirect；新 path 注册。
3. `monitor/liteTrace/index.vue`：合并查询台命令条（吃掉 query.vue 能力）。
4. 新增 `monitor/userBehavior/index.vue`：Tab 组合批次/轨迹/事件抽屉 + 跳转请求链路。
5. 文档：本 spec；Lite Trace 设计文首增加「控制台入口已收敛」指针。
6. 代码：`traceChain` Controller/Service 标 deprecated；README/监控说明更新废弃表。

## 8. 验收标准

1. 监控菜单仅见「请求链路」「用户行为」作为链路/行为类入口（外加原有运维项），无六旧名并存。
2. 访问旧 URL 自动进入对应新页，不 404。
3. 请求链路：命令查询 + 来源筛选 + 瀑布可用；类名.方法名展示可读。
4. 用户行为：两 Tab 可用；从批次能跳到请求链路并带上 ID。
5. 全链路监控菜单不可见；直链进入请求链路。
6. Lite RUM 与（默认开启的）clientTrack 均不导致业务页报错；非法 appId 仍不影响主流程。

## 9. 风险

- 双 SDK 流量与存储体积：用开关与 TTL 缓解；排障时注意别在两套 UI 各查一半。
- 权限码未改名可能导致文档与菜单名不一致：在角色权限文案中注明「请求链路 = liteTrace 权限」。
- 合成页若简单 iframe 式堆叠，体验差：优先抽 Tab/抽屉复用现有组件逻辑，而非复制三份页面。

## 10. 开放问题（已关闭）

- 菜单方案：方案 1。
- clientTrack 默认：仍开启。
- 全链路：隐藏 + redirect，不留高级入口。
