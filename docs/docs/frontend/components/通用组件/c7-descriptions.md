# C7Descriptions 描述列表

配置驱动 `ElDescriptions`，支持 `text` / `tag` 等列类型，用于详情页。

**源码**：`quick-ui/src/packages/C7Descriptions/index.vue`

## 主要 Props

| 属性 | 说明 |
|------|------|
| `data` | 详情对象 |
| `items` | 列配置（`prop`、`label`、`columnType`） |
| `defaultEmptyText` | 空值展示，默认「暂无」 |

## 示例

```vue
<C7Descriptions :data="detail" :items="descItems" />
```

`columnType: 'tag'` 时配合 `dictList` / `options` 使用 `C7DictTag` 渲染。
