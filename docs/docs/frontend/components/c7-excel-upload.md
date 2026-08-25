# C7ExcelUpload

## 用途

C7 Excel 同步导入：拖拽/点选 xls/xlsx，调用 `uploadFn(file, strategy)` 完成导入； 无导入导出中心依赖；可选模板下载与失败明细下载。

## 导入

```js
import C7ExcelUpload from '@/packages/C7ExcelUpload/index.vue'
```

源码：`quick-ui/src/packages/C7ExcelUpload/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| accept | String | '.xls | 见组件注释 / 源码 |
| mode | mapped.mode | - | 见组件注释 / 源码 |
| total | Number | - | 见组件注释 / 源码 |
| successCount | Number | - | 见组件注释 / 源码 |
| failCount | Number | - | 见组件注释 / 源码 |
| errorFileName | mapped.errorFileName | - | 见组件注释 / 源码 |
| errorFileBase64 | mapped.errorFileBase64 | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| success | 导入完成，载荷为后端返回的结果对象 |
| error | 导入失败 |
| cancel | 用户点击取消 |


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <C7ExcelUpload />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
