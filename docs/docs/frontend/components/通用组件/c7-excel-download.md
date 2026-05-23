# C7ExcelDownload Excel 下载

按钮触发导出：调用 `downloadFn` 返回 `Blob`，自动解析文件名并 `file-saver` 保存。

**源码**：`quick-ui/src/packages/C7ExcelDownload/index.vue`

## 主要 Props

| 属性 | 说明 |
|------|------|
| `downloadFn` | `() => Promise<Blob \| { data, headers }>` |
| `fileName` | 固定文件名（优先） |
| `defaultFileName` | 兜底文件名 |
| `notify` | 自定义提示回调 |

## 示例

```vue
<C7ExcelDownload
  :download-fn="() => exportConfig(query)"
  file-name="参数配置.xlsx"
/>
```

通常与列表页工具栏、`v-hasPermi="['system:xxx:export']"` 配合。

## 相关

- [C7ExcelUpload](./c7-excel-upload)
