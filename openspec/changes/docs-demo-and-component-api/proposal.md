## Why

实用向三端文档已落地，但仍缺少对外「在线演示」入口，且 `quickboot-common`、C7（`quick-ui/src/packages`）、Qb（`quick-h5/src/components`）缺少手册级 API 文档，开发者只能翻源码，不利于对外展示与组件复用。

## What Changes

- 新增指南页「在线演示」及顶栏入口，列出 `qc.126w.com` 文档 / 后台 / H5 三链接，并与介绍、上手页互链。
- 新增 `backend/components`、`frontend/components`、`h5/components` 分区：各包/组件独立手册页 + 索引，含 Props/Events/Slots 或 Java 公开 API 详表。
- 更新 nav / sidebar / capabilities-outline。
- 不改三端业务运行时代码；不做自动生成流水线与站内 playground。

## Capabilities

### New Capabilities

- `docs-online-demo`：在线演示页与导航入口（三端演示 URL）。
- `docs-component-api-handbook`：common / C7 / Qb 组件（能力）API 手册页、索引与侧栏。

### Modified Capabilities

- （无）不修改已归档主 specs 的需求级行为；本变更仅扩展文档站内容。

## Impact

- 影响：`docs/`（Markdown、nav、sidebar、大纲）。
- 不影响：业务 API、部署流水线（除文档内容外）、三端运行时。
- 内容来源：以源码定义为准，禁止臆造 API。
