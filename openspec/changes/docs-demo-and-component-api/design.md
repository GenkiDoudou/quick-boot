## Context

VitePress 已有实用向指南与三端概述页（OpenSpec `docs-sites-and-deploy`）。权威扩展设计见 `docs/superpowers/specs/2026-08-25-docs-demo-and-component-api-design.md`。需补对外演示入口，以及 common / C7 / Qb 的手册级 API 文档。

约束：只改 `docs/`；内容以源码为准；不编入 `superpowers/`。

## Goals / Non-Goals

**Goals:**

- 独立演示页 + 顶栏入口，三链接为 `qc.126w.com` 约定地址。
- 三端 components 分区全量落盘（索引 + 每包/组件一页，含 API 表）。

**Non-Goals:**

- 自动生成流水线、站内 playground、改业务代码。

## Decisions

1. **演示独立成页而非仅首页按钮**  
   - 理由：用户已选 C；便于分享与指南互链。  
   - 备选：仅 hero（入口弱）。

2. **手册级详表，按源码包/组件拆文件**  
   - 理由：用户已选 API 手册深度；侧栏可导航。  
   - 备选：一览表（过浅）；自动生成（基建重）。

3. **命名 kebab + common 用包名**  
   - 理由：与设计一致，URL 稳定。

4. **缺失 API 标「见源码/透传」，禁止臆造**  
   - 理由：正确性优先于「看起来完整」。

## Risks / Trade-offs

- [页面数量多、props 表长] → 按目录批量提炼；表格不擅自删项。  
- [common 包交叉] → 按 Java 包目录拆页并互链。  
- [与实用向文档并存导致侧栏变长] → 组件组默认 collapsed 可选；索引页承担总览。

## Migration Plan

1. 合并文档变更后 `pnpm build` / 既有 docs Job 发布即可。  
2. 无数据迁移；回滚为还原文档提交。

## Open Questions

- 无阻塞项。演示域名以设计稿 `qc.126w.com` 为准。
