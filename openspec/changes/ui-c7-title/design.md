## Context

- **已定稿超级能力设计**：`docs/superpowers/specs/2026-05-07-c7-title-design.md`（brainstorming 结论：1B、2A、3A、4A、5A、6A）。
- **现状**：`quick-ui/src/packages` 无 **`C7Title`**；**`C7Card`** 有独立标题头实现，本变更 **不** 抽取共享子组件、**不** 改 Card。
- **约束**：Vue 3 + Element Plus；图标来自 **`@element-plus/icons-vue`**；注释与 JSDoc 简体中文（与仓库约定一致）。

## Goals / Non-Goals

**Goals:**

- 提供 **`C7Title`**：**统一区块标题** 的字号/加粗/底部分割线/左图标/右操作区；**`labelSize`**、`tag`、颜色、**`showBorder`**、插槽行为与设计文档一致。
- 根节点暴露 **`--c7-title-decoration-color`**，便于主题与页面级覆盖。

**Non-Goals:**

- 不实现路由、权限、数据请求；不修改 **`openspec/specs/`** 主库既有 capability 文本。

## Decisions

| 决策 | 说明 | 备选未选原因 |
|------|------|----------------|
| 标题节点用 **`ElText`** | 与 **`C7Card`** 语义路径一致，`:tag` 由 **`resolvedTag`** 驱动 | 纯 **`<component :is>`** 与现有 Card 不一致、主题需更多自建 |
| **`tag` 默认 `undefined`** + **`resolvedTag` 计算** | 未传 **`tag`** 且 **`labelSize`** 为 h 级 → 语义标签与 **`labelSize`** 同级；已传 **`tag`**（含显式 **`h4`**）→ 语义标签 **不** 被 h 级 **`labelSize`** 改写，但 **`labelSize`** 仍为 h 级时 **须** 应用对应字号/行高预设；未传 **`tag`** 且 **`labelSize`** 非 h 级 → 语义标签 **`h4`** | Vue 无法区分「默认 **`h4`**」与「显式 **`h4`**」，故 **`tag`** 不能以 **`h4`** 作为 props 默认值 |
| **`labelSize` 为自定义单位** | 仅 **`font-size`/`line-height`**，**不** 改 **`tag`** | — |
| **装饰线 = `showBorder` 底线** | 同一元素，**`showBorder=false`** 不渲染底边 | 双轨（短装饰 + 全宽 border）增加概念与验收成本 |
| **`icon` 字符串** | **`ElementPlusIconsVue[name]`** 动态取组件；未知 **`console.warn`**、不渲染 | 白名单过僵；仅插槽则违背「EP 图标名」需求 |
| **主文案** | **`label` 为主**；**`title` 别名**；二者均有非空时 **仅用 `label`**，可选 dev warn | 避免双 prop 歧义 |
| **颜色** | **`decorationColor` > `labelColor` > `var(--el-color-primary)`** 写入 **`--c7-title-decoration-color`** | — |
| **字重** | 固定 **`font-weight: 600`**，无 **`bold` prop** | YAGNI |
| **根 `inheritAttrs: true`** | **`class`/`style`** 等落到根 **`div.c7-title`** | 便于页面微调布局 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **`labelSize` 非法字符串** | dev **`console.warn`**，字号回退到与当前 **`resolvedTag`** 对应的 h 预设或文档约定基线 |
| **动态图标 tree-shaking** | 使用 **`import * as Icons`** 可能增大 chunk；接受与 **`C7Button`** 等「显式 import」策略的差异，或在后续优化为按需映射（非本变更必做） |
| **`ElText` 与自定义字号** | 自定义 **`labelSize`** 通过内联 style 或 BEM 修饰类覆盖，避免破坏 EP 主题对比度责任边界（业务仍负责可读性） |

## Migration Plan

- 无存量 **`C7Title`**；新页面按需引用。无需数据迁移。

## Open Questions

- （无）实现以 **`docs/superpowers/specs/2026-05-07-c7-title-design.md`** 为准。
