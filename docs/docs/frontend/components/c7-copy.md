# C7Copy

## 用途

C7 复制：统一纯文本写入剪贴板（Clipboard API 优先，`execCommand('copy')` 降级）、提示与事件。 **安全上下文**：仅在 `window.isSecureContext` 且存在 `navigator.clipboard.writeText` 时走 Clipboard； 否则直接降级。`writeText` reject 时继续尝试降级。 **重入**：一次复制流程（含 `getCopyText` 的 Promise）未完成前再次触发会被忽略。 **disabled**：不调用 `beforeCopy`、不写剪贴板、不 emit、不提示（与 design 一致）。

## 导入

```js
import C7Copy from '@/packages/C7Copy/index.vue'
```

源码：`quick-ui/src/packages/C7Copy/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| text | Function | undefined} | 基于规范化后的 `text` 字符串生成最终写入内容；可返回 Promise。 返回值非字符串时会 `String(...)`；`null`/`undefined` 结果视为 `''`。 |
| size | attrs.size | - | 见组件注释 / 源码 |
| link | true | - | 见组件注释 / 源码 |
| size | attrs.size | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| copy | 见 `@emits` / defineEmits |
| success | 见 `@emits` / defineEmits |
| error | 见 `@emits` / defineEmits |


## Slots

| 插槽 | 说明 |
| --- | --- |
| default | 默认插槽 |


## 示例

```vue
<template>
  <C7Copy />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
