# 精简版前端性能监控（Lite RUM）设计

日期：2026-08-14  
状态：待用户审阅  
来源：腾讯云前端性能监控（Aegis/RUM）能力对照 + 精简裁剪  
范围说明：本文描述一套**独立的精简前端 RUM**产品设计，不依赖、不复用、不对照本仓库既有慢 SQL / clientTrack / 全链路等能力。

## 1. 背景与目标

### 1.1 要解决什么

对标云厂商前端性能监控的核心价值，但只做「能用的最小闭环」：

1. **发现**：关键指标异常（错误率、API 成功率）能被看见，并能 Webhook 告警。  
2. **粗定位**：知道是哪类问题（JS 错误 vs 接口）、哪个页面、哪条 API。  
3. **可演进**：协议预留多端（H5）与多应用（`appId`），一期不摊薄实现。

### 1.2 已确认决策

| 项 | 选择 |
|----|------|
| 主目标 | 一期：发现 + 粗定位；体验深度指标（首屏/CWV 等）放二期 |
| 覆盖端 | 同一上报协议；一期只实现 Web SDK，H5 后补 |
| 控制台页面 | 数据总览 + 异常分析 + API 监控（+ 告警规则） |
| 告警 | 最小告警：错误率 / API 成功率 → Webhook；冷却防抖 |
| 维度下钻 | 不做地区/ISP/浏览器/机型；仅时间趋势 + 页面 + 接口路径 |
| 应用模型 | 一期单应用配置；表与协议预留 `appId`，接入管理 UI 后置 |
| 存储架构 | **明细 + 分钟级汇总**（方案 2） |

### 1.3 非目标（一期）

- 页面性能深度页（瀑布图、DNS/TCP/SSL、CWV 专项）
- 静态资源监控页
- 地区 / ISP / 品牌机型 / Ext 自定义维度视图
- 多应用接入管理控制台
- H5 / 小程序 SDK 实现
- 自定义日志、自定义测速、离线日志
- Sourcemap 还原、Session Replay
- 完整告警平台（值班表、升级、静默日历）

## 2. 架构

```text
[Web SDK] --批量上报--> [Ingest API] --> rum_event（明细）
                              |              |
                              |         聚合任务(1m)
                              |              v
                              |         rum_metric_1m（汇总）
                              |              |
                         error upsert   告警扫描(1～5m)
                              v              v
                         rum_issue      rum_alert_* + Webhook

[控制台]
  总览     <-- 读 rum_metric_1m
  异常     <-- 读 rum_issue / rum_event
  API      <-- 读 rum_metric_1m（必要时短窗明细）
  告警规则 <-- 读写下 rum_alert_rule / rum_alert_event
```

| 模块 | 职责 |
|------|------|
| Web SDK | 尽早初始化；采集 PV、JS/Promise 错误、API 耗时与成败；批量上报与失败有限重试 |
| Ingest | 校验 `appId`、限流、落库；不承载复杂业务逻辑 |
| 明细存储 | 错误保留可排障字段；API/PV 可按需采样（默认全量，量大再开） |
| 汇总任务 | 按 `appId + 时间桶 + 维度键` 滚动关键指标 |
| 控制台 | 总览 / 异常 / API / 告警规则 |
| 告警 | 基于汇总窗口阈值判断，Webhook 通知 |

## 3. 上报协议

### 3.1 请求

```http
POST /rum/ingest
Content-Type: application/json
```

```json
{
  "appId": "web-admin",
  "sdkVersion": "0.1.0",
  "clientTime": 1723622400000,
  "events": []
}
```

### 3.2 事件公共字段

| 字段 | 说明 |
|------|------|
| `type` | `pv` \| `error` \| `api` |
| `page` | 路由或 path（SPA 取 route） |
| `ts` | 客户端时间戳（ms） |
| `sessionId` | 可选，同一次访问关联 |
| `release` | 可选，前端版本号 |

### 3.3 分类型字段

**error**

| 字段 | 说明 |
|------|------|
| `name` | 错误名 |
| `message` | 错误信息 |
| `stack` | 堆栈原文 |
| `fingerprint` | SDK 按 `name + message + stack 首帧` 生成；服务端可再规范化 |

