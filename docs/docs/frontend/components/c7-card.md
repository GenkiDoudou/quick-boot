# C7Card

## 用途

C7 业务卡片：基于 **`ElCard`** 统一 **标题栏**（可选色块 + **`ElText` 语义标签 h1~h5** + 加粗）、**`extra` / 折叠** 与 **内容区 fade**。 **`#header`**：若提供，则 **整块 `ElCard` 头部** 仅渲染该插槽，**不**再出现默认色块、标题、`extra`、内置折叠按钮（折叠需在插槽内自行处理）。 **受控 / 非受控**：传入 **`modelValue`**（`undefined` 以外）时为受控，展开态以 **`modelValue`** 为准；否则以内部状态为准，初值来自 **`defaultExpanded`（默认 `true`）**。 **`toggle` / `expand` / `collapse`**：在受控模式下通过 **`emit('update:modelValue', …)

## 导入

```js
import C7Card from '@/packages/C7Card/index.vue'
```

源码：`quick-ui/src/packages/C7Card/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| label | String | ''} | 标题层级，对应  `ElText` 的 `tag`  ，取 `h1`~`h5` |
| isBold | Boolean | false} | 是否显示左侧色块（主开关）。 若显式传入（含 `false`），  仅  以此为准。 |


## Events

| 事件 | 说明 |
| --- | --- |
| update | 见 `@emits` / defineEmits |
| change | 见 `@emits` / defineEmits |


## Slots

| 插槽 | 说明 |
| --- | --- |
| header | 具名插槽 |
| extra | 具名插槽 |
| toggle | 具名插槽 |
| default | 默认插槽 |


## 示例

```vue
&lt;template&gt;
  &lt;C7Card /&gt;
&lt;/template&gt;
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

> 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
