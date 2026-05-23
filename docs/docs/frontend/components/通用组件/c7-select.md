# C7Select 下拉选择

增强 `ElSelect`：静态/远程 `fetchData`、`resultKey`、选项整形。

**源码**：`quick-ui/src/packages/C7Select/index.vue`  
**注册名**：`C7Select`

## 主要 Props

| 属性 | 说明 |
|------|------|
| `dataList` / `options` | 静态选项，`dataList` 优先 |
| `fetchData` | 远程搜索/加载 |
| `fetchParams` | 合并进请求参数 |
| `resultKey` | 如 `data.records` |

## 示例

```vue
<C7Select
  v-model="query.status"
  :data-list="statusOptions"
  clearable
  style="width: 100%"
/>
```

`C7JsonTable` 搜索区 `type: 'select'` 内部使用本组件。
