# C7Pagination

## 用途

C7 业务分页：在 `ElPagination` 上统一 **双绑**、**切换 pageSize 后回第一页（`autoReset`）** 与 **`change(page, pageSize)` 单次汇总**。 **`change` 语义**：每次用户操作导致的 **最终** `currentPage` + `pageSize` 只通知一次。`autoReset=true` 且用户改条数时，内部会忽略 Element Plus 在同一更新周期内可能产生的 **中间态** `change`，并在 `nextTick` 中 **`emit('change', 1, newPageSize)`**。 **`autoReset=false`**：不强制回第 1 页；页码纠正与 `ElPagination` 一致。

## 导入

```js
import C7Pagination from '@/packages/C7Pagination/index.vue'
```

源码：`quick-ui/src/packages/C7Pagination/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| currentPage | Number | undefined} | 每页条数，与 `v-model:pageSize` 同步 |


## Events

| 事件 | 说明 |
| --- | --- |
| update | 见 `@emits` / defineEmits |
| update | 见 `@emits` / defineEmits |
| current-change | 当前页变化（与 EP 一致） |
| size-change | 每页条数变化 |
| prev-click | / next-click |
| change | 见 `@emits` / defineEmits |
| next-click | 见 defineEmits |


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <C7Pagination />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
