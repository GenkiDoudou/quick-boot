# C7Dialog 弹窗 / 抽屉

在 **`ElDialog` / `ElDrawer`** 上统一 **footer、异步确定、双 v-model（`modelValue` + 兼容 `visible`）** 与 **`modalProps` 透传**。

**源码**：`quick-ui/src/packages/C7Dialog/index.vue`  
**Dev 页**：开发服务器启动后访问 `/dev/c7-dialog-e2e`

## 功能概要

- **`mode`**：`dialog` | `drawer`。
- **标题**：`title` 文案 prop；需完全自定义标题区时使用 **`#title`** 插槽（内部映射为 Element Plus 的 **`#header`**）。
- **显隐**：优先 **`v-model:modelValue`**；否则 **`v-model:visible`**。关闭时 **同时** `update:modelValue` 与 `update:visible` 为 **`false`**。二者同时传入且不一致时，开发环境 **warn**，以 **`modelValue`** 为准。
- **默认 footer**：`footer=true` 且无 **`#footer`** 时，左侧 **`#extra`**，右侧 **取消 / 确定**；提供 **`#footer`** 时 **仅渲染插槽**。
- **`onConfirm`**：点击确定 **await**；**resolve** 后自动关闭；**reject/throw** 不关、无内置 **`ElMessage`**。确定钮 **loading**：**`confirmLoading !== undefined`** 时以外部为准，否则为内部 **`onConfirm` pending**。
- **无 `onConfirm`**：依次 **`emit('confirm')`、`emit('submit')`**，不自动关窗。
- **`@cancel`**：点击取消时触发并已关闭。
- **卸载**：若仍打开，**双 emit `false`** 同步父状态。
- **透传**：根上非监听属性与 **`modalProps`** 合并进壳层，**后合并覆盖**（调用方优先）。

## 与全局注册

在 `main.js` 已调用 `installPackages(app)` 时，模板中可直接使用 `<c7-dialog />`（或 `<C7Dialog />`）。

## 相关规格

OpenSpec 变更：`openspec/changes/ui-c7-dialog/specs/ui-c7-dialog/spec.md`
