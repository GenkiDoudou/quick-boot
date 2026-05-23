# C7Card 卡片

带标题色块、加粗标题的 `ElCard` 封装，用于表单分区或详情块。

**源码**：`quick-ui/src/packages/C7Card/index.vue`

## 主要 Props

| 属性 | 说明 |
|------|------|
| `label` | 标题文案 |
| `textSize` | `h1`~`h5`，对应标题层级 |
| `isBold` | 标题加粗 |
| `showColorBlock` | 左侧色条开关 |

## 示例

```vue
<C7Card label="基本信息" text-size="h4" :show-color-block="true">
  <el-form>...</el-form>
</C7Card>
```

## 相关

- [C7Title](./c7-title)
