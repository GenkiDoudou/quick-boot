# C7Upload

## 用途

C7 文件上传：基于 `ElUpload` 拖拽上传，按 `classify` 拉取分类规则（数量/大小/扩展名/压缩）， 可选图片压缩后调用 `/file/upload`；`v-model:results` 绑定已成功上传的结果列表。

## 导入

```js
import C7Upload from '@/packages/C7Upload/index.vue'
```

源码：`quick-ui/src/packages/C7Upload/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| classify | String | true } | 选中文件后是否立即上传 |
| file | item.raw | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| update | 见 `@emits` / defineEmits |
| success | 单文件上传成功 |
| error | 上传或加载分类规则失败 |
| change | 文件列表或结果变更 |


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <C7Upload />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
