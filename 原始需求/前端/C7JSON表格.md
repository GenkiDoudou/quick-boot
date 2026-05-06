# C7JSON表格（C7JsonTable）原始需求

## 背景
- 后台列表页普遍包含：搜索区、表格区、批量操作、删除、导出、列显隐设置、分页、排序等。
- 希望通过配置快速搭建，同时保留 slot 扩展点。

## 目标
- 提供一个一体化列表组件：
  - 配置搜索项与表格列即可运行
  - 内置删除/导出能力
  - 内置列设置（可持久化）
  - 内置分页与排序参数组装

## 功能需求
### 1. 数据获取
- `listFunction(params)` 获取数据：
  - 组件负责组装参数：`searchParam + pageNum/pageSize + orderByColumn/isAsc`
- 支持 `rowsKey/totalKey` 指定从响应中读取 rows/total 的路径（支持 `data.records`）。

### 2. 搜索区
- `searchColumns[]` 驱动生成表单：
  - 支持 input/select/date/daterange/slot
- Enter 触发搜索；支持重置回 `defaultSearchParam`。
- 提供 `search-extra` slot 扩展。

### 3. 工具栏
- 左侧：
  - `toolbar-left` slot
  - 内置批量删除按钮（存在 `deleteFunction` 时）
  - 内置导出按钮（存在 `exportFunction` 时）
- 右侧：
  - `toolbar-right` slot
  - 列设置按钮
  - 刷新按钮

### 4. 表格区
- 支持多选（selection）、序号列（showIndex）、border/stripe。
- 支持排序变化事件并触发重新加载。
- 树形懒加载相关参数预留（rowKey/lazy/treeProps/loadFunction）。

### 5. 列设置
- 从 `tableColumns` 中抽取可设置列（具备 prop 的列）。
- 使用 `_visible` 控制显示；可通过 `columnSettingKey` 持久化到 localStorage。
- 支持重置列设置。

### 6. 删除
- 批量删除选中行（按 `rowKey` 取 id）。
- 删除前确认：
  - 优先 `beforeDelete(ids,rows)`，返回 false 取消
  - 否则使用内置确认框
- 删除成功判定：
  - 优先 `checkDeleteSuccess(res)`，否则以 `!!res`
- 成功后提示并刷新列表。

### 7. 导出
- 支持导出 loading：
  - `exportLoadingOptions=false` 不显示
  - 否则用 `ElLoading.service`
- 导出参数使用 `searchParam` 快照。

## 事件
- `before-fetch(params)` / `after-fetch(rows,total)`
- `selection-change(rows)`
- `sort-change({prop,order})`
- `delete-success(ids)` / `export-success()`

## 对外能力（Expose）
- `refreshData()`（保留当前页）
- `getDataList()`（回到第一页）
- `selectedRows/searchParam/currentPage/currentPageSize/total/tableRef`

## 验收标准
- 传入 listFunction + columns 即可展示列表并分页、排序正常。
- 列设置可隐藏/显示列并按 key 持久化。

