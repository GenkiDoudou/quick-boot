# C7ButtonGroup

## 用途

C7 按钮组：在 {

## 导入

```js
import C7ButtonGroup from '@/packages/C7ButtonGroup/index.vue'
```

源码：`quick-ui/src/packages/C7ButtonGroup/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| buttons | Array | () =&gt; [] | 见组件注释 / 源码 |
| mode | String | 'auto' | 见组件注释 / 源码 |
| maxVisible | Number | 2 | 见组件注释 / 源码 |
| spacing | 见源码 | 'md' | 见组件注释 / 源码 |
| size | String | '' | 见组件注释 / 源码 |
| responsive | Boolean | false | 见组件注释 / 源码 |
| moreText | String | '更多' | 见组件注释 / 源码 |
| moreIcon | 见源码 | null | 见组件注释 / 源码 |
| moreButtonType | String | 'default' | 见组件注释 / 源码 |
| moreButtonPlain | Boolean | false | 见组件注释 / 源码 |
| trigger | String | 'click' | 见组件注释 / 源码 |
| props | Array | () =&gt; [] | 见组件注释 / 源码 |
| gap | gapCss.value | - | 见组件注释 / 源码 |
| gap | gapCss.value | - | 见组件注释 / 源码 |
| gap | gapCss.value | - | 见组件注释 / 源码 |
| size | mergedSize | - | 见组件注释 / 源码 |
| beforePipeline | async | - | 见组件注释 / 源码 |
| key | ent.row.key | - | 见组件注释 / 源码 |
| index | ent.idx | - | 见组件注释 / 源码 |
| raw | ent.row | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| before-command | 进入子按钮流水线之前，载荷为 data 项描述或 &#123;@code { slotIndex &#125;&#125; |
| after-command | 子按钮流水线结束后，载荷为 &#123;@code { item, success &#125;&#125; |


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <C7ButtonGroup />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
