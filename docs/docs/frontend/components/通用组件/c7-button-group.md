# C7ButtonGroup 按钮组

多个 `C7Button` 的容器，统一间距与对齐，用于工具栏右侧操作区。

**源码**：`quick-ui/src/packages/C7ButtonGroup/index.vue`

## 用法

```vue
<C7ButtonGroup>
  <C7Button btn-type="add" :click-function="handleAdd" />
  <C7Button btn-type="export" :click-function="handleExport" />
</C7ButtonGroup>
```

透传布局属性到外层 `el-space` / flex 容器（以源码为准）。

## 相关

- [C7Button](./c7-button)
- [列表页模板](../../list-page-template)
