## 1. 组件与注册

- [x] 1.1 新增 `quick-ui/src/packages/C7Pagination/index.vue`：基于 **`ElPagination`**，实现 **`v-model:currentPage` / `v-model:pageSize`**、**`autoReset`（默认 true）** 下 **`size-change` 后回第一页**、以及 **`change(page, pageSize)` 单次汇总**（见 `design.md`）
- [x] 1.2 透传 **`ElPagination`** 其余 props / 属性；**`inheritAttrs: false`**
- [x] 1.3 事件：**`update:currentPage` / `update:pageSize`**、**`current-change` / `size-change` / `prev-click` / `next-click` / `change`**
- [x] 1.4 **`packages/index.js`** 导出并 **`installPackages`** 注册 **`C7Pagination`**

## 2. 与规格对齐校验

- [x] 2.1 对照 `openspec/changes/ui-c7-pagination/specs/ui-c7-pagination/spec.md` 核对 **`autoReset`** 与 **`change`** 语义
- [x] 2.2 验收：**`autoReset=true`** 时切换 **`pageSize`** → **`currentPage` 为 1** 且 **`change`** 为 **`(1, newSize)`** 且 **不重复**

## 3. 工程与健康

- [x] 3.1 `quick-ui` 生产构建通过（**`pnpm build:prod`** 或项目等价脚本）

## 4. Dev 演示（可选但推荐）

- [x] 4.1 增加 **`C7PaginationE2E`** Dev 页与 **`/dev/c7-pagination-e2e`** 路由，便于手工验收 **条数切换 + `change` 日志**
