# C7JsonTable（C7 JSON 表格 / 一体化列表）设计说明

**日期**：2026-05-08  
**状态**：已定稿（经 brainstorming 澄清 **1C、2B、3B、4A、5A、6C** 与「设计确认」）  
**依据**：`原始需求/前端/C7JSON表格.md`、`docs/superpowers/specs/2026-05-08-c7-json-table-column-design.md`（列契约与 **`C7JsonTableColumn`** 引用）  
**实现路径**：首版 **单文件 `quick-ui/src/packages/C7JsonTable/index.vue`**；若体量过大再抽 **`useJsonTableQuery.ts`** / **`useTableColumnSettings.ts`** 等，**不**首版拆多子组件。

---

## 1. 背景与目标

- **背景**：后台列表页重复包含搜索区、表格区、工具栏、分页、排序、删除、导出、列显隐等；希望 **配置驱动** 快速搭建，并保留 **slot** 扩展点。
- **目标**：提供 **`C7JsonTable`**：传入 **`listFunction`** 与列/搜索配置即可运行；内置参数组装、分页、排序、列设置（localStorage）、批量删除与导出、刷新；与 **`C7JsonTableColumn`**、**`C7Pagination`**、**`C7ExcelDownload`** 契约对齐。
- **非目标（首版）**：**`searchColumns` 全量对齐 `C7JsonForm`**（联动、`visibleWhen`、`rules`、全部 `type`）；树表懒加载的完整业务验收；列设置 **后端持久化**（仅 **localStorage**）。

---

## 2. 命名、边界与落点

| 项 | 约定 |
|----|------|
| **对外组件名** | **`C7JsonTable`** |
| **目录** | **`quick-ui/src/packages/C7JsonTable/index.vue`**（首版） |
| **全局注册** | **`quick-ui/src/packages/index.js`**：**`export`** + **`installPackages`**（与现有 C7 包一致） |

**职责**：搜索区渲染（约定子集）、工具栏、**`el-table`** 数据绑定、**内置多选/序号列**、默认 **`C7JsonTableColumn`** 列区、**`C7Pagination`**、请求与分页/排序参数组装、列显隐与 localStorage、删除/导出流程、事件与 **expose**。

**不负责**：列单元格 **`columnType`** 细节（由 **`C7JsonTableColumn`** 承担，见列设计说明）。

---

## 3. 与 C7JsonTableColumn 的关系（澄清 1C）

- **默认**：在 **`el-table`** 内，于 **`type="selection"`**、**`type="index"`**（若开启）之后，渲染 **`<C7JsonTableColumn :columns="effectiveTableColumns" />`**，其中 **`effectiveTableColumns`** 为合并列设置 **`_visible`** 与 **`order`** 后的列数组（与列组件 **`visible !== false`** 规则一致，见列 spec）。
- **扩展**：若提供 **`#table-columns`** 插槽，则 **不渲染** 默认 **`C7JsonTableColumn`**；由插槽 **自行输出多个 `el-table-column` 或其它列组件**。插槽 **仅替换 `el-table` 下列子树**，外层 **仍为本组件持有的单个 `el-table`**。
- **插槽作用域（建议注入，实现阶段以代码为准）**：至少包含 **`tableColumns`**（当前生效列配置）、**`searchParam`**、**`selectedRows`**、**`refreshData`**、**`getDataList`**；便于自定义列区仍可调列表行为。

**多选 / 序号**：由 **`C7JsonTable`** 声明 **`el-table-column type="selection"`**、**`type="index"`**（与 **`C7JsonTableColumn` 设计** 中「由父级声明」一致；此处父级即 **`C7JsonTable`**）。

---

## 4. 与 C7JsonForm 的对齐（澄清 2B）

