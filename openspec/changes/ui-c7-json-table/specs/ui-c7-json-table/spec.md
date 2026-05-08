## ADDED Requirements

### Requirement: C7JsonTable 提供配置驱动的一体化列表壳

系统 MUST 在 `quick-ui` 中提供 **`C7JsonTable`** 组件：在传入 **`listFunction`** 与 **`tableColumns`** 的前提下，MUST 能够展示 **`el-table`** 数据区、底部分页，并 MUST 通过 **`listFunction`** 拉取数据；MUST 支持可选 **`searchColumns`** 搜索区、工具栏内置能力（见后续 Requirement）及插槽扩展。

#### Scenario: 最小配置展示列表与分页

- **WHEN** 调用方传入有效的 **`listFunction`** 与 **`tableColumns`**，且列表请求成功返回可解析的行与总数
- **THEN** 表格展示行数据，**`C7Pagination`** 展示总数与当前页，且再次翻页时 **`listFunction`** 以包含 **`pageNum`/`pageSize`** 的参数被调用

### Requirement: 列表请求参数与响应解析

组件 MUST 将查询参数组装为包含 **`searchParam` 展开字段**、**`pageNum`**、**`pageSize`**、**`orderByColumn`**、**`isAsc`** 的对象并传入 **`listFunction`**。组件 MUST 支持 **`rowsKey`** 与 **`totalKey`** 点路径从响应中读取行数组与总数；解析失败时 MUST 视为空列表与 0 条总数，且在开发环境 MUST 输出 **`console.warn`**。

#### Scenario: 自定义 rowsKey 与 totalKey

- **WHEN** 配置 **`rowsKey`** 为 **`data.records`** 且 **`totalKey`** 为 **`data.total`**，且响应结构匹配
- **THEN** 表格绑定 **`data.records`**，分页总数为 **`data.total`**

### Requirement: 搜索区子集与数据同步

组件 MUST 在挂载时使用 **`defaultSearchParam`** 的深拷贝初始化内部搜索对象；MUST 在搜索项变化时同步 **`update:searchParam`**（或与项目一致的 **`v-model:searchParam`** 双向绑定）。搜索区 MUST 仅首版实现 **`searchColumns`** 中 **`type`** 为 **`input`**、**`select`**、**`date`**、**`daterange`**、**`slot`** 的项；未知类型 MUST 在开发环境 **`console.warn`** 并跳过渲染。组件 MUST 在搜索区按 **Enter** 触发查询；MUST 提供重置将搜索恢复为 **`defaultSearchParam`** 并重新拉数，且 MUST 将当前页置为 **1**（与「搜索提交」UX 一致）。

#### Scenario: 重置搜索回到默认并回到第一页

- **WHEN** 用户修改搜索项后点击重置
- **THEN** 内部搜索对象恢复为 **`defaultSearchParam`** 的快照，**`currentPage`** 为 **1**，并触发 **`listFunction`**

### Requirement: 表格、排序与内置选择列

组件 MUST 使用单一 **`el-table`** 绑定列表数据；MUST 支持 **`border`**、**`stripe`**、**`rowKey`**（由 prop **`rowKey`** 映射至 **`el-table`** 的 **`row-key`**）。当启用多选时，组件 MUST 渲染 **`type="selection"`** 列；当启用序号列时，MUST 渲染 **`type="index"`** 列。组件 MUST 在 **`sort-change`** 后更新排序状态并重新请求列表。组件 MUST 将 **`lazy`**、**`load`**、**`tree-props`** 等与树懒加载相关的属性透传至 **`el-table`**（首版不要求树表业务验收）。

#### Scenario: 排序变化触发重新加载

- **WHEN** 用户对可排序列触发排序变更
- **THEN** 组件更新内部排序字段并再次调用 **`listFunction`**

### Requirement: 默认列渲染与 table-columns 插槽

在未提供 **`#table-columns`** 插槽时，组件 MUST 在 **`selection`/`index`** 列之后渲染 **`<C7JsonTableColumn :columns="effectiveTableColumns" />`**，其中 **`effectiveTableColumns`** MUST 为合并列显隐与顺序后的列配置，且 MUST 满足 **`C7JsonTableColumn`** 对 **`visible`** 与 **`order`** 的约定。当提供 **`#table-columns`** 时，组件 MUST **不**渲染默认 **`C7JsonTableColumn`**，而由插槽输出列节点；插槽作用域 MUST 至少包含 **`tableColumns`**、**`searchParam`**、**`selectedRows`**、**`refreshData`**、**`getDataList`**。

#### Scenario: 自定义列子树替换默认列

- **WHEN** 调用方提供 **`#table-columns`** 并在插槽内自行渲染 **`el-table-column`**
- **THEN** 组件不渲染默认 **`C7JsonTableColumn`**，且 **`el-table`** 仍由 **`C7JsonTable`** 持有并绑定同一 **`data`**

### Requirement: 列设置与 localStorage 持久化

