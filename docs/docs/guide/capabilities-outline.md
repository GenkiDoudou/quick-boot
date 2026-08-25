# QuickBoot 文档大纲

> **状态（2026-08）**：实用向三端文档 + **在线演示** + **common / C7 / Qb 组件 API 手册**已落地。设计深链 / skill 站内页等仍暂缓。

## 快速导航

| 分区 | 入口 |
|------|------|
| 指南 | [项目介绍](./introduction)、[在线演示](./demo) |
| 后端 | [概述](/docs/backend/)、[能力包](/docs/backend/components/) |
| 管理端 | [概述](/docs/frontend/)、[C7 组件](/docs/frontend/components/) |
| 移动端 | [概述](/docs/h5/)、[Qb 组件](/docs/h5/components/) |

启动文档站：`cd docs && pnpm dev`  
构建：`cd docs && pnpm build`（产物用于 Jenkins docs Job）

---

## 一、指南 `/docs/guide/`

| 文档 | 状态 |
|------|------|
| 项目介绍、大纲、快速上手、**在线演示**、环境搭建、FAQ、贡献指南 | ✅ |

## 二、后端 `/docs/backend/`

| 文档 | 状态 |
|------|------|
| 概述、上手、结构、约定 | ✅ 实用向 |
| `components/`：quickboot-common 各包 API 手册 | ✅ |

## 三、管理端 `/docs/frontend/`

| 文档 | 状态 |
|------|------|
| 概述、上手、结构、约定 | ✅ 实用向 |
| `components/`：C7 组件 API 手册 | ✅ |

## 四、移动端 `/docs/h5/`

| 文档 | 状态 |
|------|------|
| 概述、上手、结构、约定 | ✅ 实用向 |
| `components/`：Qb 组件 API 手册 | ✅ |

## 五、部署（仓库 `deploy/`，非本站分区）

| 内容 | 状态 |
|------|------|
| Jenkins：quickboot / quick-ui / quick-h5 / docs | ✅ |
| Nginx：`/`、`/h5/`、`/docs/`、积木路径、`/prod-api/` | ✅ 示例配置 |
| 机上目录说明 | ✅ `deploy/env/README.md` |

## 六、源码映射

```text
quickboot-common/     → docs/docs/backend/components/
quick-ui/src/packages → docs/docs/frontend/components/
quick-h5/src/components/qb → docs/docs/h5/components/
```

设计与变更记录仍可在 `docs/superpowers/` 查阅（**不**编入 VitePress 构建）。
