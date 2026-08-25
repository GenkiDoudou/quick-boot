# C7Title

## 用途

C7 区块标题：统一字号/加粗、底部分割线、左侧图标（EP 图标名或 `#icon`）、右侧默认插槽操作区。 **`resolvedTag`（语义标签）** - **`tag` prop 为 `undefined`**（未传）：若 **`labelSize`** 为 **`h1`~`h6`**，则语义标签与 **`labelSize`** 同级；否则为 **`h4`**。 - **`tag` 已传**：始终使用该标签（含显式 **`h4`**），**不**被 h 级 **`labelSize`** 改写语义层级。 **字号** - **`labelSize`** 为 **`h1`~`h6`**：按上表预设；与 **`tag`** 解耦（例如 **`labelSize=h2`** + **`tag=div`** 时仍为 h2 档字号）。 - **`labelSize`** 为带 **`px`

## 导入

```js
import C7Title from '@/packages/C7Title/index.vue'
```

源码：`quick-ui/src/packages/C7Title/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| tag | String | undefined | 见组件注释 / 源码 |
| label | String | ''} | `label` 的兼容别名 |
| fontSize | props.labelSize.trim | - | 见组件注释 / 源码 |


## Events

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Slots

| 插槽 | 说明 |
| --- | --- |
| icon | 具名插槽 |
| title | 具名插槽 |
| default | 默认插槽 |


## 示例

```vue
<template>
  <C7Title />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
