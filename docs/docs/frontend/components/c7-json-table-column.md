# C7JsonTableColumn

## 用途

C7 表格列渲染器：根据 `columns` 配置生成 `el-table-column`，内置 text/tag/image/link/slot 等列类型， 与 {

## 导入

```js
import C7JsonTableColumn from '@/packages/C7JsonTableColumn/index.vue'
```

源码：`quick-ui/src/packages/C7JsonTableColumn/index.vue`

## Props

| 属性 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| columns | Array | undefined} | 表级默认空文案：用于  `columnType==='text'`  且无  `formatter`  时， 当列上未配置  `emptyText`  且单元格值为  `null` / `undefined` / ''`  的展示兜底（链式：`列 emptyText` → 本 prop →  `'-'`  ）。 |


## Events

_（源码未声明显式项；可能透传底层组件属性，见源码）_


## Slots

| 插槽 | 说明 |
| --- | --- |
| default | 默认插槽 |


## 示例

```vue
&lt;template&gt;
  &lt;C7JsonTableColumn /&gt;
&lt;/template&gt;
```

更多用法见 `quick-ui/src/views/dev` 下对应 E2E 页（若有）。

> 未在上表列出的透传属性：以 Element Plus / 源码 `v-bind` 为准，禁止臆造。
