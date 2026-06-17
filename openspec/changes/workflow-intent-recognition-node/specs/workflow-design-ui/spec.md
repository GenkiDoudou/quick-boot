## MODIFIED Requirements

### Requirement: 节点卡片视觉（P1）

画布节点 MUST 使用统一卡片组件展示：固定宽度约 240px、左侧色条、类型图标、**一行业务摘要**（MUST NOT 将 nodeId 作为唯一展示信息）。

节点 MUST 支持选中态（外发光）及运行态样式：`runStatus` 为 `RUNNING`/`SUCCESS`/`FAILED` 时分别显示蓝/绿/红边框（RUNNING 可有脉冲动画）。

左栏节点面板 MUST 按 **基础 / 逻辑 / AI / 工具** 分组，并提供搜索过滤。

分支节点（`if-else`、`question-classifier`）的出口连线 MUST 显示可读标签（如 IF/ELSE 或意图名称）；`question-classifier` 意图边 MUST 显示 `intents[i].name`，兜底边（`sourceHandle="0"`）MUST 显示「其他」。

#### Scenario: 节点显示摘要

- **WHEN** 画布存在 LLM 节点且 temperature=0.3、streaming=true
- **THEN** 节点卡片摘要包含温度与流式相关文案，不仅显示节点 ID

#### Scenario: 节点面板分组搜索

- **WHEN** 用户在左栏搜索「知识」
- **THEN** 面板仅展示名称或描述匹配「知识」的节点类型（如知识检索）

#### Scenario: 意图识别边标签

- **WHEN** 画布存在 `question-classifier` 节点且 `intents` 为「售前咨询」「售后问题」，并已连出 handle `1`、`2` 与兜底 `0`
- **THEN** 对应连线标签分别为「售前咨询」「售后问题」「其他」

### Requirement: 配置面板与变量选择器（P2）

右侧配置面板 MUST 包含 Tab：**设置**、**上次运行**（运行后有数据时可用）。

「设置」Tab MUST 按节点 `type` 渲染专用表单，**禁止**以 JSON 文本框作为 `if-else.conditions`、`question-classifier.intents`、`parameter-extractor.schema` 的唯一编辑方式。

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

## ADDED Requirements

### Requirement: 意图识别节点配置表单

设计器 MUST 为 `question-classifier` 提供专用表单（展示名「意图识别」），包含：

| 区块 | 说明 |
|------|------|
| 运行模式 | `fast`（极速，≤10 意图）/ `full`（完整，≤50 意图） |
| 大模型 | 下拉选择 Chat 模型，可清空（回退工作流/全局默认） |
| 输入 | `query` 模板字段，支持变量插入 |
| 意图匹配 | 表格：意图名称（必填）、典型示例（textarea，一行一例，可选） |
| 系统提示词 | 仅 `mode=full` 时显示 |

表单 MUST 在校验时 enforce：fast ≤10 意图、full ≤50 意图；每条意图 `name` 必填。

#### Scenario: 极速模式隐藏系统提示词

- **WHEN** 用户选择运行模式「极速」
- **THEN** 系统提示词输入区不可见

#### Scenario: 完整模式显示系统提示词

- **WHEN** 用户选择运行模式「完整」
- **THEN** 显示系统提示词 textarea 并可编辑

#### Scenario: 意图列表示例编辑

- **WHEN** 用户在意图行「典型示例」中输入多行文本
- **THEN** 保存为 `examples` 数组（按行拆分）

### Requirement: 意图识别画布出口 Handle

`question-classifier` 节点画布右侧 MUST 为每个 `intents[i]` 渲染一个出口 Handle，`id` 为字符串 `"1"`..`"N"`；MUST 在末尾渲染固定兜底 Handle，`id` 为 `"0"`，展示文案「其他」。新建节点时 MUST 默认至少一条意图。

#### Scenario: 新增意图同步 Handle

- **WHEN** 用户在表单中新增第 3 个意图
- **THEN** 画布节点出现第 3 个出口 Handle（`id="3"`）且兜底 Handle 仍为 `"0"`

### Requirement: 意图识别 nodeMeta 展示

左栏节点面板与节点卡片 MUST 将 `question-classifier` 展示为「意图识别」；`nodeMeta.outputs` MUST 声明 `classificationId`、`classificationName`、`reason`；摘要 SHOULD 体现模式与意图数量。

#### Scenario: 面板显示意图识别

- **WHEN** 用户在左栏「逻辑」分组浏览节点
- **THEN** 可见「意图识别」而非仅「问题分类」旧称
