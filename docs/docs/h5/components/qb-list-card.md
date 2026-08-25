# QbListCard

## 用途

CRUD 列表卡片：标题 / 副标题 / 元信息 + status、actions 插槽。 支持 depth 缩进（部门树）；操作区将 qb-link 渲染为胶囊按钮。

## 导入

```js
import QbListCard from '@/components/qb/QbListCard.vue'
```

源码：`quick-h5/src/components/qb/QbListCard.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| title | string | - | 见源码 |
| subtitle | string | - | 见源码 |
| meta | string | - | 见源码 |
| depth | number | - | 见源码 |
| depthStep | number | - | 见源码 |


## Events

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Slots

| 插槽 | 说明 |
| --- | --- |
| title | 具名插槽 |
| subtitle | 具名插槽 |
| status | 具名插槽 |
| meta | 具名插槽 |
| actions | 具名插槽 |


## 示例

```vue
<template>
  <QbListCard />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
