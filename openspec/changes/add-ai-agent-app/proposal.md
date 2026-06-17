## Why

项目已具备工作流引擎、知识库 RAG 与 AI 模型管理，但缺少面向终端用户的 **AI 应用 / 智能体** 能力：无法像扣子一样配置人设与技能、在对话中自主调用流程、多会话聊天并发布到演示/嵌入/系统菜单。在 workflow 与 knowledge 能力就绪后，补齐智能体应用是自然下一步。

## What Changes

- 新增 Maven 模块 **`quickboot-ai-app`**：应用定义、会话、消息、发布配置、智能体 Tool Calling 运行时。
- 新增 MySQL 表：`ai_app`、`ai_app_session`、`ai_app_message`、`ai_app_publish`。
- 新增 REST API（`/ai/app/**`）：应用 CRUD、发布、SSE 对话、会话/消息、嵌入配置。
- 新增管理端页面：应用列表、智能体三栏编排、高级编排配置、演示聊天、嵌入页。
- **智能体模式**：提示词 + 知识库 Tool + 关联工作流 Tool（Function Calling）+ 会话变量记忆。
- **高级编排模式**：绑定已发布工作流，用户消息触发 DAG 运行（SSE 优先）。
- **发布**：管理端演示、iframe/script 嵌入（token + 域名白名单）、系统菜单挂载元数据。
- **多会话** + **千问联网搜索**（聊天页开关）。
- 功能开关 `qc.ai-app.enabled`（默认 false）。

**非本期**：长期记忆库、AI 绘画、第三方渠道（飞书/微信）、插件市场、智能体商店。

## Capabilities

### New Capabilities

- `ai-app-engine`：后端模块、数据表、应用 CRUD/发布、智能体与高级编排运行时、Tool Calling、嵌入公开 API、权限与开关。
- `ai-app-ui`：应用列表、智能体三栏编排、高级编排页、多会话聊天、发布弹窗、嵌入页 UI。

### Modified Capabilities

（无。工作流与知识库对外契约不变；智能体通过内部 Service 调用 `WorkflowEngine` 与 `KnowledgeSearchService`。）

## Impact

- **后端**：新增 `quickboot-ai-app`；`quickboot-web` 引入依赖；Flyway 迁移 4 张表。
- **前端**：`quick-ui/src/views/ai/app/` 新页面与 API 封装；路由与菜单配置。
- **依赖**：`ai-app` → `workflow`、`knowledge`、`ai`（单向）。
- **权限**：新增 `aiapp:*` 权限码与菜单项。
- **兼容性**：纯新增能力；`qc.ai-app.enabled=false` 时不影响现有模块。
