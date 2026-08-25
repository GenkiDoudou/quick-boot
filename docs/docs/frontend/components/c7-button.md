# C7Button

## 用途

C7 业务按钮：在 ElButton 上封装「校验 → 确认 → 异步执行 → 成功判定 → 提示 → 回调」固定流水线。 约定（与 axios 封装一致）：{

## 导入

```js
import C7Button from '@/packages/C7Button/index.vue'
```

源码：`quick-ui/src/packages/C7Button/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| btnType | String | '' | 见组件注释 / 源码 |
| clickFunction | Function | - | 见组件注释 / 源码 |
| debounceDelay | Number | 300 | 见组件注释 / 源码 |
| confirm | Boolean | false | 见组件注释 / 源码 |
| confirmMessage | String | '确定执行该操作？' | 见组件注释 / 源码 |
| confirmFn | Function | null | 见组件注释 / 源码 |
| validate | Boolean | false | 见组件注释 / 源码 |
| validateRef | Object | null | 见组件注释 / 源码 |
| beforeClick | Function | null | 见组件注释 / 源码 |
| beforePipeline | Function | null | 见组件注释 / 源码 |
| checkSuccess | Function | () =&gt; true | 见组件注释 / 源码 |
| successMessage | String | '' | 见组件注释 / 源码 |
| successNotify | Boolean | false | 见组件注释 / 源码 |
| showErrorToast | Boolean | true | 见组件注释 / 源码 |
| errorMessage | String | '' | 见组件注释 / 源码 |
| label | String | '' | 见组件注释 / 源码 |
| plain | Boolean | undefined | 见组件注释 / 源码 |
| size | String | '' | 见组件注释 / 源码 |
| disabled | Boolean | false | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| before-click | 流水线开始（校验前） |
| success | 业务成功，载荷为 &#123;@code clickFunction} resolve 值 |
| error | 校验失败、用户取消确认、请求 reject、或 &#123;@code checkSuccess} 为 false，载荷为 Error 或其它 reason |
| after-click | 流水线结束，载荷为是否整体成功（确认取消 / 校验失败 / veto / 异常 / 业务失败均为 false） |


## Slots

| 插槽 | 说明 |
| --- | --- |
| default | 默认插槽 |


## 示例

```vue
<template>
  <C7Button />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

&gt; 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
