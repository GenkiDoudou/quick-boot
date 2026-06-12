## Why

`quickboot2` 已完成知识库 RAG（语义检索、固定链路问答），但缺少 **Dify / Coze 式可视化工作流**能力：业务方无法自行编排「检索 → 分支 → LLM → HTTP → 回答」等多步链路，也无法调试 Trace 或发布版本。在知识库能力就绪后，引入工作流引擎是提升 AI 应用可配置性与可观测性的自然下一步。

## What Changes

- 新增 Maven 模块 **`quickboot-workflow`**，实现 DAG 工作流 DSL、执行引擎、`NodeHandlerRegistry` 与 12 种核心节点（不含 Code 节点）。
- 新增 MySQL 表：`wf_workflow`、`wf_workflow_version`、`wf_run`、`wf_run_step`；预留 `wf_api_key`（P0 不启用）。
- 新增 REST API（前缀 `/workflow/**`）：定义 CRUD、草稿保存/发布、同步 Debug 运行、异步运行、SSE 流式、运行 Trace 查询。
- 新增 `qc.workflow.*` 功能开关；`qc.workflow.enabled=false` 时不注册工作流 Bean。
- 新增管理端三页：**工作流列表 / Vue Flow 设计器 / 运行记录**；内置「默认 RAG」只读模板。
- **`knowledge-retrieval` 节点**复用 `quickboot-knowledge` 的 `KnowledgeSearchService`；**不修改**现有 `/knowledge/chat` 行为。
- P0 **不做**：Code 节点、公网 Bot 广场、对外 API Key 调用（仅表结构与配置预留）、按部门 ACL。

## Capabilities

### New Capabilities

- `workflow-engine`：工作流定义与版本、12 种节点 Handler、DAG 执行引擎、同步 Debug / 异步 Run / SSE 流式、运行 Trace、Vue Flow 设计器与管理端页面

### Modified Capabilities

（无。知识库 `knowledge-rag` 规范级行为不变；工作流通过 Handler 调用其 Service，不修改既有 API 契约。）

## Impact

- **后端**：
  - 新增 `quickboot-workflow` 模块；`quickboot-web` 引入依赖；`quickboot-knowledge` 保持独立（workflow → knowledge 单向依赖）。
  - Flyway：工作流业务表 + 菜单/权限种子数据。
  - 复用 Spring AI `ChatModel`（LLM / 分类 / 参数抽取节点）；HTTP 节点 SSRF 策略对齐 `qc.knowledge.web-fetch`。
- **前端**：`quick-ui` 新增 `@vue-flow/core` 依赖；`views/workflow/` 三页与 `api/workflow/` 封装。
- **配置**：`application*.yml` 增加 `qc.workflow.*`。
- **部署**：依赖既有 Ollama/DeepSeek + PGVector（与 knowledge 模块相同）；`qc.workflow.enabled=false` 时可跳过 Bean 注册以便 CI 构建。