- **`searchColumns[]`** 使用 **C7JSON表单**（`原始需求/前端/C7JSON表单.md`）中概念的 **子集**，字段名尽量一致：**`prop`**、**`label`**、**`type`**、**`order`**、**`span`**、**`defaultValue`** 等。
- **首版支持的 `type`**（与原始表格需求一致）：**`input`**、**`select`**、**`date`**、**`daterange`**、**`slot`**。其它 JsonForm 类型（`upload`、`tree-select` 等）**首版不支持**；文档与实现中 **开发环境** 可对未知类型 **`console.warn`** 并跳过渲染。
- **首版不实现**：**`visibleWhen`**、**`disabledWhen`**、**`optionsWhen`**、**`linkage`**、**`rules`** 与表单项级校验；若后续需要，单独开变更将 JsonTable 搜索区 **升级为嵌入 `C7JsonForm`** 或扩展 schema。

---

## 5. 搜索数据模型

- **`defaultSearchParam`**：**对象**；组件 **挂载时深拷贝** 为内部 **`searchParam`** 初始值；**重置** 恢复为该快照。
- **对外同步**：通过 **`update:searchParam`**（及/或 **`v-model:searchParam`**，实现时与项目其它组件命名一致即可）把当前搜索对象同步给父级，保证 **导出** 与父级使用的快照一致。
- **交互**：**Enter** 触发查询（聚焦在搜索区内任一输入时）；**重置** 回到 **`defaultSearchParam`** 并重新拉数（页码是否回第一页见下节「分页行为」约定）。

---

## 6. 数据获取与解析

- **`listFunction(params)`**：**`params`** 由组件组装：  
  **`{ ...searchParam, pageNum, pageSize, orderByColumn, isAsc }`**  
  其中 **`pageNum` / `pageSize`** 与后端/若依风格不一致时，实现阶段在 **implementation plan** 中对照 **`quick-ui`** 现有列表页做一次字段名对齐（必要时提供 **prop 映射** 配置，**首版 YAGNI**：若全项目已统一为同一套字段名，则 **不增加** 映射 prop）。
- **`rowsKey` / `totalKey`**：从响应对象取列表与总数的 **点路径**（如 **`data.records`**、**`data.total`**）；解析失败时视为 **空列表 / 0** 并 **开发环境 `console.warn`**。
- **`before-fetch(params)`**：若返回 **`false`**，**取消**本次请求（与常见若依封装约定一致时采用；若项目无此惯例则首版 **仅 emit**，不拦截——实现前在代码库中 **核对** `request` 封装，**二选一写死**在实现说明中）。
- **`after-fetch(rows, total)`**：列表成功写入后触发。
- **错误（澄清 6C）**：列表请求失败依赖 **axios / request 拦截器** 的 **全局错误提示**；组件 **`emit('fetch-error', err)`**（事件名实现时可选用 **`list-error`**，但 **spec 与文档、实现三处统一**）。组件 **不默认二次 toast**。

---

## 7. 表格与排序

- **`el-table`**：**`border`**、**`stripe`**、**`row-key`**（来自 **`rowKey`** prop）、**`data`** 绑定列表行。
- **`sort-change`**：更新内部 **`orderByColumn` / `isAsc`**（或项目等价字段），并 **重新请求**；与 **`clear排序`** 行为与 Element Plus 表格一致即可。
- **树形懒加载**：**`lazy`**、**`load`**、**`tree-props`** 等 **仅透传** 至 **`el-table`**；首版 **不将树表列入验收**，仅保证不挡扩展。

---

## 8. 分页（澄清 5A）

- 使用 **`C7Pagination`**：**`v-model:currentPage`**、**`v-model:pageSize`**，**`total`** 来自最近一次成功列表响应。
- **`change(page, pageSize)`**（或等价的 **`C7Pagination`** 汇总事件）：触发 **`listFunction`**。用户 **切换 `pageSize`** 时依赖 **`C7Pagination`** 默认 **`autoReset`** 行为（回第一页）与拉数一致。
- **搜索提交**：建议 **将 `currentPage` 置为 1** 再请求（与常见 UX 一致）；**`refreshData`** 保留当前页（与原始需求一致）。

---

## 9. 列设置与持久化（澄清 4A）

