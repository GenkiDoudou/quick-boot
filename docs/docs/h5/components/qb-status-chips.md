# QbStatusChips

## 用途

状态单选芯片组：v-model 绑定字典 value，支持禁用指定选项。

## 导入

```js
import QbStatusChips from '@/components/qb/QbStatusChips.vue'
```

源码：`quick-h5/src/components/qb/QbStatusChips.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| modelValue | string | - | 见源码 |
| options | DictOption[] | - | 见源码 |
| disabled | boolean | - | 见源码 |
| disabledValues | string[] | - | 见源码 |


## Events

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <QbStatusChips />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
