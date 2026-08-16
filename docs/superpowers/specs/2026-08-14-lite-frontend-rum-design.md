# 精简版链路监控（Lite Trace / RUM）设计

日期：2026-08-14  
状态：已确认方向（开发向链路；控制台采用 A′ + E）  
**入口收敛（2026-08-15）：** 管理端将 A′+E 合成「请求链路」，行为三页合成「用户行为」；见 `docs/superpowers/specs/2026-08-15-monitor-ia-consolidation-design.md`。  
来源：腾讯云前端性能监控能力对照 → 精简裁剪 → 开发排障链路定稿  
原型：

- `docs/demo/lite-rum-chain-picker.html`（方案对比）
- `docs/demo/lite-rum-chain-a-unified.html`（**A′** Trace 统一入口，含纯后端）
- `docs/demo/lite-rum-chain-e.html`（**E** 查询控制台）
- `docs/demo/lite-rum-chain-d-unified.html`（**D′** Issue 统一，可选配套）

范围说明：本文描述**开发向全链路排障**能力：前端 RUM 事件与后端接口/SQL/异常通过 `traceId` 汇合。不依赖、不绑定本仓库既有监控菜单实现，但存储上允许与慢 SQL / access 等域表并存并投影。

## 1. 背景与目标

### 1.1 要解决什么（开发视角）

开发接线上反馈时，需要快速回答：

1. 这条请求/这次操作整条链耗时卡在哪（前端、网关、服务、SQL）？  
2. 前端报错与后端异常是否同一 `traceId`？  
3. 无浏览器场景（OpenAPI、定时任务）能否同样查链？  
4. 能否用 `traceId` / `operationId` / `uin` 等从工单直接搜到链？

**不做产品重心：** PV/UV、健康分大盘、地区/ISP 饼图、告警运营台（可后置；字段与汇总表可预留但不作为本期主交付）。

### 1.2 已确认决策

| 项 | 选择 |
|----|------|
| 主用户 | 研发排障 |
| 控制台主交互 | **A′ Trace 统一入口（瀑布）** + **E 查询控制台** |
| 可选配套 | D′ Issue→Chain（FE 错误 + BE 异常同一收件箱） |
| 覆盖来源 | 浏览器 RUM、纯 API（OpenAPI/伙伴）、Job/任务；**第一公民是 `traceId`** |
| 采集字段 | 见 §3.0（含 `fromPage`/`action`/`operationId`/`traceId`/`env.ua`；IP 由服务端补） |
| 存储 | **域明细（可选）+ `trace_index` + `trace_span` 投影**；排障 UI 只读 index/span |
| 应用模型 | 协议与表预留 `appId`；一期可单应用 |
| 统计大盘 | **非本期主目标**（原总览/健康分/告警降为 backlog） |

### 1.3 非目标（本期）

- PV/健康分/成功率运营大盘、地区/ISP/机型维度大盘  
- 页面性能瀑布（DNS/TCP/SSL/CWV 专项页）  
- Session Replay、Sourcemap、全站 click 热力图  
- 完整告警值班平台  
- H5 SDK 实现（协议兼容即可）  
- 控制台方案 B/C 作为主入口（B 操作台、C 双栏可作后续增强，数据模型已覆盖）

### 1.4 方案 A′ / E 与纯后端适用性

| 方案 | 无前端、仅后端接口 | 说明 |
|------|-------------------|------|
| A′ Trace | **适用** | 根节点用 `entry` + `caller`，不依赖 page/action |
| E 查询台 | **适用** | `traceId:` / `entry` 等语法；无 UI 字段时可空 |
| D′ Issue | **有条件适用** | 用后端异常 fingerprint；与 FE Issue 并列 |
| 原 C 双栏 FE\|BE | 不适用原设计 | 若做对照应改为 Client\|Server |

## 2. 架构

```text
[Web SDK]                  [网关 / Filter]           [SQL 拦截]        [异常处理]
   pv/action/api/error          access log               sql log           be error
         \                         |                       |                  /
          \                        |                       |                 /
           +-----------------------+-----------------------+----------------+
                                   |
                                   v
                    投影写入（同步或异步）
                                   |
                    +--------------+--------------+
                    |                             |
                    v                             v
             trace_index                    trace_span
             （链摘要，检索）                 （片段，瀑布）
                    |
        （可选保留域表） rum_event / svc_access_log / sql_* / ...

[控制台]
  A′ 列表/瀑布  <-- trace_index + trace_span
  E  查询台     <-- trace_index（命中后再拉 span dump）
  D′ Issue      <-- issue_*（sample_trace_id → 同上 span）
```

