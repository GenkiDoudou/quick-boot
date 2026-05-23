# 列表页模板（C7JsonTable）

标准业务列表以 **`quick-ui/src/views/system/config/index.vue`** 为蓝本。

## 页面结构

```text
┌─ 查询区（el-form inline） ─────────────────┐
│  字段 + 查询/重置按钮                        │
└────────────────────────────────────────────┘
┌─ 工具栏（C7Button / el-row） ──────────────┐
│  新增、导出、导入等 v-hasPermi               │
└────────────────────────────────────────────┘
┌─ C7JsonTable ──────────────────────────────┐
│  tableColumns + 操作列 slot                  │
│  C7Pagination                                │
└────────────────────────────────────────────┘
┌─ C7Dialog（新增/编辑） ─────────────────────┘
```

## 核心代码模式

```vue
<C7JsonTable
  :loading="loading"
  :data="dataList"
  :table-columns="tableColumns"
  @query="getList"
>
  <template #action="{ row }">
    <el-button v-hasPermi="['system:config:edit']" @click="handleUpdate(row)">修改</el-button>
  </template>
</C7JsonTable>
```

`tableColumns` 示例：

```js
{ prop: 'configName', label: '参数名称', columnType: 'text' },
{ prop: 'configType', label: '系统内置', columnType: 'tag', dictType: 'sys_yes_no' },
```

## API 层

`src/api/system/config.js`：

- `listXxx(query)` → GET `/list`
- `addXxx(data)` → POST `/create`
- `updateXxx(data)` → POST `/update`
- `delXxx(id)` → POST `/remove`

## 新建列表页检查清单

- [ ] 复制 `config/index.vue` 骨架而非从零写 `el-table`
- [ ] `tableColumns` 与字典类型、脱敏字段对齐
- [ ] 权限标识与菜单 `perms`、后端 `@SaCheckPermission` 一致
- [ ] 导出/导入使用 `C7ExcelDownload` / `C7ExcelUpload`（若需要）

## 相关组件文档

- [C7JsonTable](./components/通用组件/c7-json-table)
- [C7JsonTableColumn](./components/通用组件/c7-json-table-column)
- [C7Dialog](./components/通用组件/c7-dialog)
