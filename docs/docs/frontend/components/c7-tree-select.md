# C7TreeSelect

## 用途

C7 树选择：在 `ElTreeSelect` 上统一静态/异步整树加载、字段映射、多选 `separator` 与 `valueType`。 **静态**：`dataList` 与 `options` 为别名；**`dataList !== undefined` 时仅用 `dataList`**（与 `C7Select` 一致）。 **异步**：`autoLoad` 为 true、且无静态 props 绑定时，挂载后 `fetchData({ ...fetchParams })`（**无 `query`**）。 **树字段**：内部将节点规范为 `{ label, value, children }` 供 EP 使用（`mapTree`）。 **多选 + `separator`**：对外 `v-model`/`change` 为逗号字符串，空为 `''`；对内为数组（与 `C7Selec

## 导入

```js
import C7TreeSelect from '@/packages/C7TreeSelect/index.vue'
```

源码：`quick-ui/src/packages/C7TreeSelect/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| dataList | Array | undefined} | `auto`：由规范树根首节点的 `value` 类型推断对外单选标量/多选元素类型 |
| label | n | - | 见组件注释 / 源码 |
| value | n | - | 见组件注释 / 源码 |
| disabled | n.disabled | - | 见组件注释 / 源码 |
| loading | loadingInternal | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| update | 见 `@emits` / defineEmits |
| change | 载荷与对外 `modelValue` 一致 |
| load-error | 异步失败 |
| visible-change | 下拉可见性 |
| loading-change | 请求进行中为 true |


## Slots

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## 示例

```vue
&lt;template&gt;
  &lt;C7TreeSelect /&gt;
&lt;/template&gt;
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

> 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
