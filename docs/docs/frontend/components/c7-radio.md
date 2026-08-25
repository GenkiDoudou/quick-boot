# C7Radio

## 用途

C7 业务单选组：在 `ElRadioGroup` 上统一 **静态 `dataList` / `options`** 与 **`fetchData` + `fetchParams`** 异步字典、**`response.data` → `resultKey` → `dataFormatter`** 解析链，以及 **`labelKey` / `valueKey`** 行映射。 **与 `C7Select` 对齐**：`dataList` 与 `options` 为别名，**同时存在时 `dataList` 优先**；`fetchData(mergedParams)` **不注入 `query`**。 **与 `C7Select` 不同**：**`autoLoad` 默认 `true`**（单选无「展开再拉」，首屏尽早出选项）。 **根级透传**：除 `RESERVED_ATTR_KEYS

## 导入

```js
import C7Radio from '@/packages/C7Radio/index.vue'
```

源码：`quick-ui/src/packages/C7Radio/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| dataList | Array | undefined} | `dataList` 别名；  `dataList !== undefined` 时仅用 `dataList` |
| fetchErrorBehavior | String | 'keep-last' | 见组件注释 / 源码 |
| invalidModelBehavior | String | 'keep' | 见组件注释 / 源码 |
| emptyDisplay | String | 'none' | 见组件注释 / 源码 |
| emptyText | String | ''} | 为 true 时不输出「当前值不在选项中」的开发期 `console.warn` |
| loading | loadingInternal | - | 见组件注释 / 源码 |


## Events

| 事件 | 说明 |
| --- | --- |
| update | 见 `@emits` / defineEmits |
| change | 与 `v-model` 值形态一致 |
| loading-change | `fetchData` 并发计数 >0 为 true |


## Slots

| 插槽 | 说明 |
| --- | --- |
| default | 默认插槽 |


## 示例

```vue
&lt;template&gt;
  &lt;C7Radio /&gt;
&lt;/template&gt;
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

> 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
