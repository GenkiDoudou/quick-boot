## Why

列表页工具栏按钮较多时，移动端与窄屏需要把超出部分折叠到「更多」下拉以节省横向空间。调用方既希望 **数据驱动（`buttons[]`）**，也希望 **默认插槽内放置多个 `C7Button`** 与既有写法兼容，且折叠后与平铺时行为一致。原始说明见 `原始需求/前端/C7按钮组.md`。

已有 **`C7Button`**（变更 **`ui-c7-button`**）承担单按钮流水线；本变更在其之上提供布局容器 **`C7ButtonGroup`**，不负责重写校验/确认/`clickFunction` 语义。

## What Changes

- 在 **quick-ui** 新增 **`C7ButtonGroup`**：支持 **`mode`**：`inline`（全平铺）、`dropdown`（全部进下拉）、`auto`（最多外露 **`maxVisible`** 个，其余进「更多」）。
- **数据模式**：通过 **`buttons[]`** 生成子 **`C7Button`**，字段支持 **`hidden` / `disabled` / `icon` / `label` / `btnType` / `clickFunction`** 等与单按钮对齐的常用项；折叠项点击 MUST 执行对应 **`clickFunction`**，且 MUST 走与 **`C7Button`** 相同的整条流水线（校验 → 确认 → 异步 → 提示 → emit）。
- **插槽模式**：默认插槽内多个 **`<C7Button />`** MUST 按相同折叠规则分配；折叠项 MUST 仍通过 **`C7Button`** 实例触发命令（禁止仅用下拉菜单项直连业务函数绕过流水线）。
- **组级事件**：**`before-command(item)`**、**`after-command({ item, success })`**（载荷形状见 design/spec）；与每个 **`C7Button`** 既有 **`before-click` / `success` / `error` / `after-click`** **并存**，组事件用于容器层观测，不替代按钮事件。
- **API**：**`spacing`**、**`size`**、**`responsive`**（class 钩子）、**「更多」** 相关 **`moreText` / `moreIcon` / `moreButtonType` / `moreButtonPlain` / `trigger`**；暴露 **`forceUpdate()`** 供插槽子节点动态变化后强制重算折叠。
- **注册**：经 **`packages/index.js`** 全局注册 **`C7ButtonGroup`**（与 **`C7Button`** 一致）。

## Capabilities

### New Capabilities

- **`ui-c7-button-group`**：**`C7ButtonGroup`** 的布局模式、`buttons` 与插槽双模式等价行为、`maxVisible` 语义、组级与按钮级事件分工、`forceUpdate`、验收标准。

### Modified Capabilities

- （无）新建 capability；依赖 **`ui-c7-button`** 单按钮规格，不修改后端规格。

## Impact

- **代码**：新增 **`quick-ui/src/packages/C7ButtonGroup/index.vue`**（或等价路径）、**`packages/index.js`** 导出与注册；可选 Dev/E2E 页与路由（tasks 中决定是否与 **`C7ButtonE2E`** 合并或新建）。
- **依赖**：以现有 **Element Plus**（如 **`el-dropdown`**）与 **`C7Button`** 为准；无强制新增 npm 包（若实现选用额外依赖须在 tasks 中说明）。
- **测试**：建议 Playwright 覆盖 **`mode=auto,maxVisible=2`** 与插槽三按钮折叠场景（可与 **`ui-c7-button`** 自动化测试目录风格对齐，非阻塞 proposal）。

## Non-goals

- 不在本变更内规定全站列表页替换进度。
- **`responsive`** 不承担具体断点逻辑，仅提供扩展用 class 标记。
