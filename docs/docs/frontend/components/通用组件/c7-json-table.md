# C7JsonTable JSON 动态表格

一体化列表组件：搜索区 + 工具栏 + 表格 + 分页 + 列设置（localStorage）+ 导出/批量删除。

**源码**：`quick-ui/src/packages/C7JsonTable/index.vue`  
**模板参考**：`views/system/config/index.vue`

## 核心 Props

| 属性 | 说明 |
|------|------|
| `listFunction` | `(params) => Promise`，返回 `{ rows, total }` 或项目约定分页结构 |
| `tableColumns` | 列描述数组，交给内置 `C7JsonTableColumn` |
| `searchColumns` | 搜索区配置（`input`/`select`/`date`/`daterange`/`slot`） |
| `defaultSearchParam` | 搜索初始值，重置时恢复 |
| `showSelection` / `showIndex` | 多选列、序号列 |
| `pagination` | 是否显示 `C7Pagination` |

## 搜索区 searchColumns

```js
{ prop: 'userName', label: '用户名称', type: 'input', span: 6 },
{ prop: 'status', label: '状态', type: 'select', dataList: statusOptions },
```

支持 `v-model:searchParam` / `@update:searchParam` 与父组件同步（导出时用同一快照）。

## 列配置 tableColumns

与 [C7JsonTableColumn](./c7-json-table-column) 一致，支持 `columnType: text | tag | image | link | slot`。

## 插槽

| 插槽 | 说明 |
|------|------|
| `#toolbar-left` / `#toolbar-right` | 工具栏按钮区 |
| `#action` | 行操作列（需在 columns 中声明 slot 列） |
| `#table-columns` | 完全自定义列（替换默认 C7JsonTableColumn） |
| `search-extra` | 搜索区额外按钮 |

## 事件与 expose

- `@query`：查询触发
- `expose`：`refreshData`、`getDataList`、`selectedRows` 等（以组件 `defineExpose` 为准）

## Dev 演示

菜单「组件演示 → C7JsonTable」或路由 `/dev/c7-json-table-e2e`（需登录后动态菜单）。

## 相关

- [列表页模板](../../list-page-template)
- 设计说明：仓库 `docs/superpowers/specs/2026-05-08-c7-json-table-design.md`
