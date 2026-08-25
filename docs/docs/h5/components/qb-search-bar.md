# QbSearchBar

## 用途

列表页通用搜索栏：白底圆角容器 + 搜索 + 可选主色操作按钮。

## 导入

```js
import QbSearchBar from '@/components/qb/QbSearchBar.vue'
```

源码：`quick-h5/src/components/qb/QbSearchBar.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| modelValue | string | - | 见源码 |
| placeholder | string | - | 见源码 |
| addText | string | - | 见源码 |
| showAdd | boolean | - | 见源码 |


## Events

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
<template>
  <QbSearchBar />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
