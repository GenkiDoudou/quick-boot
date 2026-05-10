## Context

- **quick-ui**：Vue 3 + Element Plus；各页复制订单号/链接时重复处理 **Clipboard API**、**execCommand** 与 **ElMessage**。
- **原始需求**：**`原始需求/前端/C7复制.md`**。
- **proposal** 已定：**`C7Copy`** 多 **`mode`**、**`getCopyText`**（含 **Promise**）、**`notify` 注入**、**`beforeCopy` / `afterCopy`**、事件 **`copy` / `success` / `error`**。

## Goals / Non-Goals

**Goals**

- **复制串来源**：**`text`**（**`String` / `Number`**，`null`/`undefined` 安全）；或 **`getCopyText`**（同步或 **Promise**）返回最终写入剪贴板的字符串。
- **复制路径**：优先 **`navigator.clipboard.writeText`**；在 **不可用或 reject** 时 **降级 `document.execCommand('copy')`**（与原始需求一致）；两种均失败时 **进入 error 路径**。
- **展示模式**：**`mode`** 为 **`button` | `icon` | `text` | `clickable`**；**`none`** 作为 **`clickable`** 的 **历史别名**（见 **spec**）。
- **通知**：**`showMessage`** 控制是否调用默认 **`ElMessage`**；**`notify(type, message)`** 可 **完全替换**默认通知实现。
- **钩子**：**`beforeCopy`** 返回 **`false`** 时 **MUST** 阻止复制；**`afterCopy`** 在 **成功写入剪贴板后**调用。
- **禁用**：**`disabled=true`** 时 **MUST NOT** 触发复制、**MUST NOT** 触发默认成功/失败提示（**`beforeCopy` 是否仍调用**见 **Decisions**）。

**Non-Goals**

- 不实现 **跨域 iframe** 内复制、**图片/文件** 型剪贴板写入。
- 不提供 **权限持久化**（**`clipboard-read`** 等）管理 UI。

## Decisions

1. **`beforeCopy` 与 `disabled`**  
   - **`disabled=true`**：**不调用** **`beforeCopy`**，**不 emit** **`copy/success/error`**。  
   - **理由**：禁用即无操作，避免业务钩子在禁用态误触发。

2. **`getCopyText` 与 `text` 优先级**  
   - **若 `getCopyText` 为函数**：**以 `getCopyText` 返回值为准**（实现传入规范化后的 **`text` 字符串**作为参数，与原始需求 **`getCopyText(text)`** 对齐）；**`text` prop 仍参与传入**。  
   - **若未提供 `getCopyText`**：使用 **`text`** 规范化结果。

3. **`execCommand` 降级**  
   - 使用 **临时 **`textarea`** / `input`** 选中并 **`execCommand('copy')`** 的经典模式；**完成后 MUST 移除**临时节点并 **恢复选区**（尽力而为，JSDoc 说明限制）。

4. **事件顺序**（与 **`spec.md`** 一致）  
   - 成功路径：**`emit('copy', resolvedText)`**（已解析 **`getCopyText`**，尚未写入）→ **写剪贴板成功** → **`afterCopy(resolvedText)`**（若存在）→ **`emit('success', resolvedText)`** → **默认 **`ElMessage`**（若启用）**。  
   - **`beforeCopy` 返回 false**：**不**解析 **`getCopyText`**、**不 emit** **`copy/success/error`**，**不调用** **`afterCopy`**。

5. **`notify` 签名**  
   - 固定为 **`(type: 'success' | 'error' | 'info' | 'warning', message: string) => void`**（与 **`ElMessage`** 常用形态对齐）；未注入时使用 **`ElMessage`**。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **非安全上下文无 Clipboard API** | 自动走 **`execCommand`**；**spec** 要求验收 |
| **异步 `getCopyText` 重复点击** | 实现 **inFlight** 忽略或排队 **二选一**，JSDoc 固定 |

## Migration Plan

- 新页使用 **`C7Copy`**；旧工具函数可逐步下线，无数据迁移。

## Open Questions

- （无）**`mode=icon`** 时图标名/尺寸跟随 **`ElButton` text** 或 **`ElIcon`** 由实现选定并在 JSDoc 给默认值。
