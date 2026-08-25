# C7Cascader

## 用途

C7 级联选择：在 `ElCascader` 上统一静态/整树异步/懒加载、`resultKey`/`dataFormatter`、字段映射、多选 `separator` 与 `valueType`。 **静态**：`dataList` 与 `options` 为别名；**`dataList !== undefined` 时仅用 `dataList`**（与 `C7TreeSelect` 一致）。 **整树异步**：无静态、**非懒加载**、**`autoLoad=true`** 时挂载后 `fetchData({ ...fetchParams })`（无 `parentId`）。 **懒加载**：**`lazy=true`** 且提供 **`fetchData`** 时，通过 EP **`props.lazy` + `props.lazyLoad`** 拉取子层；根层 **`parent

## 导入

```js
import C7Cascader from '@/packages/C7Cascader/index.vue'
```

源码：`quick-ui/src/packages/C7Cascader/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| dataList | Array | undefined} | 为 true 且无静态数据、且提供 `fetchData` 时，使用 EP `props.lazy` + `props.lazyLoad` 按层请求 |
| label | n | - | 见组件注释 / 源码 |
| value | n | - | 见组件注释 / 源码 |
| disabled | n.disabled | - | 见组件注释 / 源码 |
| label | n | - | 见组件注释 / 源码 |
| value | n | - | 见组件注释 / 源码 |
| leaf | n.leaf | - | 见组件注释 / 源码 |
| disabled | n.disabled | - | 见组件注释 / 源码 |
| children | hasChildren | - | 见组件注释 / 源码 |
| loading | loadingInternal | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| update | 见 `@emits` / defineEmits |
| change | 载荷与对外 `modelValue` 一致 |
| load-error | 异步失败，参数为 `err` |
| visible-change | 见 `@emits` / defineEmits |
| loading-change | 内部 `fetchData` 进行中为 true |


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
&lt;template&gt;
  &lt;C7Cascader /&gt;
&lt;/template&gt;
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

> 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
