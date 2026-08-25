# C7TimePicker

## 用途

C7 时间选择器：基于 **Element Plus 2.10+** 的 **`ElTimePicker`**。 **默认格式**：当 **`format`、** **`valueFormat`、** **`value-format` 均为 `undefined`** 时，同时注入 **`format` 与 `valueFormat` 为 `HH:mm:ss`**；**任一已传**则不注入对应项，与裸 **`ElTimePicker`** 一致。 **范围模式**（**`is-range` / `isRange` 为真**）： - **`rangeMerge`**（默认 **`false`**）：为 **`true`** 时 **`v-model`** 对外为 **单字符串**（两段用 **`mergeDelimiter`** 拼接）；为 **`false`** 时对外为 **EP 原生范

## 导入

```js
import C7TimePicker from '@/packages/C7TimePicker/index.vue'
```

源码：`quick-ui/src/packages/C7TimePicker/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| modelValue | Boolean | undefined} | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| update | 见 `@emits` / defineEmits |
| change | 归一化后的值（与  `update:modelValue`  形态一致） |
| blur | 见 `@emits` / defineEmits |
| focus | 见 `@emits` / defineEmits |


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <C7TimePicker />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
