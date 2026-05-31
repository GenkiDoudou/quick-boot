# 前端用户行为监控（埋点 SDK）

QuickBoot 在 `quick-ui/src/monitor` 内置一套**全自动**前端行为采集 SDK，用于还原用户在管理后台内的操作链路：页面访问 → 按钮点击 → API 调用 → 与后台操作日志关联。

业务页面**无需手动埋点**；在 `main.js` 启用插件后，全局 click、路由、axios、JS 错误会自动采集并批次上报至 `/monitor/clientTrack/report`。

---

## 与管理端页面的关系

| 层级 | 路径 | 职责 |
|------|------|------|
| **采集 SDK** | `quick-ui/src/monitor/` | 运行时自动采集、缓冲、上报 |
| **管理端展示** | `quick-ui/src/views/monitor/clientTrack/` | 批次列表、事件链、行为轨迹图 |
| **后端存储** | `SysClientTrackController` | 接收上报、查询、时间线聚合 |

采集与管理端相互独立：查看监控数据时，监控页本身在 `excludePages` 黑名单内，避免「看日志又产生日志」的噪声。

---

## 架构概览

```text
用户操作（click / 路由 / API / 错误）
        ↓
  collectors/（采集器）
        ↓
  core/eventBuffer（内存缓冲）
        ↓
  batchSession（批次：pageVisitId / operationId）
        ↓
  core/flushPipeline + report（组装 payload、POST 上报）
        ↓
  后端 sys_client_track → 行为轨迹 / 事件链页面
```

### 模块目录

```
src/monitor/
├── index.js                 # 入口：setupUserMonitor()
├── config.js                # 环境变量与开关
├── constants.js             # inject key、click 选择器
├── plugin/
│   └── userMonitorPlugin.js # Vue 插件工厂
├── core/
│   ├── pathGuard.js         # 路由白/黑名单
│   ├── monitorContext.js    # sessionId / browserVisitId 缓存
│   ├── eventBuffer.js       # 事件缓冲
│   └── flushPipeline.js     # 批次 flush 与上报
├── collectors/
│   ├── clickCollector.js    # 全局 click
│   ├── errorCollector.js    # JS / Promise 错误
│   ├── routeCollector.js    # 路由进入/离开
│   └── lifecycleCollector.js# beforeunload、定时 flush
├── batchSession.js          # 隐式操作批次（核心状态机）
├── requestObservation.js    # axios 请求观测（与 request.js 配合）
├── requestTrace.js          # x-trace-id / traceparent 头
├── sessionContext.js        # 登录会话 ID（localStorage）
├── browserVisitContext.js   # 浏览器访问 ID + 心跳
├── clickTarget.js           # 从 DOM 解析按钮文案
├── operationRules.js        # 主/被动/查询类操作判定
├── report.js                # fetch 上报（独立于 axios，防递归）
├── display/                 # 展示层工具（管理端格式化标签）
└── composables/
    └── useUserMonitor.js    # 组件内可选手动 track
```

### 测试目录

单元测试统一放在 `quick-ui/src/test/monitor/`，与源码分离：

```
src/test/monitor/
├── batchSession.test.js
├── createUserMonitor.test.js
├── display.test.js
└── clientTrack/             # 行为轨迹页模型/图表单测
    ├── buildTimelineModel.test.js
    └── ...
```

运行：`cd quick-ui && pnpm exec vitest run src/test/monitor`

---

## 核心概念（链路 ID）

| 字段 | 含义 | 生命周期 |
|------|------|----------|
| `browserVisitId` | 一次打开浏览器 Tab 的访问 | Tab 内持久（localStorage + 心跳） |
| `sessionId` | 一次登录会话 | 登录创建、登出清除 |
| `pageVisitId` | 一次页面访问（路由进入） | 同页多操作批次共用 |
| `operationId` | 一次用户操作批次（如点「修改」后的 click + API） | openBatch 创建，idle/离开页 flush 后结束 |
| `clientTraceId` | 单次 API 请求追踪 ID | 请求头 `x-trace-id` |
| `serverTraceId` | 后端 trace | 响应 `R.traceId`，可与操作日志关联 |

一次典型链路：

1. 路由进入 `/system/user` → 创建 `pageVisitId`，记录 `route_enter`
2. 点击「修改」→ 创建 `operationId`（action 批次），记录 `click`
3. 弹出框点「确定」→ 同一批次内记录 API → idle 超时或关页时 flush 上报

---

## 采集内容

| 事件 type | 来源 | 说明 |
|-----------|------|------|
| `route_enter` | routeCollector | 进入可采集页面 |
| `click` | clickCollector | 按钮/链接（含坐标） |
| `api_call` / `api_error` / `api_slow` | requestObservation | axios 请求 |
| `js_error` / `promise_error` | errorCollector | 全局错误 |

### 操作批次规则（batchSession）