组件 MUST 从 **`tableColumns`** 中识别带 **`prop`** 的列作为可配置列。组件 MUST 使用运行时 **`_visible`**（或等价机制）控制列是否进入 **`effectiveTableColumns`**；MUST 通过 **`normalizedColumns`**（或等价计算）合并 **`tableColumns`** 与持久化状态，且 MUST **不**直接修改调用方传入的 props 引用。当传入 **`columnSettingKey`** 时，组件 MUST 将列可见状态写入 **`localStorage`**；MUST 提供重置列设置以清除该键对应状态并恢复默认可见性。

#### Scenario: 列显隐按 key 持久化

- **WHEN** 用户通过列设置 UI 隐藏某一 **`prop`** 列且 **`columnSettingKey`** 已设置
- **THEN** 该列不再渲染，且刷新页面后该 **`prop`** 列仍保持隐藏，直至用户重置列设置

### Requirement: 删除流程

当存在 **`deleteFunction`** 时，组件 MUST 在工具栏展示批量删除入口。组件 MUST 在删除前调用 **`beforeDelete(ids, rows)`**（若提供）：若返回 **`false`**，MUST 取消删除。否则 MUST 使用 **`ElMessageBox.confirm`**（文案允许 prop 配置，首版允许默认中文）。组件 MUST 调用 **`deleteFunction(ids)`**；MUST 在提供 **`checkDeleteSuccess(res)`** 时以其返回值判定成功，否则 MUST 使用项目对齐的默认判定（实现文档写死）。删除成功后 MUST **`emit('delete-success', ids)`** 并 MUST 调用 **`refreshData()`**（保留当前页，与原始需求一致）。

#### Scenario: beforeDelete 返回 false 时不调用 deleteFunction

- **WHEN** **`beforeDelete`** 返回 **`false`**
- **THEN** 不调用 **`deleteFunction`**，且不展示删除成功提示

### Requirement: 导出流程与 Blob 契约

当存在 **`exportFunction`** 时，组件 MUST 在工具栏展示导出入口。**`exportFunction`** MUST 在被调用时使用导出发起时刻的 **`searchParam` 深拷贝快照** 作为参数上下文（若 **`exportFunction` 无参**，则组件 MUST 在闭包或包装函数中固定该快照语义，并在 JSDoc 中说明）。**`exportFunction`** MUST 返回 **`Promise<Blob | { data: Blob, headers }>`**，且下载行为 MUST 与 **`C7ExcelDownload`** 所依赖的 **`blobValidate`** / 文件名解析习惯一致。当 **`exportLoadingOptions`** 不为 **`false`** 时，导出进行中 MUST 使用 **`ElLoading.service`** 全屏加载；为 **`false`** 时 MUST 不使用该全屏 Loading。导出成功时 MUST **`emit('export-success')`**。

#### Scenario: 导出使用搜索快照

- **WHEN** 用户修改搜索条件后点击导出，且 **`exportFunction`** 由组件包装为使用快照
- **THEN** 实际导出请求所携带的查询条件与点击导出时刻的 **`searchParam`** 一致（不因用户随后修改搜索框而改变）

### Requirement: 分页组件绑定

组件 MUST 使用 **`C7Pagination`**：**`v-model:currentPage`**、**`v-model:pageSize`**，且 **`total`** MUST 来自最近一次成功列表响应。组件 MUST 在 **`C7Pagination`** 的汇总 **`change`**（或等价事件）上触发数据加载。组件 MUST 提供 **`refreshData`**：保留当前页并重新拉数；MUST 提供 **`getDataList`**：将页码置 **1** 并拉数。

#### Scenario: refreshData 不改变当前页

- **WHEN** 当前 **`currentPage`** 大于 **1** 且调用 **`refreshData`**
- **THEN** 下一次 **`listFunction`** 调用中的 **`pageNum`** 仍为当前页

### Requirement: 事件、错误处理与 Expose

组件 MUST **`emit('before-fetch', params)`**（若与拦截语义冲突则以实现定名，但 MUST 与文档一致）与 **`after-fetch(rows, total)`** 于列表成功路径；MUST **`emit('selection-change', rows)`**、**`sort-change`** 载荷与 **`el-table`** 一致；MUST **`emit('fetch-error', err)`**（或项目统一命名的列表错误事件）于列表失败路径，且 MUST **不**默认再次弹出与拦截器重复的错误 **`ElMessage`**。组件 MUST **`defineExpose`**：**`refreshData`**、**`getDataList`**、**`selectedRows`**、**`searchParam`**、**`currentPage`**、**`currentPageSize`**、**`total`**、**`tableRef`**。

#### Scenario: 列表失败仅走全局错误处理加 emit

- **WHEN** **`listFunction`** reject 且项目 axios 拦截器已提示错误
- **THEN** 组件 **`emit`** 错误事件且不额外弹出相同语义的 **`ElMessage`**

### Requirement: 包注册与交付物位置

组件源码 MUST 位于 **`quick-ui/src/packages/C7JsonTable/index.vue`**（首版）。**`quick-ui/src/packages/index.js`** MUST **export** **`C7JsonTable`** 并在 **`installPackages`** 中 **`app.component`** 注册。

#### Scenario: 应用 installPackages 后可全局使用

- **WHEN** 应用已执行与现有 C7 包相同的安装流程
- **THEN** 模板中可使用 **`<C7JsonTable />`** 而无需额外局部注册
