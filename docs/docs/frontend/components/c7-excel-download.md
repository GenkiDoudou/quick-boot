# C7ExcelDownload

## 用途

C7 导出下载按钮：执行 **`downloadFn`** 取 **Blob**（或 **`{ data, headers }`**）， 按优先级解析文件名后通过 **`objectURL` + `<a download>`** 触发浏览器下载； 管理 **`v-model:downloading`**；**JSON 错误 Blob** 与 **`download()`** 对齐提示。 **与 `request` 配合**：需要 **`Content-Disposition`** 时，请使用 **`downloadRequest(url, params, { returnBlobWithHeaders: true })`**， 在 **`downloadFn`** 中 **`return`** 该 Promise 结果（或自行组装 **`{ data, headers }`**）。

## 导入

```js
import C7ExcelDownload from '@/packages/C7ExcelDownload/index.vue'
```

源码：`quick-ui/src/packages/C7ExcelDownload/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| downloadFn | Function | undefined} | 非空时优先作为保存文件名，  不再  解析  `Content-Disposition` |


## Events

| 事件 | 说明 |
| --- | --- |
| success | 见 `@emits` / defineEmits |
| error | 见 `@emits` / defineEmits |


## Slots

| 插槽 | 说明 |
| --- | --- |
| default | 默认插槽 |


## 示例

```vue
<template>
  <C7ExcelDownload />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
