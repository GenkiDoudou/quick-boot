## Why

后台列表页在搜索、表格、工具栏、分页、排序、删除、导出、列显隐等模式上高度重复，手写成本高且易不一致。需要在 `quick-ui` 中提供配置驱动的 **`C7JsonTable`** 一体化列表组件，与既有 **`C7JsonTableColumn`**、**`C7Pagination`**、**`C7ExcelDownload`** 对齐，降低落地成本并统一交互契约。

## What Changes

- 新增 **`C7JsonTable`** 组件（首版单文件 `quick-ui/src/packages/C7JsonTable/index.vue`）：`listFunction` 组装查询参数、**`rowsKey`/`totalKey`** 解析、`searchColumns` 子集搜索区、工具栏（批量删、导出、列设置、刷新）、**`el-table`** + 内置多选/序号 + 默认 **`C7JsonTableColumn`** 或 **`#table-columns`** 覆盖列子树、**`C7Pagination`**、列显隐与 **`columnSettingKey` + localStorage** 持久化。
- 在 **`quick-ui/src/packages/index.js`** 中 **export** 并 **全局注册** `C7JsonTable`。
- （可选）新增 Dev 路由与简易 E2E 页，便于联调；VitePress 组件文档可按项目惯例后续补充，**不**作为本变更阻塞项。

## Capabilities

### New Capabilities

- **`ui-c7-json-table`**：一体化 JSON 配置列表组件的对外契约（props、事件、expose、插槽）、与 **`C7JsonTableColumn`** 的协作边界、搜索列子集、列表/导出/删除/列设置与错误处理要求。

### Modified Capabilities

- （无）主规格 **`openspec/specs/`** 下无与本组件行为冲突的既有 **`ui-c7-json-table`** 条目；本变更为纯新增能力。

## Impact

- **代码**：`quick-ui/src/packages/C7JsonTable/`、`quick-ui/src/packages/index.js`；可能增加 `quick-ui/src/views/dev/` 与 `router` 中的开发页条目。
- **依赖**：Element Plus、既有 **`C7JsonTableColumn`**、**`C7Pagination`**、**`C7ExcelDownload`**（或与其等价的 Blob 下载路径）；**不**新增 npm 包（首版）。
- **依据文档**：`docs/superpowers/specs/2026-05-08-c7-json-table-design.md`、`原始需求/前端/C7JSON表格.md`。
