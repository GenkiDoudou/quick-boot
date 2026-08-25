# C7DictTag

## 用途

C7 字典标签：只读将 **`modelValue`**（单值 / 多值 / 逗号串）按 **`options`** 匹配为 **`ElTag`** 列表。 **解析**：**`null`/`undefined`** 与解析后无非空原子 → 展示 **`-`**；**`number`** 为单原子；**`array`** 按序不去重；**`string`** 若 **包含 `separator`** 则按分隔符拆分（各段 **trim**、去空），否则整段为单原子。 **匹配**：对每个原子 **`val`**，取 **`options`** 中 **首个** 满足 **`String(opt.value) === String(val)`** 的项的 **`label`**。 **`mk` 与 `max`**：仅对 **已匹配** 原子自 **1** 递增编号 **`mk`**。当 **

## 导入

```js
import C7DictTag from '@/packages/C7DictTag/index.vue'
```

源码：`quick-ui/src/packages/C7DictTag/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| modelValue | Array | undefined} | 字典行：`{ label, value }`；匹配首项 |
| overflowLabels | overflowLabels.value.slice | - | 见组件注释 / 源码 |


## Events

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <C7DictTag />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
