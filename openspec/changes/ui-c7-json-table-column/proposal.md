## Why

列表页中字典标签、图片预览、链接、具名 slot 等列渲染在多处重复手写，与 **C7JsonTable** 的 **`tableColumns[]`** 方向不一致。需要 **`C7JsonTableColumn`** 按列描述统一生成 **`ElTableColumn`**，降低重复并在任意 **`ElTable`** 中可复用。

## What Changes

- 新增 **`C7JsonTableColumn`** 组件（**`quick-ui/src/packages/C7JsonTableColumn/index.vue`**）：根据 **`columns`** 生成多个 **`el-table-column`**（Vue 3 Fragment 多根）；支持 **`columnType`**（**`text` / `tag` / `image` / `link` / `slot`**）、列过滤与 **`order` 稳定排序**、常用 EP 列属性透传及 **`props`（列级其余属性）** 合并绑定。
- 支持表级 **`emptyText`**、列头 **`#header-${prop}`** 转发、**`slot`** 列单元格具名插槽与 **`el-table-column` #default** 作用域一致。
- 在 **`quick-ui/src/packages/index.js`** 中 **export** 并纳入 **`installPackages`** 全局注册。
- 新增 E2E 演示页（路由）覆盖关键路径；可选后续抽出 **`normalizeColumns.ts`** 时补单测。
- VitePress 文档：与其它 C7 通用组件同目录增加 **C7JsonTableColumn** 说明。

## Capabilities

### New Capabilities

- `ui-c7-json-table-column`：JSON/配置驱动的表格列组件契约（**Props**、列描述对象、**`columnType`** 行为、插槽与边界、与 **C7DictTag** / **C7Preview** 的依赖约定）。

### Modified Capabilities

- （无）本变更为新增能力，不修改 `openspec/specs/` 下既有规范的 REQUIREMENTS。

## Impact

- **前端**：`quick-ui/src/packages/`、`quick-ui/src/packages/index.js`；新增 E2E 路由与页面；依赖已有 **Element Plus**、**C7DictTag**、**C7Preview**。
- **文档**：`docs/` 侧 VitePress 侧边栏与组件文档页。
- **非目标**：搜索区、分页、列持久化、批量操作；多选/序号列由父级自行声明 **`el-table-column type`**。
