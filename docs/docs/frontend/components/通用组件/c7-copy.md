# C7Copy 复制

轻量纯文本复制组件：优先 **Clipboard API**，失败或不满足安全上下文时 **`document.execCommand('copy')`** 降级；可配置 **`ElMessage`** 或自定义 **`notify`**。

**源码**：`quick-ui/src/packages/C7Copy/index.vue`  
**Dev 页**：开发服务器启动后访问 `/dev/c7-copy-e2e`

## 功能概要

- **`text`**：`String` / `Number` / `null`；`null`/`undefined` 按空串参与复制链。
- **`getCopyText(baseText)`**：可选，同步或 **Promise**，返回最终写入剪贴板的字符串。
- **`mode`**：`button` | `icon` | `text` | `clickable`；`none` 为 **`clickable`** 的兼容别名。
- **`beforeCopy`**：返回严格 **`false`** 时中止（不调用 `getCopyText`、不写剪贴板、不 `emit`）。
- **`afterCopy`**：写入成功后、`emit('success')` 之前调用。
- **事件**：`copy`（写入前）、`success`、`error`。
- **`notify(type, message)`**：若传入，成功/失败 **仅**走该回调，不再使用 **`ElMessage`**。

## 与全局注册

在 `main.js` 已调用 `installPackages(app)` 时，模板中可直接使用 `<c7-copy />`（或 `<C7Copy />`）。

## 相关规格

OpenSpec 变更：`openspec/changes/ui-c7-copy/specs/ui-c7-copy/spec.md`
