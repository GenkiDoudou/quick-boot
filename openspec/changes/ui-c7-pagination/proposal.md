## Why

列表页分页在 **Element Plus `ElPagination`** 上重复绑定 **`v-model:currentPage` / `v-model:pageSize`**，且「**切换每页条数后回到第一页**」与「**统一 `change` 回调**」在各页实现不一致。原始说明见 `原始需求/前端/C7分页.md`。

## What Changes

- 在 **quick-ui** 提供 **`C7Pagination`**：基于 **`ElPagination`**，支持 **`v-model:currentPage`** 与 **`v-model:pageSize`**。
- **`autoReset=true`（默认）** 时：用户切换 **`pageSize`** 后 **将 `currentPage` 置为 1**；并对 **`change`** 做 **单次** 汇总（见 design：避免 EP 在受控模式下因 `post` flush 连续 **`change` 两次**）。
- **透传**：除本组件显式声明的契约项外，其余 **`ElPagination`** 能力通过 **`inheritAttrs: false` + `v-bind` 透传**（与 **`C7Select`** 家族风格一致）。
- **事件**：**`update:currentPage` / `update:pageSize`**、**`current-change` / `size-change` / `prev-click` / `next-click`**，以及统一 **`change(page, pageSize)`**。
- 在 **`packages/index.js`** 中随 **`installPackages`** 全局注册 **`C7Pagination`**；可选 Dev 演示路由（见 tasks）。

## Capabilities

### New Capabilities

- **`ui-c7-pagination`**：**`C7Pagination`** 的 **`autoReset`** 语义、**`change` 单次与最终态**、与 **`ElPagination`** 的透传边界（见 delta spec）。

### Modified Capabilities

- （无）不修改后端规格。

## Impact

- **代码**：新增 `quick-ui/src/packages/C7Pagination/index.vue`；修改 `quick-ui/src/packages/index.js`；可选 `quick-ui/src/views/dev/C7PaginationE2E.vue` 与路由。
- **文档**：本变更目录下 `proposal` / `design` / `tasks` / `specs/ui-c7-pagination/spec.md`。
