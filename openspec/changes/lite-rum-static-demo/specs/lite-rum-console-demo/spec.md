## Purpose

Defines the browser-openable static console prototype for lite frontend RUM phase-1 pages, so stakeholders can validate information architecture and mock interactions before backend work.

## ADDED Requirements

### Requirement: Single-file console shell with four sections
The prototype SHALL present a single HTML page with navigation among 数据总览, 异常分析, API 监控, and 告警规则, and SHALL display a read-only `appId` plus a time-range control (至少含近 1h / 24h / 7d).

#### Scenario: Switch sections
- **WHEN** the user selects a section in the navigation
- **THEN** the corresponding section content is shown without leaving the page

#### Scenario: Shared header context
- **WHEN** any section is visible
- **THEN** the page shows the current `appId` and the selected time range

### Requirement: Overview shows health score, KPIs, trend, and TOP lists
The 数据总览 section SHALL show a health score (总分 + 报错率分项 + 接口成功率分项), KPI tiles for PV / JS 错误数与错误率 / API 成功率 / API 平均耗时, a multi-series trend chart (or equivalent mock visualization), and TOP lists for 错误页面、慢 API、失败 API.

#### Scenario: Color semantics for rates
- **WHEN** overview KPIs are rendered from mock data
- **THEN** JS 错误率 and API 成功率 use green / orange / red semantics consistent with the approved design thresholds

#### Scenario: TOP entry jumps with filter hint
- **WHEN** the user clicks a TOP 错误页面 or TOP API row
- **THEN** the prototype navigates to 异常分析 or API 监控 and surfaces the related filter or selection in the UI

### Requirement: Exception analysis list and detail
The 异常分析 section SHALL list issues with fingerprint, summary, count, affected pages, first/last seen, and status, and SHALL allow opening a detail view that shows sample message/stack, a occurrence trend (mock), related pages, and recent raw events.

#### Scenario: Open issue detail
- **WHEN** the user opens an issue from the list
- **THEN** a drawer or detail panel shows stack sample and recent events from mock data

### Requirement: API monitoring trends and TOP tabs
The API 监控 section SHALL show request volume / success rate / average duration trends, TOP tabs for average duration and failure count, and when an API row is selected SHALL show a small per-API trend.

#### Scenario: Switch TOP sort mode
- **WHEN** the user switches between 按平均耗时 and 按失败次数 tabs
- **THEN** the TOP table ordering changes accordingly using mock data

#### Scenario: Select API for detail trend
- **WHEN** the user selects an API row
- **THEN** a per-API trend panel updates for that API

### Requirement: Alert rules form and trigger history
The 告警规则 section SHALL allow editing mock rule fields (启用、指标、阈值、窗口分钟、Webhook URL、冷却分钟) and SHALL list recent trigger records. Saving or testing SHALL only show local UI feedback and MUST NOT send real network webhooks.

#### Scenario: Save rule locally
- **WHEN** the user saves an alert rule in the prototype
- **THEN** the UI confirms success with a message and updates the on-page rule display without calling an external webhook

### Requirement: Offline-friendly static delivery
The prototype SHALL be a single HTML file under `docs/demo/` that loads Vue 3 and Element Plus from CDN, uses embedded mock data only, and SHALL NOT require a Node.js build step to open.

#### Scenario: Open without build
- **WHEN** a user opens the HTML file in a browser with network access to the CDN
- **THEN** the console UI renders and interactive controls respond using mock data
