# QbListFooter

## 用途

列表底部状态区：空态 / 加载中 / 没有更多。

## 导入

```js
import QbListFooter from '@/components/qb/QbListFooter.vue'
```

源码：`quick-h5/src/components/qb/QbListFooter.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| loading | boolean | - | 见源码 |
| finished | boolean | - | 见源码 |
| empty | boolean | - | 见源码 |
| emptyText | string | - | 见源码 |
| hasRows | boolean | - | 见源码 |


## Events

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <QbListFooter />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
