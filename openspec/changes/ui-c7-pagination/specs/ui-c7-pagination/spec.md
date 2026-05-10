# ui-c7-pagination

## Purpose

为 **quick-ui** 提供 **`C7Pagination`**：在 **Element Plus `ElPagination`** 之上统一 **双绑**、**切换每页条数后回到第一页（可关）**，以及 **`change(page, pageSize)` 单次回调**，减少列表页重复逻辑。需求来源：`原始需求/前端/C7分页.md`。

## Requirements

### Requirement: 组件与注册位置

**`C7Pagination`** MUST 位于 **`quick-ui/src/packages/C7Pagination`**，并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7Pagination`**；亦 MAY **`import { C7Pagination } from '@/packages'`** 按需使用。

### Requirement: 双绑（v-model）

系统 MUST 支持：

- **`v-model:currentPage`**：通过 **`emit('update:currentPage', number)`** 与 **`currentPage` prop**（或等价受控绑定）与父组件同步。
- **`v-model:pageSize`**：通过 **`emit('update:pageSize', number)`** 与 **`pageSize` prop** 与父组件同步。

### Requirement: autoReset

- 当 **`autoReset` 为 true（默认）** 时：用户操作导致 **`pageSize`** 变化后，组件 MUST **`emit('update:currentPage', 1)`**（将当前页重置为第一页）。
- 当 **`autoReset` 为 false** 时：组件 MUST **不**因条数切换而强制将当前页置为 1；当前页若因总页数变化需要纠正，行为 MUST 与 **`ElPagination`** 一致（通常为 **钳制到合法页码**，不一定为 1）。

### Requirement: 统一 change 事件

组件 MUST **`emit('change', page, pageSize)`**，其中 **`page`** 与 **`pageSize`** 为 **该次用户操作完成后的最终稳定值**。

- 在 **`autoReset=true`** 且用户切换 **`pageSize`** 的场景下：最终 **`change`** MUST 为 **`(1, newPageSize)`**。
- 在该场景下，组件 MUST **不**向父组件暴露 **多余的中间态 `change`**（例如先 **`(oldPage, newPageSize)`** 再 **`(1, newPageSize)`** 的连续两次）。

### Requirement: 其余事件与透传

组件 MUST **`emit`** 以下与 **`ElPagination`** 对齐的事件（载荷语义与 EP 一致）：

- **`current-change`**
- **`size-change`**
- **`prev-click`**
- **`next-click`**

除 **`autoReset`** 与显式声明的 **`v-model`** 相关绑定外，组件 SHOULD 将其余 **`ElPagination`** 支持的 props / 属性 **透传**到底层 **`ElPagination`**（以实现 **`layout` / `total` / `pageSizes` / `background` / `small` / `disabled` / `hideOnSinglePage`** 等能力随 EP 版本扩展）。

### Requirement: 验收场景

- **`autoReset=true`**：用户在 **`sizes`** 中切换 **`pageSize`** 后，界面当前页 MUST 为 **1**，且 **`change`** MUST **仅触发一次**，载荷为 **`(1, newPageSize)`**。
