## Why

导出接口普遍返回 **Blob**，文件名常由响应头 **`Content-Disposition`** 提供；各页自行拼 **`objectURL`**、解析文件名与错误提示时行为不一致，且与现有 **`downloadRequest`** 仅返回 **`Blob`** 的组合无法稳定消费响应头。需要在 **quick-ui** 提供统一的 **`C7ExcelDownload`** 按钮组件，并与 **`request.js`** 的可选「带 headers」返回能力配合。动机与边界对齐 **`docs/superpowers/specs/2026-05-08-c7-excel-download-design.md`** 与 **`原始需求/前端/C7Excel下载.md`**。

## What Changes

- 新增 **`C7ExcelDownload`**（**`quick-ui/src/packages/C7ExcelDownload/index.vue`**）：内置 **`ElButton`**，点击执行 **`downloadFn`**；按约定优先级解析文件名；**`objectURL` + `<a download>`** 触发下载并 **`revokeObjectURL`**；**`v-model:downloading`**；**`success` / `error`** 事件；默认 **`ElMessage`** 错误提示，可注入 **`notify`**。
- **`quick-ui/src/utils/request.js`**：在**不破坏**现有 **`downloadRequest`** 默认仅返回 **`Blob`** 的前提下，增加调用方可选拿到 **`{ data: Blob, headers }`** 的能力（**`config` 开关** 或 **`downloadRequestWithHeaders`** 之一，以 design/tasks 为准）。
- **`quick-ui/src/packages/index.js`**：**导出**并 **`installPackages`** 注册 **`C7ExcelDownload`**。
- **非目标**：不强制下线全局 **`download()` / `$download`**；不限定仅 Excel 扩展名（组件名为 C7 系列命名）；不改动后端导出接口契约。

## Capabilities

### New Capabilities

- **`ui-c7-excel-download`**：**`C7ExcelDownload`** 的 **`downloadFn` 返回值归一**、文件名解析优先级（**`fileName` → `filename*` → `filename=` → `defaultFileName`**）、**`application/json` Blob** 与 **`blobValidate`** 等价处理、**`downloading`** 与重复点击 **no-op**、**`notify` / `ElMessage`**、事件 **`success` / `error`**、与 **`request.js`** 带 **headers** 返回的配合及验收要求。

### Modified Capabilities

- （无）主规格库中尚无 **`ui-c7-excel-download`**；**`request.js`** 行为为向后兼容扩展，需求契约落在本变更 **`ui-c7-excel-download`** 规格中说明对调用方的约定，不单独列为「修改既有主 spec」项。

## Impact

- **代码**：新增 **`C7ExcelDownload`**；修改 **`request.js`**（blob 路径可选返回形态）；修改 **`packages/index.js`**。
- **依赖**：以现有 **`element-plus`**、**`axios`** 为准；**不**新增 npm 包（除非实现时发现硬性缺口，再于 tasks 中说明）。
- **文档**：本变更目录 **`proposal` / `design` / `tasks` / `specs/ui-c7-excel-download/spec.md`**；设计已定稿于 **`docs/superpowers/specs/2026-05-08-c7-excel-download-design.md`**。
