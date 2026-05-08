## 1. 前置核对

- [ ] 1.1 只读查阅 `quick-ui` 中列表页与 `request`/`list` 封装，定案 **`before-fetch` 是否可拦截请求**（与设计 **Open Questions** 一致），并在组件 JSDoc 写清
- [ ] 1.2 对照现有列表接口，确认 **`listFunction`** 默认参数名 **`pageNum`/`pageSize`/`orderByColumn`/`isAsc`** 是否与项目一致；不一致则在实现中按主流页面统一（必要时单点注释说明）

## 2. 组件骨架与注册

- [ ] 2.1 新建 `quick-ui/src/packages/C7JsonTable/index.vue`：`defineOptions`、props/emits 骨架、模板分区（搜索 / 工具栏 / `el-table` / `C7Pagination`）
- [ ] 2.2 在 `quick-ui/src/packages/index.js` 中 **import / export** 并 **`app.component('C7JsonTable', …)`** 注册

## 3. 数据流与解析

- [ ] 3.1 实现 **`listFunction`** 调用链：组装参数、**`before-fetch`**（按 1.1 定案）、**`after-fetch`**、loading 状态、**`rowsKey`/`totalKey`** 点路径解析与 dev **`console.warn`**
- [ ] 3.2 实现 **`fetch-error`**（或已定名的列表错误事件）emit；**不**重复 toast（与 spec 一致）
- [ ] 3.3 实现 **`refreshData`** / **`getDataList`** 与 **`defineExpose`** 其余字段（**`selectedRows`**、**`searchParam`**、分页、**`total`**、**`tableRef`**）

## 4. 搜索区（子集）

- [ ] 4.1 **`defaultSearchParam`** 深拷贝初始化、**`update:searchParam`**、重置与 **Enter** 搜索、搜索提交时 **currentPage=1**
- [ ] 4.2 实现 **`input`/`select`/`date`/`daterange`/`slot`** 渲染路径；未知 **`type`** dev **`console.warn`** 并跳过
- [ ] 4.3 接入 **`search-extra`** slot

## 5. 表格、排序、选择

- [ ] 5.1 **`el-table`**：**`data`**、**`row-key`**、**`border`/`stripe`**、多选列与 **`showIndex`**、**`sort-change`** 触发重载
- [ ] 5.2 树相关 props **透传**至 **`el-table`**
- [ ] 5.3 默认列区渲染 **`C7JsonTableColumn`**，传入 **`effectiveTableColumns`**；实现 **`#table-columns`** 分支与作用域参数

## 6. 分页

- [ ] 6.1 集成 **`C7Pagination`** 双绑与 **`change`** 拉数；**`total`** 来自最后一次成功列表响应

## 7. 列设置

- [ ] 7.1 实现 **`normalizedColumns`**（或等价）合并 **`tableColumns`**、**`_visible`** 与 **`columnSettingKey`** 的 **localStorage** 读写
- [ ] 7.2 工具栏列设置 UI（Popover/Drawer 选型）与重置列设置

## 8. 删除与导出

- [ ] 8.1 批量删除：**`beforeDelete`**、确认框、**`deleteFunction`**、**`checkDeleteSuccess`**、成功提示与 **`delete-success`** + **`refreshData`**
- [ ] 8.2 导出：包装 **`exportFunction`** + **`searchParam`** 快照；与 **`C7ExcelDownload`** 或等价下载逻辑对齐 **Blob / `{data,headers}`**；**`exportLoadingOptions`** 控制 **`ElLoading.service`**；避免与按钮 loading 重复（按 design 二选一写死）
- [ ] 8.3 **`export-success`** emit

## 9. 工具栏与插槽

- [ ] 9.1 **`toolbar-left`** / **`toolbar-right`**；条件渲染批量删除、导出、列设置、刷新按钮

## 10. 验证

- [ ] 10.1 `pnpm build:prod`（在 `quick-ui` 目录）通过
- [ ] 10.2 （可选）新增 `views/dev` + 路由：`listFunction`/导出/删除 mock，便于人工验收主路径
