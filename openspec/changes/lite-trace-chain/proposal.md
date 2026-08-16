## Why

研发排障需要把前端 RUM、后端接口、SQL 与异常用同一 `traceId` 串成可检索调用链；现有能力缺少统一投影存储与开发向控制台（A′ 瀑布 + E 查询台）。设计已定稿于 `docs/superpowers/specs/2026-08-14-lite-frontend-rum-design.md`，本期落地①期实现。

## What Changes

- 新增 Web RUM SDK：采集 pv/action/api/error，注入 `traceId`，上报 `env.ua` 等；Ingest 补 `clientIp`。
- 新增链路存储：`trace_index` + `trace_span`；各来源投影写入；排障 UI 只读这两张表。
- 后端透传/生成 `traceId`，access / SQL / 异常写入时带同一 ID 并投影为 span。
- 管理端控制台：**A′ Trace 列表+瀑布**、**E 查询控制台**（①b 可选 D′ Issue，本期 tasks 可单列可选）。
- 不做：PV/健康分运营大盘、地区/ISP 大盘、H5 SDK、完整告警平台。

## Capabilities

### New Capabilities

- `lite-rum-sdk`: Web 端采集协议、SDK 行为与 `/rum/ingest` 上报契约。
- `lite-trace-storage`: `trace_index` / `trace_span` 模型、投影规则与检索语义。
- `lite-trace-backend`: 后端 `traceId` 透传及 access/SQL/异常投影为 span。
- `lite-trace-console`: 开发向控制台 A′（Trace 瀑布）与 E（查询台）行为。

### Modified Capabilities

- （无）

## Impact

- 后端：新表 DDL、Ingest API、Filter/SQL/异常埋点、投影服务（建议落 `module-monitor` 或新建轻量链路模块，实现时按仓库分层约定）。
- 前端：`quick-ui` 接入 SDK + 监控菜单页（A′/E）；参考静态原型 `docs/demo/lite-rum-chain-a-unified.html`、`lite-rum-chain-e.html`。
- 文档：以 `docs/superpowers/specs/2026-08-14-lite-frontend-rum-design.md` 为需求源。
- 无破坏性变更预期；新增能力，不替换现有慢 SQL 等页面（可并存投影）。
