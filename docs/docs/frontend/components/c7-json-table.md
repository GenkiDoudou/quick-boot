# C7JsonTable

## 用途

一体化 JSON 配置表格：包含搜索区、工具栏、表格、分页、列设置、删除等能力。 取消列表请求请使用 `beforeFetch` 属性，返回 `false` 或 `Promise<false>` 可阻止调用 `listFunction`。 `before-fetch` 事件仅用于监听参数，返回值不会参与拦截。 入参对齐后端 { `param` 为搜索条件，并在有排序时附带 `orderByColumn`、`isAsc`。

## 导入

```js
import C7JsonTable from '@/packages/C7JsonTable/index.vue'
```

源码：`quick-ui/src/packages/C7JsonTable/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| listFunction | Function | () =&gt; []} | 表格列配置（传给 C7JsonTableColumn） |
| current | currentPage.value | - | 见组件注释 / 源码 |
| size | currentPageSize.value | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| before-fetch | 见 defineEmits |
| after-fetch | 见 defineEmits |
| fetch-error | 见 defineEmits |
| selection-change | 见 defineEmits |
| sort-change | 见 defineEmits |
| delete-success | 见 defineEmits |
| export-success | 见 defineEmits |
| import-success | 见 defineEmits |
| add-click | 见 defineEmits |
| edit-click | 见 defineEmits |
| search-submit | 见 defineEmits |
| search-reset | 见 defineEmits |


## Slots

| 插槽 | 说明 |
| --- | --- |
| default | 默认插槽 |
| search-extra | 具名插槽 |
| toolbar-left | 具名插槽 |
| toolbar-right | 具名插槽 |


## 示例

```vue
<template>
  <C7JsonTable />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