| 模块 | 职责 |
|------|------|
| Web SDK | 采集 §3.0 事件；API 注入 `traceId` 请求头 |
| 后端埋点 | 入口生成或透传 `traceId`；access / SQL / 异常写入时带同一 ID |
| 投影器 | 各来源 → upsert `trace_index` + insert `trace_span` |
| 控制台 A′ | 按来源过滤 Trace，展示 Span 瀑布 |
| 控制台 E | 命令条多键查询，展开 chain dump |
| 控制台 D′（可选） | FE/BE Issue 列表 → 下钻 Chain/Trace |

## 3. 上报与采集协议

### 3.0 前端采集字段清单（总表）

目标口径：**当前页、上个页、触发操作、接口、traceId**，以及 **UA / IP 等环境信息**，并能和后端日志对上。  
分层：`必采` = RUM/排障基础；`关联必采` = 操作级/全链路；`环境必采` = 环境排障；`可选` = 增强。

#### 3.0.1 请求信封（每次批量上报）

| 字段 | 分层 | 说明 |
|------|------|------|
| `appId` | 必采 | 应用标识 |
| `sdkVersion` | 必采 | SDK 版本 |
| `clientTime` | 必采 | 客户端打包时间戳（ms） |
| `events` | 必采 | 事件数组 |
| `env` | 环境必采 | 本批次环境快照（见 §3.0.2b） |

#### 3.0.2 所有事件公共字段

| 字段 | 分层 | 说明 |
|------|------|------|
| `type` | 必采 | `pv` \| `action` \| `api` \| `error` |
| `ts` | 必采 | 事件发生时间（ms） |
| `page` | 必采 | **当前页面**（纯后端链路无此事件） |
| `fromPage` | 关联必采 | **上个页面** |
| `sessionId` | 必采 | 浏览器会话 ID |
| `operationId` | 关联必采 | 一次用户操作 ID；由 `action` 创建 |
| `traceId` | 关联必采 | 单次 HTTP 关联 ID；写入请求头 |
| `release` | 可选 | 前端版本 |
| `uin` | 可选 | 用户唯一标识 |

#### 3.0.2b 环境与网络上下文（UA / IP）

**原则：** UA 等由前端写入 `env`；**公网 IP 由 Ingest/网关解析**，前端不自报。

**前端 `env`：** `ua`（环境必采）、`language`、分辨率/视口、`timezone`、`networkType` 等可选。  
**服务端补全：** `clientIp`（环境必采）、`userAgent` 回退、可选 `geo*` / `isp`。

#### 3.0.3～3.0.6 事件类型

- **`pv`：** 路由进入；`page`/`fromPage`；可选 `stayMs`  
- **`action`：** 白名单/`rum.action`；`action`、`operationId`；禁止全站 click  
- **`api`：** method/url/status/ok/durationMs + `traceId`/`operationId`  
- **`error`：** name/message/stack/fingerprint；可选挂 `operationId`/`traceId`  

#### 3.0.7 链路串法

```text
env + clientIp
pv / action(operationId) / api(traceId) / error
后端 access / sql / exception（同一 traceId）
→ trace_span 瀑布；trace_index 可检索
```

#### 3.0.8 明确不采

前端自报 IP、全站 click、输入内容、Cookie/Token 原文、CWV/资源逐条（本期）、无库时的 ISP 大盘。

### 3.1 Ingest 请求示例

```http
POST /rum/ingest
```

```json
{
  "appId": "web-admin",
  "sdkVersion": "0.1.0",
  "clientTime": 1723622400000,
  "env": { "ua": "Mozilla/5.0 ...", "language": "zh-CN" },
  "events": []
}
```

### 3.2 后端侧必须透传的关联

| 头/字段 | 说明 |
|---------|------|
| `X-Trace-Id` 或 `X-Request-Id` | 与前端 `api.traceId` 一致；无则入口生成 |
| （日志字段）`trace_id` | access、SQL、异常日志强制打印/落库 |

无 `trace_id` 的 SQL/日志**不能进入链路瀑布**，仅可进旁路域表。

### 3.4 SDK 默认开关

- 开：PV、错误、API、`fromPage`、`traceId` 注入、`env.ua`  
- 开（白名单）：`action`  
- 关：全量 click、自报 IP、CWV、默认解析 `retCode`

## 4. 存储设计

### 4.1 总原则

- **排障 UI（A′/E）只读 `trace_index` + `trace_span`**，避免运行时 JOIN 多张域表拼瀑布。  
- 各来源可先写域明细，再**投影**到 index/span（同步或异步均可；一期允许同事务双写）。  
- 热数据 TTL 建议 **7～14 天**（开发排障）；SQL 文本存指纹 + 截断。

