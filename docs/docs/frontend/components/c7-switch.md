# C7Switch

## 用途

C7 业务开关：基于 Element Plus `ElSwitch`，用 `beforeChange` 串联确认与可选异步提交；支持 `v-model` 与 `activeValue` / `inactiveValue`，可选 `dictList` 匹配字典文案。

## 导入

```js
import C7Switch from '@/packages/C7Switch/index.vue'
```

源码：`quick-ui/src/packages/C7Switch/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| modelValue | Boolean / String / Number | （必填） | v-model 绑定值 |
| activeValue | Boolean / String / Number | true | 打开态值 |
| inactiveValue | Boolean / String / Number | false | 关闭态值 |
| activeText | String | `''` | 打开态文案 |
| inactiveText | String | `''` | 关闭态文案 |
| dictList | Array | null | 字典项列表，按 value 匹配 label |
| confirmFn | Function | null | 自定义确认（优先于 confirmMessage） |
| confirmMessage | String | `''` | 确认弹窗文案 |
| asyncChange | Function | null | 异步提交 |
| afterChange | Function | null | 变更后回调 |
| beforeChange | Function | null | 变更前钩子，返回 false 可中止 |
| disabled | Boolean | false | 禁用 |
| activeColor | String | `''` | 打开色 |
| inactiveColor | String | `''` | 关闭色 |
| loading | Boolean | false | 加载中 |

## Events

| 事件 | 说明 |
| --- | --- |
| update:modelValue | v-model 更新 |
| change | 成功提交后，载荷 `(newVal, oldVal)` |
| cancel | 用户中止确认或 beforeChange 返回 false |

## Slots

_（无显式具名插槽；透传以源码为准）_

## 示例

```vue
<template>
  <C7Switch v-model="on" confirm-message="确认切换？" />
</template>
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。
