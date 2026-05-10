## Context

- **quick-ui**：Vue 3 + Element Plus；**`C7Button`** 已实现完整点击流水线（见 **`openspec/changes/ui-c7-button`**）。
- **原始需求**：`原始需求/前端/C7按钮组.md`。
- **已确认约束**（评审拍板）：
  1. 折叠进下拉的命令 MUST **仍走同一条 `clickFunction` / 确认链**（即 **`C7Button` 内部流水线**，不得用下拉 **`el-dropdown-item` 裸 `@click` 调业务函数替代）。
  2. **`before-command` / `after-command`** 与 **`C7Button`** 既有 emit **并存**，语义分层而非替换。

## Goals / Non-Goals

**Goals**

- **`inline` / `dropdown` / `auto`** 三种 **`mode`**，**`auto`** 下 **`maxVisible`** 控制外露数量，其余进入「更多」。
- **数据模式**与**插槽模式**在「谁可见、谁进下拉、点击后行为」上 **一致**。
- **`hidden`**：不参与外露计数（折叠算法仅统计「可见」按钮）；实现须在 spec 与代码注释中与 **`disabled`** 区分清楚。
- **`spacing`**：支持预设或数值（px）；**`size`** 传递给子 **`C7Button`**（与 Element 尺寸一致）。
- **`responsive`**：向根元素写入约定 **class**（具体类名在实现时固定并写入组件 JSDoc），便于业务 CSS 扩展。
- **`forceUpdate()`**：组件实例方法，插槽子节点增删改后调用以 **同步折叠分区**。

**Non-Goals**

- 不实现权限指令内置逻辑（业务仍可在外层包裹 **`v-hasPermi`** 等）。
- 不要求下拉内按钮与平铺在像素级完全一致（但交互与流水线必须一致）。

## Architecture Sketch

```
┌─────────────────────────────────────────────────────────┐
│ C7ButtonGroup                                            │
│  mode / maxVisible / spacing / more* / trigger          │
├─────────────────────────────────────────────────────────┤
│  [外露区: C7Button × N]     [更多 ▼ → 下拉内的 C7Button]   │
│       │                              │                   │
│       └──────────┬───────────────────┘                   │
│                  ▼                                       │
│           同一 C7Button 流水线（校验/确认/clickFunction）   │
│                  │                                       │
│    emit: before-click / success / error / after-click    │
│    emit: before-command(item) / after-command({item,success}) │
└─────────────────────────────────────────────────────────┘
```

**实现取向（推荐）**：下拉面板内渲染 **真实的 `C7Button`**（或与被外露按钮 **同一套 vnode/实例语义**），保证 **防抖、`busy`、`internalLoading`** 与确认框行为与文档一致。避免「仅菜单文案 + 手动调用 `clickFunction()`」复制 **`C7Button`** 内部逻辑。

## Decisions

1. **命令路径**  
   - 折叠项 MUST 触发 **`C7Button`** 的标准入口（用户可见为点击按钮或等价可访问动作），从而 **自动** 包含防抖与 **`busy`**。  
   - 若采用 **`el-dropdown`**：关闭下拉时机与 **`ElMessageBox.confirm`** 叠加时，须验证无焦点陷阱；必要时在 design 迭代中补充。

2. **组级事件载荷 `item`**  
   - **数据模式**：**`item`** 为 **`buttons[]`** 中对应项的规范化引用（建议包含稳定 **`key`** 或数组索引 + 快照字段，避免仅引用可变对象无说明）。  
   - **插槽模式**：**`item`** MUST 包含 **`slotIndex`**（或等价）与可选 **`name`**（若子 **`C7Button`** 提供 **`data-command`** 或 **`name` prop** 之类约定，在实现时写死契约）；若无显式标识，**`item`** 至少含 **`slotIndex`**。

3. **`before-command` / `after-command` 时机**  
   - **`before-command`**：在「用户意图触发某一子按钮命令」时、**进入该子按钮流水线之前** 触发（与 **`C7Button` 的 `before-click`** 顺序：**组级先于或后与按钮级** 须在实现中二选一并写入 spec；**推荐**：**`before-command` → `C7Button` 内部 `before-click`**，**`after-command` 在 `C7Button` 的 `after-click` 之后**，以便组级拿到「整条命令结束」语义）。  
   - 若 **`before-command`** 允许否决扩展（初始可为「仅观测」不 veto），须在后续迭代单独开需求；**本变更默认**：组级事件 **不中止** 流水线（除非 spec 另行规定）。

4. **`forceUpdate`**  
   - 使用 **`nextTick` + 内部重新扫描插槽子项**（或计数刷新），不代替 Vue 常规响应式；文档注明 **动态插槽** 场景必须调用。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 下拉内嵌真实按钮与 EP 样式冲突 | 用 **`dropdown`** 的 **`teleported`** / 自定义插槽样式微调；E2E 截图或视觉自检 |
| 插槽扫描误判非 **`C7Button`** | 文档写明 **仅识别 `C7Button`**；其余节点 **忽略或单列条款**（spec：默认忽略） |
| 组级与按钮级事件顺序混淆 | spec 固定顺序；代码注释与 Dev 页示例 |

## Migration Plan

- 新列表工具栏优先 **`C7ButtonGroup` + `buttons[]`**；旧页可逐步改为插槽或数据模式。
- 与 **`C7Button`** 共用 **`btnType` 预设**，无需迁移单按钮 API。

## Open Questions

- **`before-command` 是否需要 veto（返回 false 中止）**：当前默认 **否**；若产品需要，单列小变更。
- Dev/E2E 路由是否与 **`/dev/c7-button-e2e`** 同页分区还是 **`/dev/c7-button-group-e2e`**：实现阶段按 tasks 选择。
