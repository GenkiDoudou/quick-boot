# QbDictTag

## 用途

字典值展示为圆角标签：优先用 options 解析文案与色调，否则按 0/1 正常停用兜底。

## 导入

```js
import QbDictTag from '@/components/qb/QbDictTag.vue'
```

源码：`quick-h5/src/components/qb/QbDictTag.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| value | string \| number \| null | - | 见源码 |
| options | DictOption[] | - | 见源码 |
| fallbackNormal | boolean | - | 见源码 |


## Events

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <QbDictTag />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
