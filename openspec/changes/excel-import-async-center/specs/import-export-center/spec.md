## ADDED Requirements

### Requirement: 导入导出中心菜单与权限

系统 SHALL 提供管理端菜单「导入导出中心」（或与产品命名一致的等价菜单名），并配置权限点至少包含：导入任务列表查询、导入任务详情、失败文件下载（复用文件管理下载权限或独立 `system:import:download`）。

#### Scenario: 有权限用户可访问中心

- **WHEN** 用户具备导入中心列表权限
- **THEN** 前端 MUST 展示导入导出中心页面且可加载导入任务列表

### Requirement: 导入任务列表 Tab

导入导出中心 MUST 提供「导入」Tab，使用 `C7JsonTable`（或项目等价列表模板）展示字段至少包含：`bizType`（中文标签可映射）、`status`、`total_rows`、`success_rows`、`fail_rows`、`create_time`、`finish_time`。

列表 MUST 支持按状态、业务类型、时间范围筛选；默认按创建时间倒序。

#### Scenario: 查看进行中的任务

- **WHEN** 存在 `status=RUNNING` 的任务
- **THEN** 列表 MUST 展示该任务且可进入详情查看 `processed_rows/total_rows` 进度

### Requirement: 导入任务详情与轮询

详情页 MUST 展示任务状态、进度、统计；当 `status` 为 `PENDING` 或 `RUNNING` 时，页面 MUST 自动轮询 `GET /import/task/{taskId}`（间隔可配置，建议 2–5s），完成后停止轮询。

#### Scenario: 任务完成后展示统计

- **WHEN** 任务 `status` 变为 `SUCCESS`
- **THEN** 详情页 MUST 展示 `total_rows`、`success_rows`、`fail_rows`

### Requirement: 失败文件下载

当 `fail_rows > 0` 且 `errorFileId` 非空时，详情页与列表操作列 MUST 提供「下载失败明细」按钮，调用 `GET /system/file/download/{fileId}` 保存 xlsx。

#### Scenario: 下载失败 Excel

- **WHEN** 用户点击下载失败明细且 `errorFileId` 有效
- **THEN** 浏览器 MUST 下载 xlsx 文件且文件名可读（如含 `bizType` 与任务 id）

### Requirement: 导出记录 Tab

导入导出中心 MUST 提供「导出」Tab，展示系统已有导出记录或导出任务（与现有导出能力对接；若当前仅为各业务即时下载，则本 Tab 展示导出历史表或占位说明，实现阶段以 design 对接方案为准）。

导出 Tab MUST 与导入 Tab 在同一页面通过 Tab 切换，不得拆分为两个顶级菜单。

#### Scenario: Tab 切换

- **WHEN** 用户在导入导出中心
- **THEN** 用户 MUST 能在「导入」「导出」Tab 间切换而无需离开页面

### Requirement: 与 C7ExcelUpload 联动

异步导入提交成功后，业务页 `C7ExcelUpload` MAY 展示「前往导入中心查看」链接，跳转至对应 `taskId` 详情或导入列表。

#### Scenario: 从用户管理页跳转中心

- **WHEN** 用户在用户管理页触发异步导入并获得 `taskId`
- **THEN** 用户 MUST 可通过链接在导入中心看到该任务
