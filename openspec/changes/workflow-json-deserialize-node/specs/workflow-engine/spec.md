## ADDED Requirements

### Requirement: JSON 反序列化节点 Handler

系统 MUST 注册 `json-deserialize` 类型 `NodeHandler`（`JsonDeserializeNodeHandler`），执行流程：

1. 通过 `InputParameterTemplateRenderer.resolveInputVariables` 解析 `data.inputVariables`
2. 取第一个有效参数值，转为非空 JSON 文本字符串
3. `JSONUtil.parse` 解析为根值 `root`
4. 校验 JSON 文档嵌套深度 ≤ 3
5. 按 `outputFields` 组装 `output`
6. 返回 `NodeResult.success`，outputs 含 `output`（object 或 array）

| `outputFields` | 根类型 | `output` |
|----------------|--------|----------|
| 空 | object 或 array | `root` 原样 |
| 非空 | object | `{ key: extract(root, path) }`；path 缺省等于 key |
| 非空 | array | 节点 **FAILED** |

点路径 MUST 使用 `.` 分段（如 `data.user.name`），不支持数组下标（本期）。

下列场景 MUST 导致节点 **FAILED**（非 SUCCESS）：

- 输入为空或仅空白
- 非法 JSON 文本
- 解析后 JSON 树嵌套深度 > 3
- 配置了 `outputFields` 但根值非 object

path 对应值不存在时 MUST 仍为 SUCCESS，该 key 值为 `null`。

#### Scenario: 整包反序列化无 outputFields

- **WHEN** 输入为合法 JSON 对象字符串 `{"name":"张三"}` 且 `outputFields` 为空
- **THEN** `output` 为 `{ "name": "张三" }`，节点 SUCCESS

#### Scenario: 按点路径提取字段

- **WHEN** 输入为 `{"data":{"user":{"name":"张三"}}}` 且 `outputFields` 含 `{ key: "name", path: "data.user.name" }`
- **THEN** `output` 为 `{ "name": "张三" }`，节点 SUCCESS

#### Scenario: 非法 JSON 失败

- **WHEN** 输入为 `{invalid`
- **THEN** 节点状态 FAILED，`error_msg` 非空

#### Scenario: 嵌套超深失败

- **WHEN** 输入 JSON 树深度 > 3
- **THEN** 节点状态 FAILED

#### Scenario: 有 fields 但根为数组

- **WHEN** 输入为 `[1,2,3]` 且 `outputFields` 非空
- **THEN** 节点状态 FAILED

#### Scenario: path 不存在

- **WHEN** 输入合法且 `outputFields` 含不存在 path
- **THEN** 节点 SUCCESS，对应 key 为 `null`

### Requirement: JSON 反序列化节点 data 模型

`json-deserialize` 节点 `data` MUST 支持：

| 字段 | 类型 | 说明 |
|------|------|------|
| `inputVariables` | array | `{ key, value }[]`；**仅 1 个有效参数** |
| `outputFields` | array | 可选 `{ key, path?, type? }[]`；`type` 为 string/number/boolean/object/array |

节点 outputs MUST 固定含：

| 字段 | 类型 | 说明 |
|------|------|------|
| `output` | object 或 array | 反序列化或提取结果 |

下游 MUST 可通过 `{{nodeId.output}}` 及 `{{nodeId.output.field}}` 引用。

#### Scenario: 固定 JSON 字符串输入

- **WHEN** `inputVariables` 配置固定值 `{"id":1}`
- **THEN** 执行后 `output.id` 为 1

## MODIFIED Requirements

### Requirement: 工作流 DSL 校验

系统 MUST 在 `saveGraph` 与 `publish` 前校验 DSL：

1. 有且仅有一个 `start` 节点、至少一个 `answer` 节点
2. 图为 DAG（无环）
3. 所有边引用的节点存在；从 `start` 可达所有 `answer`；无孤立节点
4. `if-else` 出口边 MUST 带 `sourceHandle` 为 `true` 或 `false`
5. `question-classifier`（意图识别）出口边 MUST 带 `sourceHandle`；意图边 handle MUST 为 `"1"` 至 `"N"`；**必须**存在兜底边 `sourceHandle="0"`；`intents` 非空
6. `json-serialize` 节点 MUST 配置至少一个 `inputVariables` 项，且 `key`、`value` 均非空
7. `json-deserialize` 节点 MUST 配置至少一个 `inputVariables` 项，且 `key`、`value` 均非空；`outputFields` 中 `key` MUST 不重复
8. 各节点 `data` MUST 通过对应 JSON Schema 校验

#### Scenario: 非法 graph 保存被拒绝

- **WHEN** 用户保存缺少 `answer` 节点的 graph
- **THEN** 接口返回可识别业务错误，且不更新 `graph_json`

#### Scenario: 含环 graph 被拒绝

- **WHEN** 用户保存形成环路的 graph
- **THEN** 校验失败并返回明确错误信息

#### Scenario: JSON 反序列化节点未配置输入

- **WHEN** 用户保存或发布含 `json-deserialize` 节点但 `inputVariables` 为空或 `key`/`value` 为空
- **THEN** 校验失败并提示须配置输入参数

#### Scenario: JSON 反序列化 outputFields key 重复

- **WHEN** `json-deserialize` 节点 `outputFields` 含重复 `key`
- **THEN** 校验失败并提示字段名重复

### Requirement: 工作流节点 Handler 注册

系统 MUST 注册并实现以下节点类型（`NodeHandlerRegistry`），且每种节点 MUST 具备 `execute()`：

| type | 说明 |
|------|------|
| `start` | 定义运行入参 |
| `answer` | 终止并组装最终响应 |
| `llm` | 调用 Spring AI ChatModel |
| `knowledge-retrieval` | 调用 KnowledgeSearchService 语义检索 |
| `if-else` | 条件分支 |
| `template-transform` | 文本模板拼接 |
| `variable-assign` | 变量赋值 |
| `variable-aggregator` | 多路变量合并 |
| `http-request` | HTTP 调用（含 SSRF 防护） |
| `question-classifier` | LLM 意图识别 |
| `parameter-extractor` | LLM 结构化参数抽取 |
| `list-operator` | 列表操作 |
| `json-serialize` | JSON 序列化 |
| `json-deserialize` | JSON 反序列化；点路径提取；深度 ≤ 3 |

#### Scenario: knowledge-retrieval 跨库隔离

- **WHEN** 工作流节点配置 `kbId=1` 且向量库存在 `kbId=2` 数据
- **THEN** 节点输出 chunks 中不包含 `kbId=2` 的片段

#### Scenario: if-else 分支路由

- **WHEN** if-else 条件评估为 true
- **THEN** 引擎仅沿 `sourceHandle=true` 的后继边继续执行
