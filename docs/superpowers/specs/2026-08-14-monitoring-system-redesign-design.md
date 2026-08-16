# quickboot 监控体系重设计

日期：2026-08-14  
状态：已批准 · ①期 OpenSpec `monitor-overview` 已创建  
来源：前端监控体系文章框架对照缺口清单 + 现网慢 SQL / 前端 clientTrack / 操作与登录日志 / 全链路 / 态势大屏原型  
关联：

- `2026-08-08-slow-sql-and-frontend-monitor-migration-design.md`（已落地底座）
- `2026-08-11-system-monitor-dashboard-design.md`（静态原型与指标口径；本期将「接真 + 正式路由」从非目标改为①期目标）
- `docs/demo/system-monitor-dashboard.html`、`docs/demo/system-monitor-dashboard-metrics.sql`

## 1. 背景与目标

### 1.1 现状画像

quickboot 已具备「行为可追踪 + 慢 SQL + 全链路 + 运维日志页」能力，重心在 `quick-ui/src/monitor` 与 `quickboot-module-monitor`，并辅以 `operlog` / `logininfor` / `online` / `job`。

相对完整前端可观测体系，主要缺口是：

- 用户感知性能（Web Vitals）缺失
- 前端异常缺少 stack / 指纹 Issue / Sourcemap 级可调试性
- 态势大盘仅有 mock 原型，未进正式产品
- 无告警闭环
- quick-h5 无同协议上报

### 1.2 目标

1. 给出可演进的监控体系总览：监什么、怎么组织、怎么分期落地。  
2. 在**不推倒**现有慢 SQL / clientTrack / traceChain / 日志监控的前提下，按最小增量补齐「看得见慢、查得清错、超阈值能叫人」。  
3. 交付物：本文设计定稿后，再开**一期 OpenSpec change（含 tasks）**；后续②③期各自独立 change。

### 1.3 非目标

- AI 自愈、Session Replay、独立时序库 / 外置 Prometheus 告警平台（可作为远期选项，不纳入本体系必做）
- 白屏检测、FSP、Speed Index、曝光埋点、完整 UTM 渠道分析（backlog）
- 将 `operlog` / `job` / `online` 强行迁入 `module-monitor`
- 以 Sentry 等 SaaS 替换自研链路（允许并存，但不作为依赖）
- 本期不实现完整 Sourcemap 上传服务（②期 Issue 先做到 stack + fingerprint；Sourcemap 列为②期可选增强或紧随其后的小 change）

## 2. 已确认决策

| 项 | 选择 |
|----|------|
| 交付 | 设计文档 + 分期 OpenSpec（先①期 change） |
| 主用户 | 研发排障为主，运维投屏大盘为辅 |
| 覆盖端 | quick-ui + quick-h5 **同一上报协议**（H5 实现放③期） |
| 能力包 | **最小增量**：Web Vitals + 异常 stack/指纹 + 大盘接真 + 最小告警 |
| 告警 | 规则表 + 企微 / 钉钉 / 通用 Webhook |
| 菜单 | 保留现有监控子页 + 新增「态势总览」 |
| 数据 | 继续以现有业务表为主，RUM / 告警用旁路新表 |
| 节奏 | 分三期（见 §4） |
| 架构路径 | **方案 A · 演进增强**（扩展 `module-monitor` + 现有 SDK） |

## 3. 目标架构

```text
                    ┌──────────────────────────────────────┐
                    │  quick-ui 监控中心                    │
                    │  态势总览(新) │ 既有子页下钻            │
                    └──────────────┬───────────────────────┘
                                   │
┌──────────────┐   同协议上报     ┌─▼────────────────────────┐
│ quick-ui SDK │────────────────►│ module-monitor            │
│ (+ h5 ③)     │                 │ clientTrack / slowSql     │
└──────────────┘                 │ traceChain / overview     │
                                 │ rum(vital/issue) / alert  │
┌──────────────┐                 └─┬─────────────────────────┘
│ 后端采集      │                   │ system.api 只读
│ 慢SQL Filter  │◄──────────────────┘
│ operlog 等    │
└──────────────┘
```

