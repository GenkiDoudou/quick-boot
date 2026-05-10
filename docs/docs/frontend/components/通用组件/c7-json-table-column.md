# C7JsonTableColumn JSON 表格列

根据列描述数组 **`columns`** 在 **`ElTable`** 内批量生成 **`ElTableColumn`**，内置 **`columnType`**（**`text` / `tag` / `image` / `link` / `slot`**）与列头 **`#header-${prop}`** 转发，减少列表页重复渲染逻辑。与 **C7JsonTable** 的 **`tableColumns[]`** 方向一致，可单独用于任意表格，也可由 JsonTable 内部复用。

**源码**：`quick-ui/src/packages/C7JsonTableColumn/index.vue`  
**Dev 页**：开发服务器启动后访问 `/dev/c7-json-table-column-e2e`

## Props

| 属性 | 类型 | 说明 |
|------|------|------|
| **`columns`** | `Array` | **必填**。列描述对象列表；非数组时不渲染列，开发环境 **`console.warn`**。 |
| **`emptyText`** | `string` | 可选。表级默认空文案：用于 **`text`** 且无 **`formatter`** 时，列上未配置 **`emptyText`** 且单元格值为 **`null` / `undefined` / ''`** 时的兜底（链式：**列 `emptyText`** → **本属性** → **`'-'`**）。 |

## 列描述对象要点

- **过滤**：仅 **`visible !== false`** 的列参与渲染。
- **排序**：**`order` 数值升序**；**未设置 `order`** 的列排在后面；**`order` 相同**时保持 **`columns` 数组中的原始相对顺序**。
- **透传 `el-table-column`**：**`prop`、`label`、`width`、`minWidth`、`fixed`、`align`、`headerAlign`、`sortable`、`showOverflowTooltip`**。列对象上的 **`props`** 为「其余列级属性」对象，以 **`v-bind` 合并**；模板中显式绑定的上述字段**后写**，故与同名字段冲突时**以列对象顶层为准**。
- **`columnType`**：默认 **`text`**；未知取值按 **`text`** 处理并 **dev warn**。
- **`text`**：若存在 **`formatter`**，签名与 EP 一致 **`(row, column, cellValue, index) => string`**，**直接交给 `el-table-column`**，组件**不会**对返回值套 **`emptyText`**。无 **`formatter`** 时按 **`prop`** 取值，空值见上 **`emptyText`** 链。
- **`tag`**：**`C7DictTag`**；**`options ?? dictList`**（**`options` 优先**）；缺 **`prop`** 则 dev warn 并跳过该列。
- **`image`**：**`C7Preview`**；**`urls`** 为 **`row[prop]`** 字符串（逗号分隔同 C7Preview）；**`coverType` 固定为 `none`**；缩略尺寸等通过 **C7Preview** 已有 props 在列上配置。
- **`link`**：**`<a>`**；**`linkHref` / `linkText`** 可为字符串或函数；**`linkTarget`** 仅字符串；解析后 **`href`** 为空则不渲染链接，单元格为 **`-`**。
- **`slot`**：具名 **`slotName || prop`**；作用域与 **`el-table-column` #default** 一致（**`row`、`column`、`$index`**）。父级未提供对应插槽时单元格为 **`-`**。
- **列头**：仅当列有 **`prop`** 时支持 **`#header-${prop}`**（转发 EP **`#header`** 作用域）。

## 与 C7JsonTable 的关系

- **C7JsonTableColumn** 只负责「列 → **`ElTableColumn`**」这一层；**不负责**数据请求、分页、查询区、列持久化等。
- **C7JsonTable**（若已存在）可将同一套列配置委托给本组件，以保持列渲染行为一致。

## 父组件示例

```vue
<el-table :data="rows">
  <C7JsonTableColumn :columns="columns" empty-text="—">
    <template #header-username="{ column }">自定义：{{ column.label }}</template>
    <template #action="{ row, $index }">
      <el-button size="small">编辑 {{ row.name }}</el-button>
    </template>
  </C7JsonTableColumn>
</el-table>
```

## 相关规格

OpenSpec 变更：`openspec/changes/ui-c7-json-table-column/specs/ui-c7-json-table-column/spec.md`
