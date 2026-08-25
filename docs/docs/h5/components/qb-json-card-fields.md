# QbJsonCardFields

## 用途

JSON 配置驱动的卡片 meta 字段区。 按 columns 渲染 qb-row / qb-col-* / qb-kv；支持 text、dict（QbDictTag）、slot。

## 导入

```js
import QbJsonCardFields from '@/components/qb/QbJsonCardFields.vue'
```

源码：`quick-h5/src/components/qb/QbJsonCardFields.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| row | Record&lt;string, unknown&gt; | - | 见源码 |
| columns | QbCardColumn[] | - | 见源码 |


## Events

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Slots

| 插槽 | 说明 |
| --- | --- |
| default | 默认插槽 |


## 示例

```vue
<template>
  <QbJsonCardFields />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
