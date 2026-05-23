# 设计概述

QuickBoot 采用**单体分层**架构：浏览器 → Nginx（可选）→ `quick-ui` 静态资源 + `quickboot-web` API，公共能力下沉 `quickboot-common`。

## 文档地图

| 主题 | 文档 |
|------|------|
| 架构 | [系统架构](./architecture) |
| 数据 | [数据库设计](./database) |
| 认证 | [认证授权](./auth) |
| 安全 | [安全防护](./security) |
| 功能模块 | [后端模块总览](../backend/modules/index) |
| 前端组件 | [C7 组件](../frontend/index) |

## 设计稿归档

详细交互与组件契约见 `docs/superpowers/specs/`（如 C7JsonTable、OAuth2、各业务模块设计）。

## 变更流程

功能变更通过 `openspec/changes/<id>/` 提案 → 设计 → 任务 → 实现，避免实现与规范脱节。
