## Why

精简版前端 RUM 设计（`docs/superpowers/specs/2026-08-14-lite-frontend-rum-design.md`）已定稿，需要一份可双击打开的静态控制台原型，用来对齐一期信息架构与交互，再进入真实前后端实现。

## What Changes

- 新增 CDN 版 Vue 3 + Element Plus 单文件静态原型，内嵌 mock 数据。
- 在同一页内可切换：**数据总览**、**异常分析**、**API 监控**、**告警规则**。
- 原型覆盖健康分/色块、关键指标、趋势图、TOP 列表、Issue 详情抽屉、API TOP + 选中趋势、告警规则表单与触发记录。
- 不实现真实 Ingest、SDK、库表、Webhook 调用（仅 UI 演示与文案提示）。

## Capabilities

### New Capabilities

- `lite-rum-console-demo`: 精简前端监控控制台静态原型的页面结构、可交互元素与 mock 展示口径。

### Modified Capabilities

- （无）

## Impact

- 新增文件：`docs/demo/lite-frontend-rum-console.html`
- 新增 OpenSpec change：`openspec/changes/lite-rum-static-demo/`
- 不影响现网 quick-ui / 后端模块；无 API / 库表变更
