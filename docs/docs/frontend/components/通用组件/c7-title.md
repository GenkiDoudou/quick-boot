# C7Title 标题

区块标题：字号层级、底部分割线、左侧图标、右侧操作插槽。

**源码**：`quick-ui/src/packages/C7Title/index.vue`

## 主要 Props

| 属性 | 说明 |
|------|------|
| `label` / `title` | 主标题文案 |
| `tag` | 语义标签 `h1`~`h6`、`div`、`p` |
| `level` | 预设层级或自定义 `20px` |
| 默认插槽 | 右侧操作按钮 |

## 示例

```vue
<C7Title label="用户列表">
  <C7Button btn-type="add" :click-function="handleAdd" />
</C7Title>
```
