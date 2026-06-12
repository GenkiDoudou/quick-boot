## ADDED Requirements

### Requirement: 工作流定义与版本（wf_workflow / wf_workflow_version）

系统 MUST 在 MySQL 中持久化工作流定义，主表 `wf_workflow` 至少包含：`workflow_id`、`name`、`description`、`status`（`DRAFT`/`PUBLISHED`/`DISABLED`）、`published_version_id`、`bot_enabled`（默认 0）、`external_api_enabled`（默认 0）、标准审计字段与 `deleted` 逻辑删除。

版本表 `wf_workflow_version` MUST 存储 `graph_json`（nodes + edges DSL）、`version_no`、`checksum`、`is_draft`（1 表示当前编辑草稿）。

#### Scenario: 创建工作流成功

- **WHEN** 具备 `workflow:add` 权限的用户提交合法名称
- **THEN** 数据库新增一条 `deleted=0` 的工作流记录，并创建初始草稿版本（含空 graph 或默认模板）

#### Scenario: 发布工作流生成新版本

- **WHEN** 用户对校验通过的草稿执行 publish
- **THEN** 生成新 `wf_workflow_version`（`is_draft=0`），更新 `wf_workflow.published_version_id`，且 `status=PUBLISHED`

#### Scenario: 草稿修改不影响已发布版本

- **WHEN** 用户修改草稿 graph 后保存
- **THEN** 已发布版本的 `graph_json` 不变；使用已发布版本运行结果不受草稿变更影响

### Requirement: 工作流 DSL 校验

系统 MUST 在 `saveGraph` 与 `publish` 前校验 DSL：

1. 有且仅有一个 `start` 节点、至少一个 `answer` 节点
2. 图为 DAG（无环）
3. 所有边引用的节点存在；从 `start` 可达所有 `answer`；无孤立节点
4. `if-else` 出口边 MUST 带 `sourceHandle` 为 `true` 或 `false`
5. `question-classifier` 出口边 MUST 带 `sourceHandle` 为已定义类别 id
6. 各节点 `data` MUST 通过对应 JSON Schema 校验

#### Scenario: 非法 graph 保存被拒绝

- **WHEN** 用户保存缺少 `answer` 节点的 graph
- **THEN** 接口返回可识别业务错误，且不更新 `graph_json`

#### Scenario: 含环 graph 被拒绝

- **WHEN** 用户保存形成环路的 graph
- **THEN** 校验失败并返回明确错误信息

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
| `question-classifier` | LLM 意图分类 |
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

### Requirement: 变量模板解析

系统 MUST 提供 `TemplateRenderer`，支持 `{{nodeId.field}}`、`{{sys.runId}}`、`{{sys.kbId}}`、`{{sys.userId}}`、`{{inputs.key}}` 语法。

系统 MUST NOT 使用 SpEL 或任意脚本引擎解析模板。

#### Scenario: 模板引用上游节点输出

- **WHEN** LLM 节点 userPrompt 配置为 `{{start_1.question}}` 且 start 节点已执行
- **THEN** 渲染后的 prompt 包含 start 入参中的 question 值

### Requirement: 工作流 CRUD API

系统 SHALL 提供以下接口（前缀 `/workflow`），使用 Sa-Token 权限校验；修改/删除统一 `@PostMapping`：

| 路径 | 方法 | 权限 |
|------|------|------|
| `/list` | GET | `workflow:list` |
| `/getInfo` | GET | `workflow:query` |
| `/add` | POST | `workflow:add` |
| `/update` | POST | `workflow:edit` |
| `/saveGraph` | POST | `workflow:edit` |
| `/validateGraph` | POST | `workflow:edit` |
| `/publish` | POST | `workflow:publish` |
| `/remove` | POST | `workflow:remove` |
| `/template/list` | GET | `workflow:query` |

#### Scenario: 获取工作流含草稿 graph

- **WHEN** 用户调用 `GET /workflow/getInfo?workflowId=1`
- **THEN** 响应包含元数据与当前草稿 `graph_json`

### Requirement: 同步 Debug 运行

系统 SHALL 提供 `POST /workflow/run/debug`，接收 `workflowId`、`inputs`、可选 `kbId`；权限 `workflow:run`。

系统 MUST 阻塞执行至完成或超时（默认 `sync-debug-timeout-ms=60000`），返回 `outputs` 与完整 `steps` Trace。

#### Scenario: Debug 运行成功

- **WHEN** 用户对默认 RAG 模板工作流执行 debug 且知识库有 INDEXED 文档
- **THEN** 响应 `status=SUCCESS`，`steps` 包含每个节点的 inputs/outputs 摘要

