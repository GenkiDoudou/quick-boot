# C7Watermark

## 用途

页面/全屏水印：Canvas 生成平铺图，`image` 优先，失败回落 `text`。 **防删**：`tamperResistant === true` 开启 MutationObserver；若仅传 `editable=false` 亦开启。 若 **`tamperResistant` 显式传入**（含 `false`），**以其为准**，覆盖 `editable`。 **`fullscreenScope`**：仅当 `fullscreen=true` 时生效；`false` 时忽略（见 props 说明）。

## 导入

```js
import C7Watermark from '@/packages/C7Watermark/index.vue'
```

源码：`quick-ui/src/packages/C7Watermark/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| text | String | '' } | 图片 URL；优先于 `text`，加载或绘制失败回落文本 |
| zIndex | props.zIndex | - | 见组件注释 / 源码 |
| text | props.text | - | 见组件注释 / 源码 |
| fontSize | props.fontSize | - | 见组件注释 / 源码 |
| fontColor | props.fontColor | - | 见组件注释 / 源码 |
| fontFamily | props.fontFamily | - | 见组件注释 / 源码 |
| opacity | props.opacity | - | 见组件注释 / 源码 |
| rotate | props.rotate | - | 见组件注释 / 源码 |
| gapX | props.gapX | - | 见组件注释 / 源码 |
| gapY | props.gapY | - | 见组件注释 / 源码 |
| width | props.width | - | 见组件注释 / 源码 |
| height | props.height | - | 见组件注释 / 源码 |
| offsetX | props.offsetX | - | 见组件注释 / 源码 |
| offsetY | props.offsetY | - | 见组件注释 / 源码 |
| dataUrl | built.dataUrl | - | 见组件注释 / 源码 |
| tileW | built.tileWidth | - | 见组件注释 / 源码 |
| tileH | built.tileHeight | - | 见组件注释 / 源码 |


## Events

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Slots

| 插槽 | 说明 |
| --- | --- |
| default | 默认插槽 |


## 示例

```vue
<template>
  <C7Watermark />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
