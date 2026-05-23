# C7Pagination 分页

对 `ElPagination` 的双向绑定封装：`v-model:current-page`、`v-model:page-size`。

**源码**：`quick-ui/src/packages/C7Pagination/index.vue`

## 主要 Props

| 属性 | 说明 |
|------|------|
| `currentPage` | 当前页（从 1 开始） |
| `pageSize` | 每页条数 |
| `total` | 总条数 |
| `pageSizes` | 可选 `[10, 20, 50, 100]` |
| `layout` | EP 布局字符串 |

## 与 C7JsonTable

`C7JsonTable` 内置分页；独立使用时：

```vue
<C7Pagination
  v-model:current-page="query.pageNum"
  v-model:page-size="query.pageSize"
  :total="total"
  @pagination="getList"
/>
```