#### Scenario: Debug 运行超时

- **WHEN** 执行时间超过同步超时阈值
- **THEN** 运行终态为 FAILED 或 CANCELLED，并返回超时错误信息

### Requirement: 异步运行

系统 SHALL 提供 `POST /workflow/run/async`，立即返回 `{ runId }`；权限 `workflow:run`。

系统 MUST 创建 `wf_run`（`status=QUEUED`），经 `WorkflowRunAsyncExecutor` 后台执行；支持 `GET /workflow/run/getInfo` 与 `GET /workflow/run/list` 查询。

#### Scenario: 异步运行完成

- **WHEN** 客户端提交 async 运行并轮询 getInfo
- **THEN** 终态为 SUCCESS 或 FAILED，且 `wf_run_step` 记录与节点数一致

### Requirement: SSE 流式输出

系统 SHALL 提供 `GET /workflow/run/stream?runId=`（SSE），权限 `workflow:run`。

当 LLM 节点 `streaming=true` 时，系统 MUST 推送事件：`step_start`、`llm_delta`、`step_end`、`done`、`error`、`heartbeat`。

#### Scenario: 收到 LLM 流式 delta

- **WHEN** 客户端对 `stream_enabled=true` 的 run 订阅 SSE
- **THEN** 在 LLM 节点执行期间收到至少一条 `llm_delta` 事件，结束时收到 `done`

### Requirement: 运行 Trace（wf_run / wf_run_step）

系统 MUST 为每次运行创建 `wf_run`，为每个 executed 节点创建 `wf_run_step`，包含：`node_id`、`node_type`、`status`、`inputs_json`、`outputs_json`（脱敏后）、`duration_ms`、`order_no`。

#### Scenario: 失败节点记录错误

- **WHEN** 某节点执行抛出业务异常
- **THEN** 对应 `wf_run_step.status=FAILED` 且 `error_msg` 非空；`wf_run.status=FAILED`

### Requirement: 内置默认 RAG 模板

系统 MUST 提供内置只读模板「默认 RAG 工作流」：`start → knowledge-retrieval → llm → answer`，可通过 `GET /workflow/template/list` 获取。

系统 MUST NOT 修改现有 `POST /knowledge/chat` 的行为或契约。

#### Scenario: 从模板创建工作流

- **WHEN** 用户基于默认 RAG 模板创建新工作流
- **THEN** 新工作流 graph 包含上述四节点线性链路

### Requirement: 功能开关 qc.workflow.enabled

当 `qc.workflow.enabled=false` 时，系统 MUST NOT 注册依赖工作流的 REST 端点（或返回功能未启用错误）。

#### Scenario: 关闭开关后无工作流端点

- **WHEN** 配置 `qc.workflow.enabled=false` 且应用启动成功
- **THEN** `/workflow/**` 路由不可用或返回功能未启用错误

### Requirement: 管理端工作流页面

系统前端 MUST 提供以下页面，遵循 `DESIGN.md` 与列表页模板（默认参照 `views/system/config/index.vue`）：

- `views/workflow/list/index.vue` — 工作流 CRUD 列表
- `views/workflow/design/index.vue` — Vue Flow 设计器（12 种节点、属性面板、Debug/异步运行、Trace/SSE 展示区）
- `views/workflow/run/index.vue` — 运行记录列表与详情

前端 MUST 引入 `@vue-flow/core`（及 background、controls）。

#### Scenario: 登录后可打开设计器

- **WHEN** 用户具备 `workflow:list` 与 `workflow:edit` 权限
- **THEN** 可进入设计器页面并进行节点拖拽与连线

### Requirement: 数据可见性与权限

具备工作流菜单权限的用户 MUST 可查看与运行**全部**工作流；P0 MUST NOT 按部门过滤。维护权限由 `workflow:*` 按钮权限控制。

#### Scenario: 有列表权限可见全部工作流

- **WHEN** 用户具备 `workflow:list`
- **THEN** 分页列表返回系统中全部未删除工作流

### Requirement: 对外 API 预留（P0 不启用）

系统 MUST 创建 `wf_api_key` 表结构，且 `wf_workflow.external_api_enabled` 默认 0；配置 `qc.workflow.external-api.enabled=false` 时 MUST NOT 暴露对外 API Key 鉴权入口。

#### Scenario: 外部 API 开关关闭

- **WHEN** `qc.workflow.external-api.enabled=false`
- **THEN** 不存在无需 Sa-Token 的工作流调用入口
