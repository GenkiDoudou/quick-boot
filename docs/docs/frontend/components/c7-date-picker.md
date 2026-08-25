# C7DatePicker

## 用途

C7 日期选择器：基于 **Element Plus 2.10+** 的 **`ElDatePicker`**。 **默认格式**：当父级 **未** 传入 **`format` / `valueFormat`**（含 **`value-format`**）时，按 **`type`** 注入内置映射（见脚本内 **`FORMAT_DEFAULTS`**）；**未命中映射的 `type`** 不注入，行为与裸 **`ElDatePicker`** 一致。 **范围类 `type`**（**`daterange` / `datetimerange` / `monthrange` / `yearrange`**）： - **`rangeMerge`**（默认 **`false`**）：为 **`true`** 时 **`v-model`** 对外为 **单字符串**（两段用 **`mergeDe

## 导入

```js
import C7DatePicker from '@/packages/C7DatePicker/index.vue'
```

源码：`quick-ui/src/packages/C7DatePicker/index.vue`

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
&lt;template&gt;
  &lt;C7DatePicker /&gt;
&lt;/template&gt;
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

> 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