**排障主路径（产品叙事）**

```text
告警 / 总览红点 → Issue 或慢 SQL / 错误 KPI
  → 全链路（operationId / traceId）
  → clientTrack 轨迹 + operlog + slowSql
```

研发第一反应：开总览 → 看哪一维异常 → 下钻；而不是先开浏览器控制台。

### 3.1 模块边界

| 位置 | 职责 |
|------|------|
| `quick-ui/src/monitor` | 采集、缓冲、脱敏、上报；插件式 collector 增量 |
| `quickboot-module-monitor` | clientTrack / slowSql / traceChain / overview / rum / alert |
| `module-system` 的 `api` | overview / alert 只读 operlog、logininfor、user、job 等；禁止 monitor 依赖 system `internal` |
| `quickboot-common` | 慢 SQL 采集与 operlog 切面保持现状；告警调度可放 monitor 或复用 quartz |

## 4. 分期计划

| 期 | 名称 | 含 | 不含 |
|----|------|----|------|
| **①** | 体系入口 + 大盘接真 | 本文定稿；`GET /monitor/overview/*`；quick-ui「态势总览」正式路由与权限；指标口径对齐 `system-monitor-dashboard-metrics.sql` 与 2026-08-11 布局 | 不改采集协议大结构；不上 RUM 新表 |
| **②** | 感知性能 + 可调试异常 | `vital` / 增强错误事件；`sys_rum_vital`、`sys_rum_issue`；SDK：web-vitals、stack+fingerprint、beforeSend、pagehide/Beacon、vital 采样；总览补 Vitals/Issue 卡片；协议文档可供 H5 对齐 | 长任务/白屏/曝光/UTM；H5 实现；告警 |
| **③** | 告警 + 多端 | `sys_alert_rule` / `sys_alert_event`；预置规则与通知；quick-h5 轻量同协议接入（PV/vital/错误） | AI 自愈、Session Replay、完整点击全埋点移植 |

**OpenSpec 建议**：每期一个 change（例如 `monitor-overview`、`monitor-rum`、`monitor-alert-h5`），避免单 change 跨三期。

## 5. 数据模型与上报协议

### 5.1 表职责

| 表 | 角色 | 动作 |
|----|------|------|
| `sys_client_track` | 原始行为批次（含扩展事件） | 表结构可不动；协议扩展 |
| `sys_slow_sql` | 慢 SQL | 只读聚合 |
| `sys_oper_log` / `sys_logininfor` / `sys_job_log` | 日志与任务 | 只读聚合 |
| `sys_rum_vital` | Web Vitals 扁平样本 | **② 新建** |
| `sys_rum_issue` | 前端错误指纹聚合 | **② 新建** |
| `sys_alert_rule` / `sys_alert_event` | 规则与告警记录 | **③ 新建** |

### 5.2 上报协议（ui / h5 共用）

- 入口：既有 `POST /monitor/clientTrack/report`（需登录；H5 使用既有 OAuth token）。
- 批次字段保持：`reason`、`browserVisitId`、`sessionId`、`pageVisitId`、`operationId`、`triggerAction`、`events[]`。

**事件 envelope（所有 type）：**

| 字段 | 说明 |
|------|------|
| `eventId` | 客户端 UUID；重试复用；服务端幂等（② 起对拆表写入生效） |
| `type` | 见下 |
| `ts` | 客户端毫秒时间 |
| `pagePath` | 可选 |
| type 特有字段 | 见下 |

| type | 期 | payload 要点 |
|------|----|----------------|
| 现有 `click` / `route_*` / `api_*` / `js_error` / `promise_error` 等 | 已有 | 兼容；② 起 `js_error`/`promise_error` 应带 `stack`，并鼓励升格或并行写 `rum_error` |
| `vital` | ② | `name`: `LCP` \| `FCP` \| `CLS` \| `INP` \| `TTFB`；`value`；`rating`；`navigationType?` |
| `rum_error` | ② | `subType`、`message`、`stack`、`fingerprint`、可选 `breadcrumbs`（最近 N 条摘要） |

