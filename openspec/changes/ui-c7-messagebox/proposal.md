## Why

项目中 **`ElMessageBox` / `ElLoading`** 的调用分散，**标题、按钮文案、遮罩行为**等不一致，且 **取消/关闭** 常依赖 **Promise reject**，业务侧 **`try/catch`** 写法不统一。需在 **quick-ui** 提供 **C7MessageBox** 函数式工具（见 **`原始需求/前端/C7消息弹窗工具.md`** 与已定稿 **`docs/superpowers/specs/2026-05-08-c7-messagebox-design.md`**），统一默认值、**结构化返回值**与 **`asyncConfirm` + loading** 行为。

## What Changes

- 新增 **`quick-ui/src/packages/C7MessageBox/`**（**无 `.vue`**）：导出 **`setMessageBoxDefaults`**、**`c7Confirm`**、**`c7Alert`**、**`c7Prompt`**、**`c7DangerConfirm`**、**`c7Loading`**。
- **全局默认**：**`setMessageBoxDefaults(config)`** 使用模块级默认值；每次调用与入参 **浅合并**，单次入参优先。
- **统一返回**：上述对话框类 API 均 **resolve** 为 **`{ action: 'confirm'|'cancel'|'close', value?: string }`**；**用户取消/关闭** 路径 **不**以 reject 暴露给调用方（与 Element Plus **`MessageBoxData.action`** 语义对齐）。
- **`c7Confirm` + `asyncConfirm`**：确定后 **loading**（优先 **`confirmButtonLoading`**，不足时 **`ElLoading.service`**）；成功关闭；失败 **保持弹窗** 并调用 **`errorNotify(err)`**（**不**在工具内默认 **`ElMessage`**）。
- **`c7Loading`**：基于 **`ElLoading.service`**，返回 **`{ close() }`**。
- **`c7DangerConfirm`**：**`c7Confirm`** 的预设变体（危险确认按钮样式等）。
- 在 **`quick-ui/src/packages/index.js`** 增加 **命名导出**；**不**通过 **`installPackages`** 注册组件。
- **第一版不**批量替换 **`C7Button` / `C7Switch`** 等存量 **`ElMessageBox`** 调用；文档与（可选）单测见 **tasks**。

## Capabilities

### New Capabilities

- **`ui-c7-messagebox`**：**`setMessageBoxDefaults`**、**`c7Confirm` / `c7Alert` / `c7Prompt` / `c7DangerConfirm`** 的默认值合并、**统一返回值**、**`asyncConfirm` + `errorNotify`**、**`c7Loading`**、**`packages/index.js` 导出**及与 **Element Plus** 的透传/映射约定。

### Modified Capabilities

- （无）

## Impact

- **代码**：新增 **`C7MessageBox`** 目录；修改 **`quick-ui/src/packages/index.js`**。
- **应用入口**：业务可在 **`main.ts`** 可选调用 **`setMessageBoxDefaults`**。
- **文档**：VitePress 侧与 C7 系列风格一致的说明页（实现阶段落地）。
- **依赖**：沿用现有 **Vue 3 + Element Plus**，不新增 npm 包除非实现中发现硬性缺口并经 **design** 记录理由。
