# C7ExcelUpload Excel 导入

选择文件 → 校验大小/类型 → 调用 `uploadFn` → 成功/失败提示；可选模板下载。

**源码**：`quick-ui/src/packages/C7ExcelUpload/index.vue`

## 主要 Props

| 属性 | 说明 |
|------|------|
| `maxSizeMb` | **必填**，最大 MB |
| `accept` | 默认 `.xls,.xlsx` |
| `uploadFn` | `(file, { duplicateStrategy }) => Promise` |
| `templateDownloadFn` | 下载导入模板 |
| `v-model:duplicateStrategy` | 重复策略 `ignore` / `update` 等 |

## 示例

```vue
<C7ExcelUpload
  :max-size-mb="10"
  :upload-fn="uploadUserFile"
  :template-download-fn="downloadUserTemplate"
  @success="getList"
/>
```

后端对应 `POST .../importData`（multipart）。
