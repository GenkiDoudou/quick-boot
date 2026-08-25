# C7Select

## 用途

C7 业务下拉：在 `ElSelect` 上统一静态/异步/远程选项加载与多选 `separator` 对外格式。 **静态来源**：`dataList` 与 `options` 为别名语义；**同时存在时 `dataList` 优先**（见 props 说明）。 **远程**：首次展开下拉触发 **不含 `query` 键** 的 `fetchData`；输入关键字经防抖后触发 `{ ...fetchParams, query }`。 **多选 + `separator`**：对外 `v-model` / `change` 为逗号分隔字符串，空为 `''`；对内始终数组。外部逗号字符串会解析为数组； 若部分 value 不在当前选项中，**不静默删除**（保留策略）。 **限制**：若 option 的 `value` 本身可能含英文逗号，请勿使用 `separator` 模式。

## 导入

```js
import C7Select from '@/packages/C7Select/index.vue'
```

源码：`quick-ui/src/packages/C7Select/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| dataList | Array | undefined} | `dataList` 别名；  若 `dataList` 已传入（含 `undefined` 占位由父组件决定）以 `dataList` 为准  ——此处简化为：`dataList !== undefined` 时只用 `dataList` |
| loading | loadingInternal | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| update | 见 `@emits` / defineEmits |
| change | 载荷与 `update:modelValue` 一致 |
| visible-change | 透传 `ElSelect` 可见性 |
| loading-change | `fetchData` 并发计数 &gt;0 为 true |


## Slots

| 插槽 | 说明 |
| --- | --- |
| prefix | 具名插槽 |
| label | 具名插槽 |
| option | 具名插槽 |
| empty | 具名插槽 |


## 示例

```vue
<template>
  <C7Select />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