```text
写入方 →（可选域表）→ 投影 → trace_index + trace_span → A′ / E
```

### 4.2 `trace_index`（一条 Trace 一行）

用途：A′ 左侧列表、E 多键检索。

| 字段 | 说明 |
|------|------|
| `trace_id` | PK |
| `app_id` | 应用 |
| `root_source` | `browser` \| `api` \| `job` |
| `entry` | 根调用：`POST /api/...`、`job:xxx`、`POST /openapi/...` |
| `caller` | `quick-ui` / `partner-erp` / `quartz` 等 |
| `operation_id` | 可空 |
| `action` / `page` / `from_page` | 可空（纯 API/Job 无） |
| `uin` | 可空 |
| `ok` / `status` | 整链成败 |
| `duration_ms` | 整链耗时 |
| `started_at` / `ended_at` | |
| `client_ip` / `ua` | 有则记 |
| `error_summary` | 首错摘要 |

**索引（支撑 E）：**

- PK `trace_id`  
- `(operation_id, started_at)`  
- `(uin, started_at)`  
- `(entry, started_at)`  
- `(action, started_at)`  
- `(root_source, started_at)`  
- `(started_at)`  

### 4.3 `trace_span`（链上每一段）

用途：A′ 瀑布、E 的 chain dump。

| 字段 | 说明 |
|------|------|
| `span_id` | PK |
| `trace_id` | 关联 index |
| `parent_span_id` | 可空，拼树 |
| `source` | 见下表 |
| `name` | 展示名 |
| `service` | `browser` / `order-service` / `mysql` / `api-gateway` … |
| `start_ts` / `end_ts` 或 `start_offset_ms` + `duration_ms` | 绘制瀑布 |
| `ok` / `status` | |
| `attrs` | JSON：sql 指纹/截断、http、page、fingerprint、行号等 |

**`source` 枚举与来源映射：**

| source | 来源 |
|--------|------|
| `fe_action` | 前端 action |
| `fe_api` | 前端 api 测速 |
| `fe_error` | 前端 JS/Promise 错误 |
| `gateway` | 网关 |
| `service` | 后端接口处理 |
| `sql` | SQL 日志 / 慢 SQL |
| `be_error` | 后端异常 |

**索引：** `(trace_id, start_ts)`（或 `start_offset_ms`）。

### 4.4 可选域明细表

| 表 | 用途 |
|----|------|
| `rum_event` | 前端原始事件审计、重放投影 |
| `svc_access_log` | 接口访问原日志 |
| `sql_*` / 慢 SQL 表 | SQL 原记录 |
| `issue` / `issue_event` | D′ 指纹聚合（`kind=fe\|be`，`sample_trace_id`） |

域表**不是** A′/E 查询主路径；缺省可只落 index+span。

### 4.5 写入时序（一次请求）

1. 入口（SDK / 网关 / Job）生成或透传 `traceId`。  
2. 前端上报 →（`rum_event`）→ 投影 `fe_*` spans + upsert `trace_index`。  
3. 后端 Filter → access → 投影 `gateway`/`service` span + 更新 index。  
4. SQL 拦截器带 `traceId` → 投影 `sql` span。  
5. 异常处理 → 投影 `be_error` + 更新 `index.ok/error_summary`；可选 upsert `issue`。

### 4.6 与 A′ / E 的读路径

| UI | 读模型 |
|----|--------|
| A′ 列表 | `trace_index`（`root_source` 过滤） |
| A′ 瀑布 | `trace_span WHERE trace_id=? ORDER BY start` |
| E Run | 解析 `traceId:`/`operationId:`/`uin:`/`page:`/`action:` → 查 `trace_index` → 选中行拉 span 生成 dump |
| D′ | `issue` 列表 → `sample_trace_id` → 同 A′ 瀑布 |

### 4.7 原「分钟汇总 / 健康分」表（backlog）

若后续要做运营大盘，可另建 `rum_metric_1m` 等；**与链路排障存储解耦**，不阻塞本期。

## 5. 控制台实现设计

### 5.1 信息架构（本期）

```text
链路监控
  ├─ Trace 列表与瀑布（A′）     ← 主工作面
  ├─ 查询控制台（E）           ← 熟手 / 工单粘贴 ID
  └─ Issue（D′，可选二期同发） ← 从报错进链
```

静态原型见文首路径。视觉：深色、等宽 ID、无健康分/儿戏 Banner。

### 5.2 A′ Trace 统一入口

**布局：** 左列表 + 右详情（头信息 + Span 瀑布）。

