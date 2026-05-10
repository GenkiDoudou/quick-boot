## Context

- **quick-ui**：Vue 3 + Element Plus（当前锁定 **`element-plus` ^2.10**）；**`C7Select` / `C7Button`** 等已存在于 **`src/packages`**。
- **原始需求**：`原始需求/前端/C7分页.md`。
- **Element Plus `ElPagination` 行为要点**（源码 `pagination.mjs`）：**`pageSize`** 更新后会 **`watch([currentPageBridge, pageSizeBridge], …, { flush: 'post' })`** 向外 **`emit('change', currentPage, pageSize)`**；在受控 **`v-model`** 下，若业务侧再将页码同步到 **1**，同一轮用户操作内可能出现 **两次 `change`（中间态 + 最终态）**。

## Goals / Non-Goals

**Goals**

- **双绑**：**`v-model:currentPage`**、**`v-model:pageSize`**（对外 emit 使用 **`update:currentPage` / `update:pageSize`** 驼峰，与 Vue 3 习惯一致）。
- **`autoReset`**：**`true`（默认）** 时，用户切换 **`pageSize`** 后 **必须** **`emit('update:currentPage', 1)`**；**`false`** 时 **不**强制回第一页，**页码越界纠正**与 **`ElPagination`** 一致（其内部在条数变化后可能将当前页 **钳制到不超过 `pageCount`**）。
- **统一 `change(page, pageSize)`**：对调用方 **每次「用户导致分页状态变化」仅触发一次**，且载荷为 **最终稳定的 `currentPage` 与 `pageSize`**；**`autoReset=true`** 的条数切换场景下 **必须为 `(1, newPageSize)`**。
- **其余事件**：**`size-change` / `current-change` / `prev-click` / `next-click`** 与 EP 语义对齐并 **再向外转发**。

**Non-Goals**

- 不在本组件内封装 **列表数据请求** 或 **与 `PageRequest` 的字段映射**（由页面自行在 **`change`** 中触发）。
- 不改变 **`ElPagination`** 对 **`total` / `pageCount` / `layout`** 的合法组合校验规则。

## Decisions

1. **`change` 的实现策略（拍板）**  
   - **正常路径**：监听内部 **`ElPagination` 的 `change`** 并 **原样转发**（此时 EP 自身已保证单次、最终态）。  
   - **`autoReset=true` 且发生 `size-change`**：在 **`emit('update:currentPage', 1)`** 的同一轮更新周期内，**临时忽略**内部 **`change`**（避免 **(旧页, 新条数)** 与 **(1, 新条数)** 双发）；在 **`nextTick`** 中 **手动 `emit('change', 1, newPageSize)` 一次**。

2. **`autoReset` 默认值**  
   - 与原始需求表述一致：**默认 `true`**。

3. **透传**  
   - **`inheritAttrs: false`**；将 **`$attrs`**（剔除与本组件 **重复绑定** 的键）合并到 **`ElPagination`**，以便 **`pagerCount` / `teleported` / `pageCount`** 等随版本扩展的 props 无需在本封装中逐一声明。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| EP 小版本差异导致事件名变化 | 以当前仓库 **`element-plus` 版本**为准；在组件 JSDoc 标明依赖的 **`ElPagination` 事件** |
| 父组件完全受控更新延迟 | 使用 **`nextTick`** 再发汇总 **`change`**，与 Vue 更新时序对齐 |

## Migration Plan

- 新列表页优先 **`C7Pagination`**；旧页可逐步替换裸 **`ElPagination`**。