- **页面访问批**（`page_visit`）：路由 `afterEach` 时 `openPageVisit`
- **操作批**（`action`）：主操作按钮（新增/修改/删除等）触发 `openBatch`
- **被动操作**（重置、翻页等）：`touchBatchPassive`，不替换主 trigger
- **idle flush**：最后一次事件后 `VITE_APP_MONITOR_IDLE_MS` 无新事件则上报
- **弹窗阻塞**：检测到 Element Plus overlay 打开时推迟 idle flush，避免弹窗未关就上报

### 路由黑名单

默认不采集以下路径（可在 `.env` 覆盖）：

- `/system/clientTrack`、`/system/clientTrackEvents`、`/system/clientTrackTimeline`
- `/monitor/clientTrack`
- `/redirect`

---

## 配置（环境变量）

在 `quick-ui/.env.development` / `.env.production` 中配置：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `VITE_APP_MONITOR_ENABLED` | `true` | 总开关；`false` 关闭采集与 axios 观测 |
| `VITE_APP_MONITOR_MAX_KEEP` | `40` | 内存缓冲最大事件数 |
| `VITE_APP_MONITOR_INTERVAL` | `10000` | 定时 flush 间隔（ms，仅 action 批） |
| `VITE_APP_MONITOR_IDLE_MS` | `2000` | 操作批 idle 上报间隔（ms） |
| `VITE_APP_MONITOR_SLOW_API_MS` | `3000` | 慢 API 阈值 |
| `VITE_APP_MONITOR_ALLOW_PAGES` | （空） | 非空时仅采集匹配前缀 |
| `VITE_APP_MONITOR_EXCLUDE_PAGES` | 见上 | 逗号分隔排除前缀 |

修改环境变量后需**重启** `pnpm dev` 才生效。

---

## 接入方式

### 1. 全局启用（已默认接入）

```javascript
// main.js
import { setupUserMonitor } from '@/monitor'

const userMonitor = setupUserMonitor()
if (userMonitor) {
  app.use(userMonitor, { router })
}
```

`setupUserMonitor()` 会：

1. 读取 `config.js` 配置
2. 创建 Vue 插件并注册 click/路由/错误采集
3. 绑定 `request.js` 的 API 观测

### 2. 组件内手动补充事件（可选）

```javascript
import { useUserMonitor } from '@/monitor'

const { track, flush, enabled } = useUserMonitor()

function onExport() {
  if (!enabled) return
  track({ type: 'custom', label: '导出报表' })
}
```

### 3. 管理端展示格式化

管理端页面请从 `@/monitor/display` 导入标签工具，勿与采集层混用：

```javascript
import { formatTrackLabel, resolveBatchTriggerAction } from '@/monitor/display'
```

---

## 上报协议

`POST {VITE_APP_BASE_API}/monitor/clientTrack/report`

请求体（简化）：

```json
{
  "reason": "idle",
  "browserVisitId": "...",
  "sessionId": "...",
  "pageVisitId": "...",
  "operationId": "...",
  "triggerAction": "修改",
  "triggerLabel": "修改",
  "events": [
    { "type": "click", "target": "修改", "ts": 1710000000000, "page": "/system/user" },
    { "type": "api_call", "url": "/system/user/1", "method": "get", "clientTraceId": "..." }
  ]
}
```

上报使用独立 `fetch` + Client HMAC 签名，**不走 axios 拦截器**，避免递归采集和 Toast 干扰。

---

## 管理端功能页面

| 路由 | 页面 | 功能 |
|------|------|------|
| `/system/clientTrack` | `clientTrack/index.vue` | 批次列表 |
| `/system/clientTrackEvents` | `clientTrack/events.vue` | 事件链 |
| `/system/clientTrackTimeline` | `clientTrack/timeline.vue` | 行为轨迹（跳转图 + 单页行为树） |

时间线页中 `serverTraceId` 可点击跳转操作日志详情（与 `operlog` 联动）。

---

## 已移除的旧 API

以下**手动批次 API 已删除**，请勿在业务代码中使用：

- `beginOperation` / `endOperation` / `ensureOperation`
- `runInOperation` / `cancelOperation`
- `registerOperationEndHook` / `registerOperationBeginHook`
- `setPendingTrigger` / `suppressEndOperation` / `resumeEndOperation`

监控已由全局 click + `batchSession` 自动关联批次。若需底层能力，可导入 `openBatch`、`flushBatchSync` 等（仅 SDK 内部或高级场景）。

---

## 调试建议

1. 开发环境安装成功后控制台输出：`[monitor] user behavior monitor installed`
2. Network 筛选 `clientTrack/report` 查看上报 payload
3. 确认当前路由不在 `excludePages`
4. 开发时可增大 `VITE_APP_MONITOR_IDLE_MS` 降低上报频率

---

## 相关文档

- [业务页面总览](./index)
- [后端监控审计模块](../../backend/modules/monitor-audit)
- [统一追踪设计](../../../superpowers/specs/2026-05-30-unified-tracing-design.md)
