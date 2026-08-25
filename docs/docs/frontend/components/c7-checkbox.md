# C7Checkbox

## 用途

C7 多选框：在 `ElCheckboxGroup` 上统一静态/异步选项加载、与 `C7Select` 一致的 **`response.data` → `resultKey` → `dataFormatter`** 解析链、 **`joinValue`** 对外编码（对齐 `C7Select` 的 **`separator`**：逗号串空值为 **`''`**）、以及可选「全选/半选」。 **静态优先**：若父级传入的 **`dataList` 不为 `undefined`**（含空数组），则选项来自 **`dataList`**，**不会**因 **`autoLoad`** 发起 **`fetchData`**。 **`reload()`**：仅当存在 **`fetchData`** 时重新请求；仅有静态 **`dataList`** 时为 **no-op**。 **`change`*

## 导入

```js
import C7Checkbox from '@/packages/C7Checkbox/index.vue'
```

源码：`quick-ui/src/packages/C7Checkbox/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| dataList | Array | undefined} | 异步加载：`mergedParams` 为  `{ ...fetchParams }`  （  不含 `query`  ）。 @param {Record&lt;string, &gt;} mergedParams @returns {Promise&lt;import('axios').AxiosResponse\|any&gt;} |
| label | label | - | 见组件注释 / 源码 |
| value | val | - | 见组件注释 / 源码 |
| disabled | raw | - | 见组件注释 / 源码 |
| loading | loadingInternal | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| update | 见 `@emits` / defineEmits |
| change | 始终 `string[]` |
| loading-change | `fetchData` 并发进行中为 true |


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <C7Checkbox />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
