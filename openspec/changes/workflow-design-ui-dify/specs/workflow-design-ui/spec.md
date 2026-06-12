## ADDED Requirements

### Requirement: 全屏沉浸设计器布局

系统前端 MUST 在工作流设计器路由（`/workflow/design/:id`）提供全屏布局：`meta.fullScreen=true` 时 MUST NOT 显示系统左侧菜单与 TagsView，画布区域 MUST 占满视口高度（`100vh`）。

顶栏 MUST 包含：返回列表、工作流名称、保存状态（已保存/保存中/未保存）、校验、测试运行、发布。

#### Scenario: 进入设计器为全屏

- **WHEN** 用户从工作流列表点击「设计」进入 `/workflow/design/:id`
- **THEN** 页面不显示系统左侧导航栏，设计器顶栏与画布占满可视区域

#### Scenario: 返回列表

- **WHEN** 用户点击顶栏「返回」
- **THEN** 导航至 `/workflow/list`

### Requirement: 节点卡片视觉（P1）

画布节点 MUST 使用统一卡片组件展示：固定宽度约 240px、左侧色条、类型图标、**一行业务摘要**（MUST NOT 将 nodeId 作为唯一展示信息）。

节点 MUST 支持选中态（外发光）及运行态样式：`runStatus` 为 `RUNNING`/`SUCCESS`/`FAILED` 时分别显示蓝/绿/红边框（RUNNING 可有脉冲动画）。

左栏节点面板 MUST 按 **基础 / 逻辑 / AI / 工具** 分组，并提供搜索过滤。

分支节点（`if-else`、`question-classifier`）的出口连线 MUST 显示可读标签（如 IF/ELSE 或分类名）。

#### Scenario: 节点显示摘要

- **WHEN** 画布存在 LLM 节点且 temperature=0.3、streaming=true
- **THEN** 节点卡片摘要包含温度与流式相关文案，不仅显示节点 ID

#### Scenario: 节点面板分组搜索

- **WHEN** 用户在左栏搜索「知识」
- **THEN** 面板仅展示名称或描述匹配「知识」的节点类型（如知识检索）

### Requirement: 自动保存

设计器 MUST 在 graph 变更后 debounce（默认 3s）自动调用 `POST /workflow/saveGraph`；顶栏 MUST 展示保存状态。用户 MUST 可使用 Ctrl+S 触发立即保存。

#### Scenario: 自动保存成功

- **WHEN** 用户拖拽新节点后等待超过 debounce 时间
- **THEN** 顶栏显示「已保存」且后端草稿 graph 已更新

### Requirement: 配置面板与变量选择器（P2）

右侧配置面板 MUST 包含 Tab：**设置**、**上次运行**（运行后有数据时可用）。

「设置」Tab MUST 按节点 `type` 渲染专用表单，**禁止**以 JSON 文本框作为 `if-else.conditions`、`question-classifier.classes`、`parameter-extractor.schema` 的唯一编辑方式。

系统 MUST 提供变量选择器：从当前图拓扑上游节点收集 `nodeMeta.outputs`，以树形展示并插入 `{{nodeId.field}}` 至光标位置；MUST 支持 `sys.kbId` 等系统变量。

`knowledge-retrieval` 节点的知识库选择 MUST 使用下拉（调用知识库列表 API），MUST NOT 要求用户仅手填数字 ID。

LLM 节点 `temperature` SHOULD 使用滑块与数字框组合控件。

#### Scenario: 变量插入 Prompt

- **WHEN** 用户在 LLM 节点 userPrompt 旁点击「插入变量」并选择 `start_1.question`
- **THEN** 输入框光标处插入 `{{start_1.question}}`

#### Scenario: 知识库下拉选择

- **WHEN** 用户配置知识检索节点
- **THEN** 可通过下拉选择知识库名称，选中后 `kbId` 写入节点 data

#### Scenario: 条件分支可视化配置

- **WHEN** 用户编辑 if-else 节点
- **THEN** 以条件行列表（左值、运算符、右值）配置，无需编辑原始 JSON 文本框

### Requirement: 测试运行与画布高亮（P3）

顶栏 MUST 提供单一「测试运行」入口，展开底部 **RunPanel**（可折叠，非 modal Drawer）。

RunPanel MUST 包含：Start 入参区（根据 `start.inputs` 动态渲染）、步骤 Trace 列表、LLM 流式输出区。

运行过程中 MUST 根据 `/run/debug` 或 SSE `step_start`/`step_end` 更新画布节点 `runStatus` 高亮。

用户点击 Trace 某步时，画布 MUST 定位/focus 对应节点；右侧「上次运行」Tab MUST 展示该步 inputs/outputs 摘要。

勾选流式时 MUST 使用 `POST /workflow/run/async` 与 SSE 订阅，流式文本 SHOULD 以 Markdown 渲染。

#### Scenario: 运行后节点高亮

- **WHEN** 用户执行测试运行且某节点执行成功
- **THEN** 该节点在画布上显示成功态边框（绿色）

#### Scenario: Trace 定位节点

- **WHEN** 用户点击 Trace 时间线中某 nodeId 步骤
- **THEN** 画布选中并视口聚焦该节点

### Requirement: workflowId 精度安全

前端调用工作流 API 时，`workflowId` MUST 以字符串形式传递（路由 params 或请求体），MUST NOT 使用 `Number(workflowId)` 转换雪花 ID。

#### Scenario: 保存不因 ID 精度失败

- **WHEN** 工作流 ID 为超过 JS 安全整数范围的雪花 ID
- **THEN** `saveGraph` 请求体中的 workflowId 与数据库一致且保存成功

### Requirement: 视觉规范

设计器 MUST 遵循 `DESIGN.md`：主色 `#0a2463`、强调 `#409eff`、画布背景 `#f2f4f7`、面板白底；字号以 13px 为基准。

#### Scenario: 与后台风格一致

- **WHEN** 用户在全屏设计器与系统列表页之间切换
- **THEN** 配色与字体与 DESIGN.md 约定一致，无突兀风格断裂
