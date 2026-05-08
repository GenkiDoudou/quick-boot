## Context

- **quick-ui**：Vue 3 + Element Plus；业务页在多视图切换时若各自维护返回与标题，行为与过渡易分裂。
- **已定稿设计**：**`docs/superpowers/specs/2026-05-08-c7-view-switch-design.md`**；原始说明：**`原始需求/前端/C7视图切换容器.md`**。
- **proposal**：新增 **`C7ViewSwitch`** 单文件组件，注册于 **`packages/index.js`**，并补充 VitePress 文档。

## Goals / Non-Goals

**Goals**

- **单文件**：**`quick-ui/src/packages/C7ViewSwitch/index.vue`** 内完成栈、`v-model` 同步、插槽分发、可选 **`ElPageHeader`**、**`<Transition>`** 三态（**`false` / `true` / `string`**），与现有 **`C7Dialog`** 等 **JSDoc 深度** 对齐。
- **有效视图列表**：**`computed`** 归一 **`views`** 与 **`showIndexs`**（**`views` 优先**）；**`findConfig(name)`** 辅助匹配。
- **历史栈**：**`ref<string[]>`**；**仅 `switchTo` / `goBack`** 修改；**`viewHistory`** 暴露为 **新数组副本** 或 **`readonly`**，避免外部原地修改。
- **父级 `v-model`**：**`watch(modelValue)`** 仅更新展示，**不** push/pop；**首次挂载** **`isReady`** 或等价标志，抑制首次 **`change`**。
- **`ElPageHeader`**：以 **`quick-ui/package.json` 锁定的 element-plus** 为准核对 **`title` / `content` / `@back`** 等插槽与事件名；**`@back`** 调用与 **`goBack()`** 相同分支。
- **无匹配视图**：渲染 **`#empty`**；无插槽则 **空占位**（零高度或透明块由实现选定，JSDoc 说明）。

**Non-Goals**

- 不与 **`vue-router`** 自动同步；不实现浏览器式「前进」队列。

## Decisions

1. **默认过渡 `name`**  
   - 常量 **`c7-view-switch`**（与 superpowers 设计稿一致），**`transition === true`** 时使用。

2. **无 `title` 时的页头标题**  
   - 使用 **空字符串** 作为 **`ElPageHeader` `title`**（避免魔法占位文案）；JSDoc 说明业务应通过 **`#header-content`** 或 **`views[].title`** 补齐。

3. **`change` / `back` 的 `config` 引用**  
   - **`emit`** 的 **`config`** 为 **`views` 数组中匹配项的对象引用**（与当前 `views` prop 同源）；调用方 **不应** 依赖其可变性，若后续改为浅拷贝再在 tasks 中评估。

4. **`closeIndex` / `defaultView` 非法**  
   - 解析后若 **不在**有效 **`name`** 集合内，视为 **无回落**，走 **`back-empty`**（不 `console.error`，除非开发环境可选 warn，**默认不**）。

5. **`switchTo` 与栈顶**  
   - 成功切换前将 **当前** `modelValue` **压栈**；若目标与当前相同则 **no-op**。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **父级强行改 `v-model` 导致栈与 UI 路径不一致** | 设计已约定；文档与 VitePress 示例说明；可选 **`clearHistory`** 留待评审 |
| **EP `ElPageHeader` API 版本差异** | 实现前对照锁定版本文档；注释标明依赖 API |
| **具名插槽与动态 `name` 的 TS/校验** | JSDoc 列出合法 `name` 集合由业务保证；运行时仅 **字符串** 匹配 |

## Migration Plan

- **新能力**：业务新页直接引用 **`C7ViewSwitch`**；**无数据迁移**。
- **回滚**：移除注册与组件文件即可。

## Open Questions

- （无）**`clearHistory`** 是否在首版 **`defineExpose`** 顺带暴露，由实现阶段代码评审决定（设计稿为可选）。
