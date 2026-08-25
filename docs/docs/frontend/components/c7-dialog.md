# C7Dialog

## 用途

C7 弹窗 / 抽屉：在 **`ElDialog` / `ElDrawer`** 上统一 **footer、异步确定、双 v-model** 与 **`modalProps` 透传**。 **显隐**：以 **`modelValue`** 为主；未传 **`modelValue`** 时回退 **`visible`**。关闭时 **同时** **`emit('update:modelValue', false)`** 与 **`emit('update:visible', false)`**。 若二者 **同时显式传入** 且 **布尔不一致**，开发环境 **至多每个打开周期** **`console.warn` 一次**（以 **`modelValue`** 为准）。 **确定**：传入 **`onConfirm`** 时点击确定会 **await** 其结果；**resolve** 

## 导入

```js
import C7Dialog from '@/packages/C7Dialog/index.vue'
```

源码：`quick-ui/src/packages/C7Dialog/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| modelValue | Boolean | undefined} | 兼容 `v-model:visible` |
| title | String | ''} | 为 `true` 且无 `#footer` 时渲染默认「取消 / 确定」；有 `#footer` 时始终展示插槽内容 |
| title | slots.title | - | 见组件注释 / 源码 |
| width | props.width | - | 见组件注释 / 源码 |
| appendToBody | true | - | 见组件注释 / 源码 |
| title | slots.title | - | 见组件注释 / 源码 |
| size | props.size | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| update | 见 `@emits` / defineEmits |
| update | 见 `@emits` / defineEmits |
| open | 见 `@emits` / defineEmits |
| opened | 见 `@emits` / defineEmits |
| close | 见 `@emits` / defineEmits |
| closed | 见 `@emits` / defineEmits |
| cancel | 见 `@emits` / defineEmits |
| confirm | 见 `@emits` / defineEmits |
| submit | 见 `@emits` / defineEmits |


## Slots

| 插槽 | 说明 |
| --- | --- |
| title | 具名插槽 |
| default | 默认插槽 |
| extra | 具名插槽 |


## 示例

```vue
<template>
  <C7Dialog />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