**服务端 ingest（②）：**

1. 原样落 `sys_client_track`（审计与全链路）。  
2. 含 `vital` → 拆行写入 `sys_rum_vital`（`event_id` UNIQUE）。  
3. 含 `rum_error` 或可指纹化的增强错误 → upsert `sys_rum_issue`，记录 `last_batch_id`。

### 5.3 新表字段（实现级草案）

**`sys_rum_vital`**

- `id`、`event_id`（UK）、`client`（`web` \| `h5`）、`name`、`value`、`rating`、`page_path`、`session_id`、`user_name`、`navigation_type`、`create_time`

**`sys_rum_issue`**

- `fingerprint`（UK）、`sub_type`、`title`、`sample_stack`、`first_seen`、`last_seen`、`hit_count`、`user_count`、`last_batch_id`、`status`（`open` \| `resolved`）

**`sys_alert_rule`**

- `rule_id`、`name`、`metric`（`error_rate` \| `slow_sql_count` \| `api_fail_rate` \| `vital_lcp_p75`）、`window_sec`、`threshold`、`compare`（`gt` \| `pct_up`）、`channels_json`、`enabled`、`cooldown_sec`（默认 900）

**`sys_alert_event`**

- `event_id`、`rule_id`、`fired_at`、`payload_json`、`status`（`firing` \| `resolved`）、`notify_result`

### 5.4 隐私与采样（② 落地，协议层先约定）

- SDK `beforeSend`：命中 `password` / `token` / `authorization` / `cookie` / `secret` 的字段替换为 `[FILTERED]`；点击文案截断。  
- 采样：`vital` 默认 30%（可环境变量配置）；错误类 100%；行为采集保持现状。  
- 卸载上报：优先 `pagehide` + `sendBeacon`，失败再 `fetch({ keepalive: true })`。

## 6. 态势总览（① 核心交付）

### 6.1 信息架构

```text
监控
├── 态势总览（新，①）
├── 前端监控（clientTrack）
├── 全链路（traceChain）
├── 慢 SQL
├── 操作日志 / 登录日志 / 在线用户 / 定时任务
├── 前端 Issue（②，可挂在前端监控下）
└── 告警规则 / 告警记录（③）
```

### 6.2 指标组（① 接真）

与 `2026-08-11-system-monitor-dashboard-design.md` §4 及 `system-monitor-dashboard-metrics.sql` 对齐：

| 组 | 来源表 |
|----|--------|
| 用户与登录 | `sys_user`、`sys_logininfor` |
| 访问与行为 | `sys_client_track` |
| 请求与错误 | `sys_oper_log` |
| 慢 SQL | `sys_slow_sql` |
| 定时任务 | `sys_job_log` |

时间窗：今日 / 昨日 / 本周 / 近 7 天 / 本月（与 demo SQL 参数约定一致）。

② 起增补：Vitals p75 卡片（`sys_rum_vital`）、开放 Issue 数（`sys_rum_issue`）；卡片带时间窗跳转到子页。

### 6.3 API 与前端

- `GET /monitor/overview/summary`：各组 KPI。  
- `GET /monitor/overview/trends`：请求 vs 错误等趋势序列。  
- 权限字建议：`monitor:overview:query`。  
- 前端：新页面接入监控菜单；视觉可延续方案 A（青蓝科技三栏）或收敛为管理端浅色 KPI 卡 + 下钻（实现计划阶段二选一，默认：**管理端浅色 KPI 布局，保留与 demo 相同的指标分组**，避免强依赖投屏深色壳）。

跨域只读：通过 `system.api` Facade 查询 operlog / logininfor / user / job；clientTrack / slowSql 本模块直查。

