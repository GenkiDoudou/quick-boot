# 文件上传说明

当前 **C7 组件库未提供独立的 `C7Upload` 组件**（`packages/index.js` 未注册）。

## 推荐做法

| 场景 | 方案 |
|------|------|
| Excel 导入 | [C7ExcelUpload](./c7-excel-upload) |
| 通用附件 | Element Plus `el-upload` + `utils/request.js` |
| 图片/附件预览 | `C7Preview`（`packages/C7Preview`） |

后端文件能力见 [文件上传模块（后端）](../../../backend/components/通用组件/文件上传模块使用文档)。

侧栏「C7Upload」条目保留名称兼容，实际请使用上表方案。
