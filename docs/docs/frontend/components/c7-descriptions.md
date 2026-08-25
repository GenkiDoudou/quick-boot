# C7Descriptions

## 用途

C7 描述列表：在 **`ElDescriptions`** 上以 **`data` + `items`** 配置驱动详情单元格，内置 **`tag`（`C7DictTag`）**、**`image`**、**`link`**、**`copy`/`copyable`** 与文本列。 **根透传**：除 **`data` / `items` / `defaultEmptyText`** 外，其余属性经 **`$attrs`** 传给 **`ElDescriptions`**（与 EP 文档一致）。 **`row` 与 `data`**：具名插槽 **`item.slotName`** 的作用域 **`row`** 与父传入的 **`data` 为同一引用**。若 **`data` 为 `null`/`undefined`**，**`row` 亦为 `null`/`undefined`**；点

## 导入

```js
import C7Descriptions from '@/packages/C7Descriptions/index.vue'
```

源码：`quick-ui/src/packages/C7Descriptions/index.vue`

## Props

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Events

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Slots

| 插槽 | 说明 |
| --- | --- |
| title | 具名插槽 |
| extra | 具名插槽 |
| default | 默认插槽 |


## 示例

```vue
<template>
  <C7Descriptions />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
