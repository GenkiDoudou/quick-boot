# C7CardGrid

## 用途

卡片网格列表壳：搜索区 + 可选工具栏 + 卡片网格 + 分页。 数据请求与分页逻辑对齐 {

## 导入

```js
import C7CardGrid from '@/packages/C7CardGrid/index.vue'
```

源码：`quick-ui/src/packages/C7CardGrid/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| listFunction | Function | () =&gt; [] } | 网格最小列宽（px），用于 auto-fill |
| pageNum | currentPage.value | - | 见组件注释 / 源码 |
| pageSize | currentPageSize.value | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| before-fetch | 见 defineEmits |
| after-fetch | 见 defineEmits |
| fetch-error | 见 defineEmits |
| add-click | 见 defineEmits |
| add-card-click | 见 defineEmits |
| search-submit | 见 defineEmits |
| search-reset | 见 defineEmits |


## Slots

| 插槽 | 说明 |
| --- | --- |
| default | 默认插槽 |
| search-extra | 具名插槽 |
| toolbar-left | 具名插槽 |
| toolbar-right | 具名插槽 |
| add-card | 具名插槽 |
| card | 具名插槽 |


## 示例

```vue
<template>
  <C7CardGrid />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
