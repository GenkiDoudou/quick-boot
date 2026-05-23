# C7Preview 媒体预览

解析逗号分隔 URL，按扩展名展示图片/视频/文件列表，支持 `coverType` 聚合。

**源码**：`quick-ui/src/packages/C7Preview/index.vue`

## 主要 Props

| 属性 | 说明 |
|------|------|
| `urls` | 逗号分隔 URL 字符串 |
| `displayType` | `image` / `video` / `file` |
| `autoDetect` | 按扩展名自动判断 |
| `coverType` | `none` / `button` / `file` |

## 与表格

`C7JsonTableColumn` 的 `columnType: 'image'` 内部使用本组件，`urls` 来自 `row[prop]`。

Dev：`/dev/c7-preview-e2e`
