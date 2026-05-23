# C7JsonForm JSON 动态表单

根据 **`formColumns`** 配置渲染表单项，用于弹窗内新增/编辑，与 `C7Dialog` 配合。

**源码**：`quick-ui/src/packages/C7JsonForm/`

## 典型用法

```vue
<C7Dialog v-model="open" title="编辑参数">
  <C7JsonForm
    ref="formRef"
    v-model="form"
    :form-columns="formColumns"
    label-width="100px"
  />
</C7Dialog>
```

## formColumns 字段

| 字段 | 说明 |
|------|------|
| `prop` | 绑定 `form[prop]` |
| `label` | 标签 |
| `type` | `input`、`select`、`radio`、`switch`、`date`、`textarea`、`slot` 等 |
| `rules` | Element Plus 校验规则 |
| `span` | 栅格占位 |
| `props` | 透传给底层 EP 组件 |
| `dataList` / `options` | 下拉/单选数据源 |

## 与列表页关系

- **搜索区**：列表页优先用 `C7JsonTable` 内置 `searchColumns`（子集）
- **编辑区**：复杂表单用 `C7JsonForm` 独立维护 `formColumns`

## 相关

- [C7Dialog](./c7-dialog)
- [列表页模板](../../list-page-template)
