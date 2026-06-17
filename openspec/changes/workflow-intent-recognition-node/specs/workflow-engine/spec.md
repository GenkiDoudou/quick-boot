## MODIFIED Requirements

### Requirement: 工作流 DSL 校验

系统 MUST 在 `saveGraph` 与 `publish` 前校验 DSL：

1. 有且仅有一个 `start` 节点、至少一个 `answer` 节点
2. 图为 DAG（无环）
3. 所有边引用的节点存在；从 `start` 可达所有 `answer`；无孤立节点
4. `if-else` 出口边 MUST 带 `sourceHandle` 为 `true` 或 `false`
5. `question-classifier`（意图识别）出口边 MUST 带 `sourceHandle`；意图边 handle MUST 为 `"1"` 至 `"N"`（N = `intents.length`）；**必须**存在兜底边 `sourceHandle="0"`；`intents` 非空；`mode=fast` 时 `intents.length` ≤ 10，`mode=full` 时 ≤ 50
6. 各节点 `data` MUST 通过对应 JSON Schema 校验

#### Scenario: 非法 graph 保存被拒绝

- **WHEN** 用户保存缺少 `answer` 节点的 graph
- **THEN** 接口返回可识别业务错误，且不更新 `graph_json`

#### Scenario: 含环 graph 被拒绝

- **WHEN** 用户保存形成环路的 graph
- **THEN** 校验失败并返回明确错误信息

#### Scenario: 意图识别节点缺少兜底边

- **WHEN** 用户保存或发布含 `question-classifier` 节点但未连接 `sourceHandle="0"` 的边
- **THEN** 校验失败并提示须连接「其他」兜底出口

#### Scenario: 极速模式意图数量超限

- **WHEN** `question-classifier` 节点 `mode=fast` 且 `intents.length` > 10
- **THEN** 校验失败并返回意图数量超限错误

### Requirement: 十二种节点 Handler

系统 MUST 注册并实现以下节点类型（`NodeHandlerRegistry`），且每种节点 MUST 具备 `validateSchema()` 与 `execute()`：

| type | 说明 |
|------|------|
| `start` | 定义运行入参 |
| `answer` | 终止并组装最终响应 |
| `llm` | 调用 Spring AI ChatModel |
| `knowledge-retrieval` | 调用 KnowledgeSearchService 语义检索 |
| `if-else` | 条件分支（eq/ne/contains/gt 等） |
| `template-transform` | 文本模板拼接 |
| `variable-assign` | 变量赋值 |
| `variable-aggregator` | 多路变量合并 |
| `http-request` | HTTP 调用（含 SSRF 防护） |
| `question-classifier` | LLM 意图识别（极速/完整模式；输出 `classificationId`/`classificationName`/`reason`；数字分支 handle） |
| `parameter-extractor` | LLM 结构化参数抽取 |
| `list-operator` | 列表 filter/first/last/map-field |

系统 MUST NOT 实现 `code` 类型节点（任意脚本执行）。

#### Scenario: knowledge-retrieval 跨库隔离

- **WHEN** 工作流节点配置 `kbId=1` 且向量库存在 `kbId=2` 数据
- **THEN** 节点输出 chunks 中不包含 `kbId=2` 的片段

#### Scenario: http-request 内网 URL 失败

- **WHEN** http-request 节点请求内网地址（如 127.0.0.1）
- **THEN** 节点状态为 FAILED，`wf_run` 终态为 FAILED，且 `error_msg` 非空

#### Scenario: if-else 分支路由

- **WHEN** if-else 条件评估为 true
- **THEN** 引擎仅沿 `sourceHandle=true` 的后继边继续执行

#### Scenario: 意图识别命中第 N 个意图

- **WHEN** `question-classifier` 节点配置 3 个意图且模型返回 `classificationId=2`
- **THEN** 引擎沿 `sourceHandle="2"` 后继边继续执行，节点 outputs 含 `classificationId=2`、`classificationName` 为第 2 个意图名称及非空 `reason`

#### Scenario: 意图识别未命中走兜底

- **WHEN** 模型返回 `classificationId=0` 或解析失败或 `classificationId` 越界
- **THEN** 引擎沿 `sourceHandle="0"` 后继边继续执行，节点状态为 SUCCESS，`reason` 说明依据或失败原因

#### Scenario: 意图识别未配置可用模型

- **WHEN** 节点与工作流均未配置可用 ChatModel
- **THEN** 节点状态为 FAILED（配置错误），非兜底分支

## ADDED Requirements

### Requirement: 意图识别节点 data 模型与兼容

`question-classifier` 节点 `data` MUST 支持以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `mode` | `fast` \| `full` | 极速最多 10 意图；完整最多 50 意图且可追加 `systemPrompt` |
| `modelId` | Long \| null | 节点级 Chat 模型；空则工作流/全局默认 |
| `query` | string | 待识别文本模板 |
| `systemPrompt` | string | 仅 `full` 生效 |
| `intents` | array | `{ name, examples[] }`；运行时 ID 为下标+1 |

系统 MUST 在校验与执行前将旧 `classes[]` 归一化为 `intents[]`：`name` 保留；`description` 非空时按行拆为 `examples`；丢弃旧 `id`。

#### Scenario: 旧 classes 自动迁移

- **WHEN** graph 中 `question-classifier` 仍使用 `classes[{id,name,description}]`
- **THEN** 执行前归一化为 `intents` 且可按顺序生成数字 handle

### Requirement: 意图识别节点输出

节点执行成功后 outputs MUST 包含：

| 字段 | 类型 | 说明 |
|------|------|------|
| `classificationId` | number | 1..intents.length 或 0（兜底） |
| `classificationName` | string | 命中意图名称；兜底时为空串或「其他」 |
| `reason` | string | 分类依据或失败摘要 |

下游 MUST 可通过 `{{nodeId.classificationId}}` 等方式引用。

#### Scenario: Trace 记录意图识别输出

- **WHEN** Debug 运行完成意图识别节点
- **THEN** `wf_run_step` 的 outputs 含 `classificationId`、`classificationName`、`reason`

### Requirement: 意图识别模型调用与 Prompt

Handler MUST 使用 `WorkflowAiGuard.requireChatModelInstance(workflowId, modelId)` 获取 ChatModel；通过 Chat API 调用；内置 prompt 要求模型仅返回 JSON `{ classificationId, reason }`；`full` 模式 MUST 在内置指令后追加 `systemPrompt`。

#### Scenario: 完整模式包含用户系统提示词

- **WHEN** 节点 `mode=full` 且 `systemPrompt` 非空
- **THEN** 发往模型的系统侧内容包含用户追加的 `systemPrompt`

#### Scenario: 指定节点 modelId

- **WHEN** 节点配置 `modelId` 且该模型可用
- **THEN** 使用对应 ChatModel 实例而非仅全局默认
