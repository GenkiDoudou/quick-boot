## Why

各业务页在 Excel 导入场景下重复实现文件选择、**`.xls/.xlsx`** 与大小校验、重复数据策略（覆盖/忽略）、上传进行中状态与导入结果展示，行为不一致且难维护。需要在 **quick-ui** 提供统一的 **`C7ExcelUpload`** 组件，与已定稿的 **`docs/superpowers/specs/2026-05-08-c7-excel-upload-design.md`**、**`原始需求/前端/C7Excel导入.md`** 对齐。

## What Changes

- 新增 **`C7ExcelUpload`**（**`quick-ui/src/packages/C7ExcelUpload/index.vue`**）：隐藏 **`input[type=file]`**、**`accept`** 默认 **`.xls,.xlsx`**、**`maxSizeMb`** 校验、**`v-model:duplicateStrategy`**（**`overwrite` | `ignore`**）、可配置策略文案；点击导入调用业务 **`uploadFn(file, strategy)`**；展示 **`total/successCount/failCount`**；**`failCount>0` 且 `errorFileUrl` 非空** 时展示错误明细链接（**`<a href>`**，不强制 Blob 链）；**`v-model:uploading`**、**`defineExpose({ uploading, reset })`**；**`success` / `error`** 事件；可注入 **`notify`**，默认 **`ElMessage`**；**校验失败仅 `notify`，不 `emit('error')`**；**`uploadFn` reject 时 `notify` + `emit('error')`**。
- **`quick-ui/src/packages/index.js`**：**导出**并 **`installPackages`** 注册 **`C7ExcelUpload`**。
- **非目标**：不内置 **`ElDialog` / `ElCard`**；不在组件内拼装 **`FormData`** 或解析后端 **`R`** 包装；不强制复用 **`C7ExcelDownload`** 处理 **`errorFileUrl`**。

## Capabilities

### New Capabilities

- **`ui-c7-excel-upload`**：**`C7ExcelUpload`** 的 **`uploadFn` / 结果类型**、文件与大小校验、策略双向绑定、**`uploading` 与防重复**、**`reset`** 语义、结果区与 **`errorFileUrl`** 入口、**`notify` / 事件** 边界及验收要求。

### Modified Capabilities

- （无）主规格库中尚无 **`ui-c7-excel-upload`**；本变更为新增能力。

## Impact

- **代码**：新增 **`C7ExcelUpload/index.vue`**；修改 **`packages/index.js`**。
- **依赖**：以现有 **`element-plus`**、**`vue`** 为准；**不**新增 npm 包（除非实现评审发现硬性缺口）。
- **文档**：本变更 **`proposal` / `design` / `tasks` / `specs/ui-c7-excel-upload/spec.md`**；设计已定稿于 **`docs/superpowers/specs/2026-05-08-c7-excel-upload-design.md`**。
