## Context

QuickBoot 已通过 Spring AI 集成 Chat / Embedding（`quickboot-knowledge` RAG、`quickboot-workflow` LLM 节点），但模型配置分散在 `application*.yml`，运行时注入单一 `ChatModel` / `EmbeddingModel` Bean。MCP 管理（`add-knowledge-mcp-management`）已定稿 DB 台账 + 动态建连模式；大模型管理采用对称方案，并升格为 **「AI 能力」** 菜单域。

**已定稿决策**（见 `docs/superpowers/specs/2026-06-07-ai-model-management-design.md`）：方案 A — 新建 `quickboot-ai` + `AiModelRegistry` 程序化建连；OpenAI 兼容 + Ollama；Chat + Embedding；全局默认 + KB/WF 绑定；SM4 + ENV_REF；MCP 迁至 `/ai/mcp` 与 `ai:mcp:*`。

## Goals / Non-Goals

**Goals:**

- 管理端维护大模型配置（CRUD、启停、连接测试、设默认、导出 YAML/env）
- `AiModelRegistry` / `AiModelResolver` 按库表动态创建 Spring AI 模型实例
- 知识库绑定 Chat/Embedding；工作流绑定 Chat；解析链：资源绑定 → 全局 default_slot → YAML 兜底
- 新建「AI 能力」菜单；MCP 管理迁入
- Embedding 维度与 `qc.knowledge.vectorDimensions` 校验

**Non-Goals:**

- 微调/训练、用量计费、Prompt 模板管理、流式配置台
- `jeecg.jmreport.ai` 纳入 Registry
- 切换 Embedding 后自动重建索引（仅 UI 提示）

## Decisions

### 1. 模块边界：新建 `quickboot-ai`

**选择**：独立 Maven 模块，包路径 `io.github.genkidoudou.web.ai.*`；`quickboot-web`、`quickboot-knowledge`、`quickboot-workflow` 均依赖它。

**备选**：放入 `quickboot-knowledge` — 工作流也需 Chat，职责混杂，不采用。

**备选**：放入 `quickboot-core` — 引入 Spring AI 重依赖，不采用。

### 2. 运行时：程序化建连（方案 A）

**选择**：`AiModelFactory` 按 `ai_model` 记录构建 `OpenAiChatModel` / `OllamaChatModel` / 对应 EmbeddingModel；`AiModelRegistry` 缓存与 evict。

**备选**：写回 YAML + Context Refresh — 多实例不一致，不采用。

### 3. 解析优先级（5D）

```
resolveChat(kbId?):     kb.chat_model_id → default_slot=CHAT → yaml Bean
resolveEmbedding(kbId?): kb.embedding_model_id → default_slot=EMBEDDING → yaml Bean
resolveWorkflowChat(wfId): wf.chat_model_id → WORKFLOW_CHAT → CHAT → yaml Bean
```

`qc.ai.registry.fallback-to-yaml=true`（默认）保证 DB 无配置时与现网一致。

### 4. 数据模型

- `ai_model`：主表（provider、model_type、api_key、default_slot、last_test_*）
- `kb_knowledge_base` 增 `chat_model_id`、`embedding_model_id`
- `wf_workflow` 增 `chat_model_id`
- Flyway **V63**（若已占用则顺延）

### 5. 密钥：`AiSecretSupport` 对齐 MCP

复用 `PasswordCodec` SM4（`McpSecretSupport` 模式）。`api_key_type`：`PLAIN` / `SECRET` / `ENV_REF`。

### 6. 菜单与 MCP 迁移

- 新建一级菜单「AI 能力」（menu_id `2320`）
- 大模型管理（`2321`）、MCP 管理（`2301` parent 改为 `2320`）
- MCP API：`/knowledge/mcp` → **`/ai/mcp`**；权限 `knowledge:mcp:*` → **`ai:mcp:*`**
- 前端 `/knowledge/mcp` redirect → `/ai/mcp`

### 7. 消费者改造

| 模块 | 改造 |
|------|------|
| `KnowledgeAiGuard` | 委托 `AiModelResolver` |
| `RagService` | 按 `kbId` 解析 ChatModel |
| 入库/检索/分块 | 按 `kbId` 解析 EmbeddingModel |
| `WorkflowAiGuard` / LLM 等节点 | 按 `workflowId` 解析 ChatModel |

KB 修改 `embedding_model_id` 时前端 confirm 提示重建索引。

### 8. 开关

`qc.ai.enabled=false` 时不注册 Registry 与 `/ai/model/**`；消费者可回落 YAML Bean。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| KB 切换 Embedding 后向量不可比 | UI 警告 + 文档说明需 re-index |
| Spring AI 1.0.0 API 差异 | 以 BOM 1.0.0 为准；Factory 集成测试 |
| MCP 路径/权限迁移破坏集成 | Flyway 更新 perms；前端 redirect；短期可选双前缀 |
| 一次交付面大 | 按 tasks：模块 → Registry → API → 消费者 → 菜单/MCP → 前端 |

## Migration Plan

1. Flyway V63：建 `ai_model`、扩展 KB/WF、新菜单、MCP 菜单/perms 迁移
2. `application.yml` 增加 `qc.ai` 默认块
3. 现有 YAML 配置保留作 fallback；可选管理页「从 YAML 导入」生成草稿（ENV_REF，无 Flyway 种子）
4. 回滚：关闭 `qc.ai.enabled` 或清空 DB 默认，回落 YAML Bean

## Open Questions

- 无（brainstorming 已定稿）
