## Why

列表页与表单页大量重复使用「新增 / 修改 / 删除 / 查询 / 重置」等按钮，样式与交互（防抖、确认、loading、校验、提示）分散在各页面，维护成本高、行为不一致。原始说明见 `原始需求/前端/C7按钮.md`。

## What Changes

- 在 **quick-ui** 提供 **`C7Button`**：基于 **Element Plus `ElButton`**，叠加 **预设类型（btnType）** 与 **固定点击流水线**（校验 → 确认 → 异步执行 → 成功判定 → 提示 → 事件）。
- 与项目现有 **`axios` / `request` 封装**对齐：`clickFunction` **reject** 表示失败；**resolve** 后再由 **`checkSuccess`** 区分业务成败（见 design）。
- **防抖**：短窗口内合并点击；**整段流水线 busy** 防止重入；**loading** 仅在执行 `clickFunction` 期间展示（见 design）。
- **事件**采用 Vue **`emit`**（`before-click` / `success` / `error` / `after-click`），而非原始文档笔误式的 `successCallback` props。
- **全局注册**：在 `main.js` 注册 **`C7Button`**，业务页可直接使用。

## Capabilities

### New Capabilities

- **`ui-c7-button`**：`C7Button` 的预设类型表、props/事件契约、点击流水线顺序、防抖与 loading 语义、与 `ElForm.validate` 及确认框的交互、验收标准。

### Modified Capabilities

- （无）首个前端通用组件 capability；不修改后端规格。

## Impact

- **测试用例清单**：`C7Button组件-测试用例清单.md`（与本变更同目录；OpenSpec 归档时随 `openspec/changes/ui-c7-button` 整体迁至 `openspec/changes/archive/YYYY-MM-DD-ui-c7-button/`）。
- **自动化测试**：同目录下 `自动化测试/`（Playwright）与 `C7Button组件-自动化测试报告.md`；E2E 场景页 `quick-ui/src/views/dev/C7ButtonE2E.vue`、路由 `/dev/c7-button-e2e`。
- **代码**：`quick-ui/src/packages/C7Button/index.vue`、`quick-ui/src/packages/index.js`、`quick-ui/src/main.js`（已实现者以 tasks 校验对齐）。
- **行为**：可选地在示例页或现有业务页逐步替换手写 `el-button`（非本变更强制范围）。
- **依赖**：`element-plus`、`@element-plus/icons-vue`、`lodash`（`debounce`）；无新增 npm 包要求。
