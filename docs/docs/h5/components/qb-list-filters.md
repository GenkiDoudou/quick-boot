# QbListFilters

## 用途

列表页筛选条：在搜索栏下方展示「全部 + 字典选项」芯片。 空字符串表示不传筛选（全部）；变更由外层 v-model 驱动 usePagedList.filters。

## 导入

```js
import QbListFilters from '@/components/qb/QbListFilters.vue'
```

源码：`quick-h5/src/components/qb/QbListFilters.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| modelValue | string | - | 见源码 |
| options | DictOption[] | - | 见源码 |
| label | string | - | 见源码 |


## Events

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <QbListFilters />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