## 7. 告警闭环（③）

### 7.1 预置规则

| 规则 | 条件 | 优先级 |
|------|------|--------|
| 操作错误率突增 | 5min 内 oper_log 失败率较前一窗上升 >50%，或绝对值 >5% | P0 |
| 慢 SQL 暴增 | 5min 内新增慢 SQL > 前 1h 均值的 3 倍 | P0 |
| 前端 API 失败 | 5min 内含 `api_error` 的批次占比 >10%（样本不足则跳过） | P1 |
| LCP 劣化 | 近 15min LCP p75 >4000ms 且样本 ≥20 | P1 |

### 7.2 通知与防抖

- 通道：通用 Webhook + 企微 / 钉钉机器人 URL（存 `channels_json`）。  
- 同一 `rule_id` 默认冷却 15min，不重复开火；可记 `resolved`。  
- 调度：`@Scheduled` 或 quartz 任务，窗口聚合 → 写 `sys_alert_event` → 通知。  

不做：电话、On-call 排班、自动修码。

## 8. SDK 改造（② 为主）

在 `quick-ui/src/monitor` 上增量：

| 模块 | 改动 |
|------|------|
| `collectors/vitalCollector`（新） | 使用 `web-vitals`；LCP/INP/CLS 封板上报；FCP/TTFB 即时 |
| `collectors/errorCollector` | 补 `stack`；生成 `fingerprint`；urgent flush |
| `beforeSend` | 默认脱敏 + 可配置钩子 |
| `report.js` | `pagehide` + `sendBeacon` 优先 |
| 采样 | `vital` 可配 `sampleRate` |
| `$track` | 保持；自动补 `eventId` |

**quick-h5（③）**：同一 report URL 与 envelope；首期只接路由 PV 类 + 可采的 vital + 全局错误；不做完整点击坐标全埋点。

## 9. 成功标准

| 期 | 可验证标准 |
|----|------------|
| ① | 态势总览在正式菜单可打开；切换时间窗后 KPI/趋势与约定 SQL 口径一致；可从卡片下钻到现有子页 |
| ② | 真实访问产生 `sys_rum_vital` 样本；故意抛错可在 Issue 列表按 fingerprint 聚合且含 stack；敏感字段经 beforeSend 脱敏 |
| ③ | 人为抬高失败率时，冷却规则下收到至少一条 Webhook/企微通知；H5 至少能成功上报一类约定事件 |

## 10. 风险与缓解

| 风险 | 缓解 |
|------|------|
| `events_json` 全表扫描拖垮总览 | ① 总览只聚合批次级字段与既有日志表；Vitals/Issue 走旁路表 |
| 埋点含 PII / 密钥 | ② beforeSend 默认脱敏；管理页自身仍在 excludePages |
| Beacon 64KB / 丢失 | 关键错误优先；超限拆批或回退 keepalive |
| H5 与 Web API 差异 | ③ 协议兼容、能力降级；不能采的 vital 直接不上报 |
| 告警疲劳 | 相对基线 + 冷却；预置规则默认偏保守 |

## 11. ① 期 OpenSpec 预告（定稿后开 change）

建议 change 名：`monitor-overview`。

范围草案：

1. `module-monitor` overview API（summary + trends）+ 必要的 `system.api` 只读 Facade。  
2. Flyway/菜单/权限：`monitor:overview:query`。  
3. `quick-ui` 态势总览页 + 路由菜单。  
4. 指标口径单测或对照 `system-monitor-dashboard-metrics.sql` 的验收说明。  

不含：RUM 表、告警、H5、SDK vital。

## 12. Backlog（明确后置）

- Sourcemap 上传与堆栈还原服务  
- 长任务分级、资源 timing、白屏、FSP  
- 曝光埋点、UTM 渠道  
- IndexedDB 离线队列  
- `track` / `time` / `log` 业务指标产品化看板  
- AI 辅助诊断（非自动合码）
