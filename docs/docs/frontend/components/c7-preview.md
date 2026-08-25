# C7Preview

## 用途

附件预览：逗号分隔 `urls`，支持 **图片大图 / 视频弹窗 / 文件新窗口**。 **钩子**：**`onPreview`** 返回 **`false` 或 reject** 时 **不**打开预览且 **不** **`emit('preview')`**；通过时 **先** **`emit('preview')`** 再打开。

## 导入

```js
import C7Preview from '@/packages/C7Preview/index.vue'
```

源码：`quick-ui/src/packages/C7Preview/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| urls | String | ''} | `autoDetect=false` 时整条列表按此类型渲染 |
| autoDetect | Boolean | true} | `none` 平铺、`button` 聚合、`file` 表格 |
| width | Number | undefined} | 封面高度（px） |
| __index | index | - | 见组件注释 / 源码 |
| name | fileBasename | - | 见组件注释 / 源码 |
| kind | kindAtIndex | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| preview | — `(url: string, index: number)` |
| close | — 视频弹窗关闭且已  pause + 进度归零  后触发一次 |


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <C7Preview />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