**列表字段：** `traceId`、来源徽章（浏览器/纯 API/任务）、OK/ERR、`entry`、耗时、`caller`、时间。  
**过滤：** 全部 / 浏览器 / 纯 API / 任务；支持搜 `traceId`/`entry`/`caller`。  
**详情头：** `traceId`、`root_source`、`entry`、`caller`、可选 `operationId`/`page`/`action`、IP。  
**瀑布：** 按时间偏移绘制；颜色区分 fe / gateway / service / sql / error。  
**纯后端示例：** OpenAPI 下单（无 page）、Job 对账（caller=quartz）。

### 5.3 E 查询控制台

**布局：** 顶部命令条 + 结果表 + 展开 CHAIN DUMP。

**查询语法（空格分隔）：**

- `traceId:`  
- `operationId:`  
- `uin:`  
- `page:`  
- `action:`  

（实现可扩展 `entry:` / `caller:`，与 `trace_index` 索引一致。）

**结果列：** type、id、summary、status、ms。  
**展开：** 文本型 span 转储（便于复制到工单）。

### 5.4 D′ Issue（可选）

- 同一收件箱：`kind=fe|be`  
- FE：JS fingerprint；BE：异常类名+消息+首帧  
- 详情：stack + context + chain 节点 + 跳转 A′  

### 5.5 明确不做的控制台能力（本期）

数据总览健康分、API TOP 运营页、告警规则页、地区饼图、原方案 C「左 FE 右 BE」作为默认首页。

## 6. Web SDK 行为

| 能力 | 行为 |
|------|------|
| 初始化 | `appId`、`ingestUrl`；可选 `release`、`uin` |
| PV | 首屏 + SPA 路由变化 |
| 错误 | `onerror` / `unhandledrejection` + fingerprint |
| API | 包装 fetch/XHR；注入 `traceId` 头；记录耗时与状态 |
| action | 显式或 `data-rum-action` |
| 上报 | 队列批量；`sendBeacon` 卸载 flush |
| 自保护 | 不上报 ingest 自身；fingerprint 短窗限流 |

## 7. 后端埋点约定

| 点 | 要求 |
|----|------|
| HTTP 入口 | 读取或生成 `traceId`，写入 MDC/上下文并回传响应头（可选） |
| Access 日志 | 必含 `trace_id`、path、status、duration |
| SQL | 拦截器从上下文取 `trace_id`；超阈值标记 slow |
| 异常 | 记录 stack + `trace_id`；投影 `be_error`；可选更新 Issue |
| OpenAPI / Job | 同样生成 `traceId`；`root_source=api|job`，`entry`/`caller` 写入 index |

## 8. 可靠性与约束

- RUM Ingest：限流、body 限制、`appId` 白名单；可接受少量重复事件。  
- 投影失败：域表可保留，需有补偿/重放任务（二期可做；一期至少日志告警）。  
- 无 `trace_id` 不进 span。  
- TTL 定时清理 index/span/域表。  
- IP/UA/uin 访问控制与脱敏展示。

## 9. 分期

| 期 | 内容 |
|----|------|
| **①** | 前端 SDK（§3）+ 后端透传 `traceId` + `trace_index`/`trace_span` 投影 + 控制台 **A′ + E** |
| **①b（可同发）** | D′ Issue（FE+BE） |
| **②** | 操作工作台（方案 B）、Client\|Server 对照、更富查询语法、投影补偿 |
| **③** | H5 SDK、多应用、运营大盘/告警（若需要）、页面性能指标 |

**Backlog：** Sourcemap、Session Replay、ISP 大盘、完整值班告警。

## 10. 验收与测试（① 期）

### 10.1 验收

1. 浏览器：一次失败下单，A′ 可见含 fe_api / service / sql / error 的瀑布，同一 `traceId`。  
2. 纯 API：无 page/action 的 OpenAPI 调用，A′ 来源=纯 API，瀑布仍完整。  
3. Job：任务链路 `root_source=job` 可查。  
4. E：`traceId:xxx`、`operationId:xxx`、`uin:xxx` 能命中并展开 dump。  
5. 后端日志与 span 中 `trace_id` 一致；故意去掉透传则 SQL 不进链（或仅域表有）。  
6. SDK 关闭或错误 `appId` 不影响业务站。

### 10.2 测试要点

- SDK：`traceId` 头注入、url 脱敏、fingerprint  
- 投影：多来源 fixture → index 字段与 span 顺序  
- E 查询解析与索引命中  
- TTL 清理不误删未到期数据  

## 11. 成功标准

本期交付后开发应能：

- 用 **A′** 看清任意 `traceId` 的前后端+SQL 片段与耗时；  
- 用 **E** 从工单 ID 秒级定位到链；  
- 在**没有前端**时仍能用同一套模型查 OpenAPI/Job 链路；  
- （若含 D′）从报错指纹一键进完整链。
