## MODIFIED Requirements

### Requirement: 工作流定义与版本（wf_workflow / wf_workflow_version）

系统 MUST 在 MySQL 中持久化工作流定义，主表 `wf_workflow` 至少包含：`workflow_id`、`name`、`description`、`status`（`DRAFT`/`PUBLISHED`/`DISABLED`）、`published_version_id`、`chat_model_id`（可选，NULL 表示使用 WORKFLOW_CHAT 全局默认 → 再回落 CHAT 默认）、`bot_enabled`（默认 0）、`external_api_enabled`（默认 0）、标准审计字段与 `deleted` 逻辑删除。

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

#### Scenario: 工作流绑定专用 Chat 模型
- **WHEN** 工作流配置了 `chatModelId` 且该模型启用
- **THEN** LLM / 分类 / 参数抽取节点 MUST 经 `AiModelResolver.resolveWorkflowChat(workflowId)` 使用该 ChatModel

### Requirement: 十二种节点 Handler

系统 MUST 注册并实现以下节点类型（`NodeHandlerRegistry`），且每种节点 MUST 具备 `validateSchema()` 与 `execute()`：

| type | 说明 |
|------|------|
| `start` | 定义运行入参 |
| `answer` | 终止并组装最终响应 |
| `llm` | 调用 Spring AI ChatModel（经 `AiModelResolver` 按 workflowId 解析） |
| `knowledge-retrieval` | 调用 KnowledgeSearchService 语义检索 |
| `if-else` | 条件分支（eq/ne/contains/gt 等） |
| `template-transform` | 文本模板拼接 |
| `variable-assign` | 变量赋值 |
| `variable-aggregator` | 多路变量合并 |
| `http-request` | HTTP 调用（含 SSRF 防护） |
| `question-classifier` | LLM 意图分类（经 Resolver 解析 ChatModel） |
| `parameter-extractor` | LLM 结构化参数抽取（经 Resolver 解析 ChatModel） |
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

`/add`、`/update`、`/getInfo` MUST 支持可选字段 `chatModelId`。

#### Scenario: 获取工作流含草稿 graph
- **WHEN** 用户调用 `GET /workflow/getInfo?workflowId=1`
- **THEN** 响应包含元数据、当前草稿 `graph_json` 及 `chatModelId`

### Requirement: 管理端工作流页面

系统前端 MUST 提供以下页面，遵循 `DESIGN.md` 与列表页模板（默认参照 `views/system/config/index.vue`）：

- `views/workflow/list/index.vue` — 工作流 CRUD 列表（含 Chat 模型选择）
- `views/workflow/design/index.vue` — Vue Flow 设计器（12 种节点、属性面板、Debug/异步运行、Trace/SSE 展示区）
- `views/workflow/run/index.vue` — 运行记录列表与详情

前端 MUST 引入 `@vue-flow/core`（及 background、controls）。

#### Scenario: 登录后可打开设计器
- **WHEN** 用户具备 `workflow:list` 与 `workflow:edit` 权限
- **THEN** 可进入设计器页面并进行节点拖拽与连线
