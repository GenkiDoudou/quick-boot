## Why

页面区块标题在各处手写导致 **字号、加粗、底部分割线、左侧图标、右侧操作区** 不一致，也难以用 **CSS 变量** 统一控制装饰色。需在 **quick-ui** 提供 **`C7Title`**，与已定稿的 `docs/superpowers/specs/2026-05-07-c7-title-design.md` 及 `原始需求/前端/C7标题.md` 对齐。

## What Changes

- 新增 **`C7Title`** 组件：`ElText` 承载语义标签（`h1`~`h6` / `div` / `p`）、**`labelSize`**（`h1`~`h6` 预设或 `px`/`rem`/`em` 自定义字号）、**`decorationColor` / `labelColor`** 优先级、**`showBorder`** 与底部装饰线为同一视觉元素、**`icon`（PascalCase 字符串）** 与 **`#icon`** 覆盖、**`#title`** 与 **`label`/`title` prop**、**默认插槽** 为右侧 actions。
- 在 **`quick-ui/src/packages/index.js`** 中 **导出并全局注册** **`C7Title`**。
- **不**修改 **`C7Card`** 实现；与 Card 解耦，仅变量命名可对齐。

## Capabilities

### New Capabilities

- **`ui-c7-title`**：**`C7Title`** 的 props/插槽/CSS 变量、**`labelSize` 与 `tag` 同步规则**、图标动态解析与未知 warn、**`showBorder`**、颜色优先级、验收场景（见 delta spec）。

### Modified Capabilities

- （无）不修改 `openspec/specs/` 下已有 capability 的 REQUIREMENTS。

## Impact

- **代码**：新增 **`quick-ui/src/packages/C7Title/index.vue`**；修改 **`quick-ui/src/packages/index.js`**。
- **文档**：本变更下 **proposal / design / tasks** 与 **`specs/ui-c7-title/spec.md`**；已定稿超级能力设计见 **`docs/superpowers/specs/2026-05-07-c7-title-design.md`**（实现须与其一致）。
- **依赖**：现有 **`element-plus`**、**`@element-plus/icons-vue`**；不强制新增 npm 包。
