# C7Checkbox 复选框组

多选组，支持静态 `dataList` 或 `fetchData` 异步选项。

**源码**：`quick-ui/src/packages/C7Checkbox/index.vue`

## 主要 Props

| 属性 | 说明 |
|------|------|
| `dataList` | 静态选项 `{ label, value }[]` |
| `fetchData` | `(params) => Promise` 异步加载 |
| `resultKey` | 从响应 `data` 取列表路径 |
| `v-model` | 选中值数组 |

## 示例

```vue
<C7Checkbox v-model="form.roleIds" :data-list="roleOptions" />
```

## 相关

- [C7Radio](./c7-radio)（同目录 `C7Radio`，侧栏未单独列出）
- [C7Select](./c7-select)