**api**

| 字段 | 说明 |
|------|------|
| `method` | HTTP 方法 |
| `url` | 规范化 path；去掉 query 中的敏感参数 |
| `status` | HTTP 状态码 |
| `ok` | 是否成功（一期默认 `status` 在 200–399 为成功） |
| `durationMs` | 耗时 |
| `retCode` | 可选预留，一期可不采集 |

**pv**

无额外必填字段；进入页面/路由记一次。

### 3.4 SDK 默认开关（一期）

- 开启：PV、错误、API 测速  
- 关闭：静态资源测速、自定义事件、自定义日志、CWV

## 4. 数据模型

### 4.1 `rum_event`（明细）

- 用途：排障下钻、聚合输入  
- 建议 TTL：7～14 天（默认 14 天）  
- 建议索引：`(app_id, type, ts)`、`(app_id, fingerprint, ts)`、`(app_id, url, ts)`  
- 可选优化：同 `fingerprint` 短窗口合并计数（非一期必须）

### 4.2 `rum_metric_1m`（分钟汇总）

按 `app_id + bucket_ts + dim_type + dim_key` 聚合。

维度示例：

| 用途 | dim_type | dim_key |
|------|----------|---------|
| 全局 | `all` | `_all` |
| 按页 | `page` | `/users` |
| 按接口 | `api` | `GET /api/user` |

建议度量字段：

- `pv_count`
- `error_count`
- `api_count`
- `api_ok_count`
- `api_duration_sum`
- `api_duration_count`（平均耗时 = sum / count）

### 4.3 `rum_issue`（错误指纹汇总）

| 概念字段 | 说明 |
|----------|------|
| `app_id` + `fingerprint` | 唯一键 |
| `message` / `stack` 示例 | 展示用快照 |
| `count` | 累计次数 |
| `page_count` 或页面集合摘要 | 影响面 |
| `first_seen_at` / `last_seen_at` | 首次 / 最近 |
| `status` | 一期固定 `open` 即可 |

异常列表主读此表。

### 4.4 `rum_alert_rule` / `rum_alert_event`

**规则**：指标（错误率 / API 成功率）、比较符、阈值、窗口分钟、Webhook URL、冷却分钟、启用开关、`app_id`。

**事件**：触发时间、当前值、规则快照、通知是否成功。

一期可用环境变量提供默认 `appId` 与默认规则；库表仍带 `app_id`。

### 4.5 健康分（一期简化）

| 指标 | 权重 | 计分 |
|------|------|------|
| 页面报错率（error / pv） | 50% | ≤0.5% → 100；≥10% → 0；中间线性（对齐腾讯简化口径） |
| 接口成功率 | 50% | 同上映射（成功率越高分越高） |

色块建议：

| 指标 | 绿 | 橙 | 红 |
|------|----|----|-----|
| JS 错误率 | ≤0.5% | 0.5%～10% | ≥10% |
| API 成功率 | >99.5% | 90%～99.5% | <90% |

二期再纳入首屏/资源等权重。

### 4.6 数据流

1. Ingest 写入 `rum_event`；`type=error` 时 upsert `rum_issue`。  
2. 每分钟任务处理上一分钟 bucket → upsert `rum_metric_1m`（含 `_all`）。  
3. 告警默认每 5 分钟扫描汇总窗口；触发则写 `rum_alert_event` 并调用 Webhook。  
4. 总览只读汇总；异常列表读 Issue；API TOP 读汇总（细节趋势可读短窗明细）。

## 5. 控制台

公共：时间范围（近 1h / 24h / 7d / 自定义）；`appId` 一期只读展示或环境默认。

### 5.1 菜单

```text
前端监控
  ├─ 数据总览
  ├─ 异常分析
  ├─ API 监控
  └─ 告警规则
```

### 5.2 数据总览

1. 健康分卡片（总分 + 分项）  
2. 关键指标：PV、JS 错误数/率、API 成功率、API 平均耗时  
3. 趋势图：错误率、API 成功率、API 平均耗时、PV（图例可开关）  
4. 粗定位入口：错误 TOP 页面、慢 API TOP、失败 API TOP → 跳转异常/API 并带筛选  

