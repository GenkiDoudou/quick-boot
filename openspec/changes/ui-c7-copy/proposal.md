## Why

业务页中频繁复制订单号、密钥、链接等文本，各页面自行拼接 **Clipboard API**、**execCommand** 降级与 **ElMessage** 提示，行为不一致且难维护。原始说明见 `原始需求/前端/C7复制.md`；需在 **quick-ui** 内提供统一的 **`C7Copy`** 轻量组件。

## What Changes

- 新增 **`C7Copy`**（命名与原始需求一致）：多种 **`mode`**（`button` / `icon` / `text` / `clickable`，并兼容历史别名 **`none`≈仅插槽可点区域**）、**`text`** 支持 **String/Number** 且 **null/undefined** 安全、**`getCopyText`** 支持同步或 **Promise** 动态生成最终复制串。
- **复制路径**：优先 **`navigator.clipboard.writeText`**；不可用或失败时 **`document.execCommand('copy')`** 降级（与原始需求一致）。
- **通知**：**`showMessage`** 控制是否走默认提示；支持注入 **`notify(type, message)`** 覆盖默认 **ElMessage**。
- **钩子**：**`beforeCopy`** 返回 **`false`** 时阻止复制；**`afterCopy`** 在复制成功且已写入剪贴板后调用。
- **事件**：**`copy`**、**`success`**、**`error`**（载荷形状见 delta spec）。
- **禁用**：**`disabled=true`** 时不触发复制及相关提示。
- **集成**：在 **`quick-ui/src/packages/index.js`** 中导出并 **`installPackages`** 全局注册 **`C7Copy`**；可选 Dev 演示路由/页与自动化测试（见 tasks）。

## Capabilities

### New Capabilities

- **`ui-c7-copy`**：**`C7Copy`** 的 props（**`text` / `getCopyText` / `mode` / `disabled` / `showMessage` / `notify`**）、钩子（**`beforeCopy` / `afterCopy`**）、事件（**`copy` / `success` / `error`**）、**Clipboard + execCommand** 降级顺序与错误语义、**空值与异步 `getCopyText`** 的验收标准。

### Modified Capabilities

- （无）新增前端 packages 能力；不修改后端或其它已发布 spec 的对外契约。

## Impact

- **代码**：新增 `quick-ui/src/packages/C7Copy/`（至少 **`index.vue`**）；修改 **`quick-ui/src/packages/index.js`**。
- **文档**：本变更目录下 **proposal / design / tasks** 与 **`specs/ui-c7-copy/spec.md`**；**`docs`** 侧栏若已有 **C7Copy** 链对应页面，实现阶段可补全或修正链接。
- **依赖**：以现有 **Vue 3**、**Element Plus**（**ElMessage**、按需 **`ElButton`** 等）为准，不引入新的复制专用 npm 包除非 design 中论证必要。
