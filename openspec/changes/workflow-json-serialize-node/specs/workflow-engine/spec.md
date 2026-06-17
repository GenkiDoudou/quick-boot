## ADDED Requirements

### Requirement: JSON 序列化节点 Handler

系统 MUST 注册 `json-serialize` 类型 `NodeHandler`（`JsonSerializeNodeHandler`），执行流程：

1. 通过 `InputParameterTemplateRenderer.resolveInputVariables` 解析 `data.inputVariables`
2. 取第一个有效参数名对应的值 `value`
3. 按序列化规则生成字符串 `output`
4. 返回 `NodeResult.success`，outputs 仅含 `output`（string）

序列化规则 MUST 为：

| 输入 `value` | `output` |
|--------------|----------|
| `null` 或参数缺失 | `""` |
| `String` 且 trim 后为空 | `""` |
| `String` 且为合法 JSON 文本 | 原样返回（trim 后，不二次 stringify） |
| `String` 普通非 JSON 文本 | `JSONUtil.toJsonStr(value)` |
| `Map` / `List` / `Number` / `Boolean` 等 | `JSONUtil.toJsonStr(value)` 紧凑单行 |
| 其他不可序列化类型 | `""` |

合法 JSON 文本：满足 `JSONUtil.isTypeJSON(str)` 为 true，或 `JSONUtil.parse(str)` 不抛异常。

节点执行 MUST 始终为 SUCCESS（不因空值或序列化失败改为 FAILED）。

#### Scenario: Object 输入序列化为 JSON 字符串

- **WHEN** `json-serialize` 节点输入解析为 `Map` `{ "name": "张三" }`
- **THEN** 节点 outputs 含 `output` 为紧凑 JSON 字符串 `{"name":"张三"}`，节点状态 SUCCESS

#### Scenario: 已是 JSON 字符串透传

- **WHEN** 输入解析为 String `{"code":0}` 且为合法 JSON 对象文本
- **THEN** `output` 为 `{"code":0}`（与输入相同，无额外转义层）

#### Scenario: 普通文本序列化为 JSON 字符串字面量

- **WHEN** 输入解析为 String `hello`（非 JSON 对象/数组文本）
- **THEN** `output` 为 `"hello"`（带 JSON 字符串引号）

#### Scenario: 空输入返回空字符串

- **WHEN** 输入解析为 `null` 或空字符串
- **THEN** `output` 为 `""`，节点状态 SUCCESS

### Requirement: JSON 序列化节点 data 模型

`json-serialize` 节点 `data` MUST 支持：

| 字段 | 类型 | 说明 |
|------|------|------|
| `inputVariables` | array | `{ key, value }[]`；**仅允许 1 个有效参数**；`value` 为上游引用或固定文本 |

默认值 SHOULD 为 `[{ "key": "input", "value": "" }]`。

节点 outputs MUST 固定为：

| 字段 | 类型 | 说明 |
|------|------|------|
| `output` | string | JSON 序列化结果 |

下游 MUST 可通过 `{{nodeId.output}}` 引用。

#### Scenario: 固定 JSON 文本作为输入

- **WHEN** 节点配置 `inputVariables` 为 `[{ "key": "input", "value": "{\"x\":1}" }]`
- **THEN** 执行后 `output` 为 `{"x":1}`（合法 JSON 透传）

## MODIFIED Requirements

### Requirement: 工作流 DSL 校验

系统 MUST 在 `saveGraph` 与 `publish` 前校验 DSL：

1. 有且仅有一个 `start` 节点、至少一个 `answer` 节点
2. 图为 DAG（无环）
3. 所有边引用的节点存在；从 `start` 可达所有 `answer`；无孤立节点
4. `if-else` 出口边 MUST 带 `sourceHandle` 为 `true` 或 `false`
5. `question-classifier`（意图识别）出口边 MUST 带 `sourceHandle`；意图边 handle MUST 为 `"1"` 至 `"N"`；**必须**存在兜底边 `sourceHandle="0"`；`intents` 非空
6. `json-serialize` 节点 MUST 配置至少一个 `inputVariables` 项，且 `key`、`value` 均非空
7. 各节点 `data` MUST 通过对应 JSON Schema 校验

#### Scenario: 非法 graph 保存被拒绝

- **WHEN** 用户保存缺少 `answer` 节点的 graph
- **THEN** 接口返回可识别业务错误，且不更新 `graph_json`

#### Scenario: 含环 graph 被拒绝

- **WHEN** 用户保存形成环路的 graph
- **THEN** 校验失败并返回明确错误信息

#### Scenario: JSON 序列化节点未配置输入

- **WHEN** 用户保存或发布含 `json-serialize` 节点但 `inputVariables` 为空或 `key`/`value` 为空
- **THEN** 校验失败并提示须配置输入参数

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
| `question-classifier` | LLM 意图识别 |
| `parameter-extractor` | LLM 结构化参数抽取 |
| `list-operator` | 列表 filter/first/last/map-field |
| `json-serialize` | 将 Object/Array 等序列化为 JSON 字符串；合法 JSON 字符串透传 |

系统 MUST NOT 将 `json-serialize` 实现为任意脚本执行。

#### Scenario: knowledge-retrieval 跨库隔离

- **WHEN** 工作流节点配置 `kbId=1` 且向量库存在 `kbId=2` 数据
- **THEN** 节点输出 chunks 中不包含 `kbId=2` 的片段

#### Scenario: http-request 内网 URL 失败

- **WHEN** http-request 节点请求内网地址（如 127.0.0.1）
- **THEN** 节点状态为 FAILED，`wf_run` 终态为 FAILED，且 `error_msg` 非空

#### Scenario: if-else 分支路由

- **WHEN** if-else 条件评估为 true
- **THEN** 引擎仅沿 `sourceHandle=true` 的后继边继续执行