- **可参与列设置的列**：**`tableColumns`** 中带 **`prop`** 的项。
- **显示控制**：运行时维护 **`_visible`**（或等价字段）；**`C7JsonTableColumn`** 侧使用其 **`visible`** 规则时，需与 JsonTable 合并策略一致：**推荐** 使用 **内部 `normalizedColumns`**，从 **`tableColumns` + localStorage 覆盖** 计算，**避免直接 mutate** 父级传入的只读 props。
- **`columnSettingKey`**：存在时，将 **`prop -> visible`**（或完整可见状态快照）写入 **`localStorage`**；**重置列设置** 清除该 key 对应状态并恢复默认 **`_visible`**。
- **列设置 UI**：工具栏 **列设置按钮** 打开 **Popover / Drawer**（实现选型在 implementation plan 中确定，**不影响**对外契约）。

---

## 10. 工具栏与内置操作

- **左侧**：**`toolbar-left`** slot；存在 **`deleteFunction`** 时 **内置批量删除**；存在 **`exportFunction`** 时 **内置导出**。
- **右侧**：**`toolbar-right`** slot；**列设置**、**刷新**。
- **`search-extra`**：搜索区额外扩展 slot。

**删除**：

- **`beforeDelete(ids, rows)`**：返回 **`false`** 则 **取消**。
- 否则 **内置 `ElMessageBox.confirm`**（文案可 prop 化，首版可用默认中文）。
- **`deleteFunction(ids)`**；**`checkDeleteSuccess(res)`** 可选，缺省时 **`!!res`** 或项目惯例（实现时与现有列表删除对齐）。
- 成功：**提示** + **`emit('delete-success', ids)`** + **`refreshData()`**。

**导出（澄清 3B）**：

- **`exportFunction()`** 返回 **`Promise<Blob | { data: Blob, headers }>`**，与 **`C7ExcelDownload`** / **`downloadRequest(..., { returnBlobWithHeaders: true })`** 一致。
- **参数**：使用 **发起导出时刻的 `searchParam` 深拷贝快照**（与原始需求一致）。
- **Loading**：**`exportLoadingOptions === false`** 时不使用 **`ElLoading.service`**；否则导出 Promise 期间 **全屏 Loading**（与原始需求一致）。可与 **`C7ExcelDownload`** 的 **`v-model:downloading`** 组合使用，**避免**重复 loading（实现细节：二选一或统一由 **`C7ExcelDownload`** 管理按钮 loading、由 JsonTable 管全屏，在 plan 中写清）。
- 成功：**`emit('export-success')`**；失败走 **下载组件 / 请求** 既有错误提示路径。

---

## 11. 事件与 Expose（与原始需求对齐）

**事件**：**`before-fetch`**、**`after-fetch`**、**`selection-change`**、**`sort-change`**、**`delete-success`**、**`export-success`**、**`fetch-error`**（或统一命名，见 §6）。

**Expose**：**`refreshData`**（当前页）、**`getDataList`**（回第一页并拉数）、**`selectedRows`**、**`searchParam`**、**`currentPage`**、**`currentPageSize`**、**`total`**、**`tableRef`**（`el-table` 实例）。

---

## 12. 测试建议

- **组件/逻辑**：请求参数组装、**`rowsKey`/`totalKey`** 解析、列设置与 localStorage 合并、**`before-delete`** 取消路径。
- **E2E（可选）**：新增 Dev 路由页，**mock `listFunction`**、导出 **Blob**、删除 **mock**，覆盖主路径。

---

## 13. 自检摘要

- **无 TBD**：**`before-fetch` 返回 false** 是否与项目 request 一致，已在 §6 要求实现前 **代码核对后二选一写死**。
- **与列设计一致**：多选/序号在 **JsonTable**；列渲染在 **`C7JsonTableColumn`**；**`#table-columns`** 覆盖列子树。
- **范围**：搜索为 **JsonForm 子集**；树表仅 **透传**；列持久化 **仅 localStorage**。