相对上期涨跌为可选，一期可不做。

### 5.3 异常分析

- 列表：指纹、摘要、次数、影响页面、首次/最近时间、状态  
- 详情：示例 message/stack、发生趋势、关联页面 TOP、最近 N 条原始事件  

不做：指派、Sourcemap、Session Replay。

### 5.4 API 监控

- 汇总趋势：请求量、成功率、平均耗时  
- TOP Tab：① 按平均耗时 ② 按失败次数  
- 列：method+path、次数、成功率、平均耗时（P95 二期可选）  
- 选中某一 API 后展示该接口小趋势（建议一期包含）  

不做：地区/ISP/浏览器饼图、独立 40x/50x 大视图、retcode 专项（协议已预留 `retCode`）。

### 5.5 告警规则

表单：启用、指标、阈值、窗口分钟、Webhook URL、冷却分钟。  
列表：最近触发记录。

## 6. Web SDK 行为

| 能力 | 行为 |
|------|------|
| 初始化 | 必填 `appId`、`ingestUrl`；可选 `release`、`uin`；尽早执行 |
| PV | 首屏一次 + SPA history/hash 变化各一次 |
| 错误 | `window.onerror`、`unhandledrejection`；生成 fingerprint |
| API | 同时包装 `fetch` 与 `XHR`；记录 method、规范化 url、status、duration、ok |
| 上报 | 内存队列；默认每 5s 或满 20 条批量；`sendBeacon` 优先在卸载时 flush |
| 自保护 | 忽略对 ingest 自身的请求；同 fingerprint 短窗限流 |
| 失败 | 有限次重试后丢弃，避免拖垮业务页 |

H5：复用同一信封与 `type`；一期只保证协议兼容，不交付 SDK。

## 7. 可靠性与约束

- Ingest：body 大小限制、QPS 限流、`appId` 白名单；监控场景不强制事件级幂等（可接受少量重复）。  
- 写库失败返回 5xx，由 SDK 有限重试。  
- 聚合按分钟水位推进；同一 bucket 可重跑（upsert）。  
- 明细按 TTL 清理。  
- 告警冷却期内同规则不重复通知；Webhook 失败记日志，不阻塞聚合。

## 8. 分期

| 期 | 内容 |
|----|------|
| ① | Web SDK（pv/error/api）+ Ingest + 明细/汇总/Issue + 总览/异常/API + 最小告警 |
| ② | 页面性能（FMP/LCP 等）与健康分扩展；可选 P95；告警指标扩展 |
| ③ | H5 SDK；多应用接入管理 UI；静态资源监控（按需） |

**Backlog：** 地区/ISP、Sourcemap、Session Replay、自定义测速/事件、完整值班升级。

## 9. 验收与测试（① 期）

### 9.1 验收

1. 测试页接入 SDK 后，PV、故意 JS 错误、失败 API 在 1～2 分钟内出现在控制台。  
2. 总览健康分与色块随错误率/成功率变化。  
3. 异常页出现对应 Issue，可查看 stack。  
4. API 页 TOP 能看到该失败或慢接口。  
5. 将错误率阈值调到极易触发时，Webhook 收到通知，冷却期内不刷屏。  
6. 错误 `appId` 或关闭 SDK 时，Ingest 拒绝/无数据，且不影响业务站。

### 9.2 测试要点

- SDK：fingerprint 稳定性、url 脱敏、不上报 ingest 自身  
- Ingest：非法 payload、超限拒绝  
- 聚合：fixture 明细 → 汇总行正确  
- 告警：阈值边界与冷却

## 10. 成功标准（产品）

一期交付后应能回答：

- 「现在整体健康吗？」（总分 + 色块）  
- 「是页面挂了还是接口挂了？」（错误率 vs API 成功率）  
- 「哪个页面 / 哪个接口最可疑？」（TOP + Issue/API 详情）  
- 「能否在人盯盘之前被叫起来？」（Webhook）
