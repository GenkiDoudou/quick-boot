## ADDED Requirements

### Requirement: AI 应用模块开关

当 `qc.ai-app.enabled=false`（默认）时，系统 MUST NOT 注册 `quickboot-ai-app` 的 Controller/Service Bean 及前端菜单。

当 `qc.ai-app.enabled=true` 时，系统 MUST 注册完整 AI 应用能力。

#### Scenario: 开关关闭

- **WHEN** 配置 `qc.ai-app.enabled=false`
- **THEN** `/ai/app/**` 接口不可用或未注册

### Requirement: AI 应用数据模型

系统 MUST 持久化以下实体：

| 表 | 用途 |
|----|------|
| `ai_app` | 应用定义、类型、草稿/发布配置 |
| `ai_app_session` | 会话（含 `variables_json`） |
| `ai_app_message` | 消息（user/assistant/tool） |
| `ai_app_publish` | 嵌入 token、域名白名单、菜单元数据 |

`ai_app.app_type` MUST 为 `agent` 或 `workflow`。

`ai_app.status` MUST 为 `draft` 或 `published`；发布时 MUST 将 `config_json` 快照至 `published_config_json`。

#### Scenario: 发布快照

- **WHEN** 管理员对草稿应用执行发布
- **THEN** `status=published` 且 `published_config_json` 与当时 `config_json` 一致

### Requirement: AI 应用 CRUD API

系统 MUST 提供 `/ai/app/**` REST API（`@PostMapping` 表达写操作），包含：

- 分页列表、详情、新增、更新、逻辑删除
- 发布

请求/响应字段 MUST 使用 camelCase。接口 MUST 接入 Jakarta Validation 与 `Sa-Token` 权限 `aiapp:*`。

#### Scenario: 无权限拒绝

- **WHEN** 用户无 `aiapp:edit` 调用更新接口
- **THEN** 返回鉴权失败

### Requirement: 智能体配置模型

`app_type=agent` 的 `config_json` MUST 支持：

| 字段 | 说明 |
|------|------|
| `chatModelId` | 必填，关联 AI 模型 |
| `systemPrompt` | 人设与回复逻辑 |
| `openingMessage` | 开场白 |
| `suggestedQuestions` | 预设问题 |
| `quickCommands` | 快捷指令 `{ label, prompt }` |
| `kbIds` | 知识库 ID 列表 |
| `workflowBindings` | `{ workflowId, toolName, description }[]` |
| `memoryVariables` | `{ key, description, defaultValue? }[]` |
| `historyTurns` | 保留历史轮数，默认 10 |
| `multiSession` | 是否多会话 |

#### Scenario: 智能体缺模型

- **WHEN** `chatModelId` 为空保存
- **THEN** 校验失败或发布被拒绝

### Requirement: 高级编排配置模型

`app_type=workflow` 的 `config_json` MUST 支持：

| 字段 | 说明 |
|------|------|
| `workflowId` | 必填，已存在的工作流 ID |
| `openingMessage` | 开场白 |
| `suggestedQuestions` | 预设问题 |
| `multiSession` | 是否多会话 |

MUST NOT 使用 Tool Calling 或 `memoryVariables`。

#### Scenario: 绑定工作流

- **WHEN** 配置有效 `workflowId` 并发布
- **THEN** 聊天时触发该工作流已发布版本

### Requirement: 智能体 Tool Calling 运行时

对 `app_type=agent` 的用户消息，系统 MUST：

1. 加载 `published_config_json`（或编排预览时的草稿）
2. 组装 system prompt（含 `memoryVariables` 当前值说明）
3. 注册 Tool：`search_knowledge`（当 `kbIds` 非空）及每个 `workflowBindings` 一项
4. 执行 ChatClient tool 循环（上限 5 次）
5. 工作流 Tool：内部运行已发布工作流，入参 `{ "query": "<string>" }`，返回 answer/end 输出摘要
6. 知识库 Tool：调用 `KnowledgeSearchService`，返回检索文本
7. 持久化 assistant/tool 消息；SSE 推送 token 增量

#### Scenario: 模型选择工作流 Tool

- **WHEN** 用户问题匹配某 `workflowBindings.description` 语义
- **THEN** 系统执行对应工作流并将结果纳入最终回复

#### Scenario: 知识库检索

- **WHEN** 用户问题需要知识库内容且配置了 `kbIds`
- **THEN** 触发 `search_knowledge` 且 metadata 含 citations

### Requirement: 会话变量记忆

对 `app_type=agent`，系统 MUST 在 `ai_app_session.variables_json` 存储 `memoryVariables` 声明的 key 值。

每轮对话结束后 MUST 尝试从对话更新变量；下一轮 system prompt MUST 注入当前变量值。

`app_type=workflow` MUST NOT 写入 `variables_json`。

#### Scenario: 变量跨轮保留

- **WHEN** 用户首轮告知姓名且 `memoryVariables` 含 `user_name`
- **THEN** 次轮对话 system prompt 含已提取的 `user_name` 值

### Requirement: 高级编排聊天运行时

对 `app_type=workflow`，系统 MUST 将用户消息作为工作流 start 入参 `query` 执行已发布图。

系统 SHOULD 优先 SSE 流式返回；失败时 MAY 回退同步。

MUST 在 message metadata 记录 `runId`。

#### Scenario: 工作流端到端

- **WHEN** 用户发送消息
- **THEN** 返回工作流 end/answer 节点产出文本

### Requirement: 多会话

当 `multiSession=true`，系统 MUST 支持同一 `user_key` 下多个 `ai_app_session`；会话列表按 `update_time` 降序。

用户 MUST 仅能访问自己的会话（`user_key` 隔离）。

#### Scenario: 新建会话

- **WHEN** 用户点击新建会话
- **THEN** 创建空 session 且不影响其他 session 历史

### Requirement: 千问联网搜索

当所选 Chat 模型为千问（DashScope 兼容）且客户端请求 `webSearch=true` 时，系统 MUST 向模型请求注入联网搜索参数。

非千问模型 MUST 忽略 `webSearch` 开关且不报错。

#### Scenario: 千问开启联网

- **WHEN** 千问模型且 `webSearch=true`
- **THEN** 模型调用含联网扩展参数

### Requirement: 发布与嵌入 API

系统 MUST 支持：

- 管理端演示聊天（`Sa-Token`，可用草稿或已发布配置——预览用草稿）
- `ai_app_publish`：`embed_token`、`allowed_origins`
- 公开路径 `/ai/embed/{token}/chat/stream`：校验 token 与 Origin；仅 `status=published` 应用

嵌入访客 MUST 使用 `user_key`（客户端生成 UUID）区分会话。

#### Scenario: 未发布不可嵌入

- **WHEN** 应用为 draft 且访问 embed URL
- **THEN** 拒绝并返回明确错误

#### Scenario: 域名白名单

- **WHEN** 请求 Origin 不在 `allowed_origins`
- **THEN** 拒绝嵌入聊天

### Requirement: SSE 聊天协议

`/ai/app/chat/stream` 与嵌入等价接口 MUST 使用 SSE，事件类型至少包含：

| 事件 | 说明 |
|------|------|
| `delta` | 文本增量 |
| `tool_call` | 工具调用开始/结束（含 name、status） |
| `done` | 本轮结束（含 messageId、metadata） |
| `error` | 失败原因 |

#### Scenario: 流式成功

- **WHEN** 智能体正常回复
- **THEN** 客户端先收到若干 `delta` 后收到 `done`
