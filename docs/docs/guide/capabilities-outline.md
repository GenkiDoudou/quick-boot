# QuickBoot 文档大纲

> **状态**：侧栏链接已基本全部落盘（2026-05 补充）。维护时功能变更请同步更新对应模块页。

## 快速导航

| 分区 | 入口 |
|------|------|
| 指南 | [项目介绍](./introduction) |
| 后端 | [后端概述](../backend/index) |
| 前端 | [前端概述](../frontend/index) |
| 部署 | [环境要求](../deploy/requirements) |
| 设计 | [设计概述](../design/index) |
| AI/SDD | [AI 工作流](../skill/) |

启动文档站：`cd docs && pnpm dev`

---

## 一、指南 `/docs/guide/`

| 文档 | 状态 |
|------|------|
| 项目介绍、大纲、快速上手、环境搭建、FAQ、贡献指南 | ✅ |

## 二、后端 `/docs/backend/`

| 分类 | 状态 |
|------|------|
| 概述、规范、结构 | ✅ |
| 功能模块（含 OAuth2） | ✅ |
| API 规范与用户/权限/系统 | ✅ |
| 工具类总览 | ✅ |
| 通用组件使用文档 | ✅ |

## 三、前端 `/docs/frontend/`

| 分类 | 状态 |
|------|------|
| 概述、规范、结构、业务总览、列表模板 | ✅ |
| 路由、Store、工具、样式、i18n | ✅ |
| C7 组件（含 JsonTable/Form、Button、Excel 等） | ✅ |

## 四、设计 `/docs/design/`

| 分类 | 状态 |
|------|------|
| 概述、架构、数据库、认证、安全、模块 | ✅ |
| 后端通用组件设计（中文目录） | ✅ |

## 五、部署 `/docs/deploy/`

| 分类 | 状态 |
|------|------|
| 环境、配置、本地部署与联调 | ✅ |
| Docker、Nginx、SSL、优化、监控 | ✅（参考模板，仓库无内置 Dockerfile） |

## 六、AI / SDD

| 分类 | 状态 |
|------|------|
| `/docs/skill/*` | ✅ |
| `/docs/sdd/*` | ✅ |

## 七、源码映射

```text
quickboot-web/  → docs/docs/backend/
quick-ui/       → docs/docs/frontend/
```
