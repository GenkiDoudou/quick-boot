## Why

QuickBoot 已通过 Spring AI 使用 Chat / Embedding（知识库 RAG、工作流 LLM 节点），但模型配置分散在 `application*.yml`，运行时仅注入单一 Bean，无法按知识库或工作流选择模型，变更需改 YAML 并重启。需要在管理端提供统一大模型台账、连接测试、全局默认与业务绑定，并与 MCP 管理一并升格为 **「AI 能力」** 菜单域。

## What Changes

- 新建 Maven 模块 **`quickboot-ai`**：`AiModelRegistry` / `AiModelFactory` / `AiModelResolver` 按 DB 配置程序化创建 Spring AI 模型实例（方案 A）。
- 新增 MySQL 表 **`ai_model`**（Flyway V63）；扩展 **`kb_knowledge_base`**（`chat_model_id`、`embedding_model_id`）、**`wf_workflow`**（`chat_model_id`）。
- 新增大模型 CRUD / 连接测试 / 设默认 / 导出（YAML、env）API，前缀 **`/ai/model`**；权限 **`ai:model:*`**。
- 支持 **OpenAI 兼容** 与 **Ollama** 两种 Provider；Chat + Embedding；API Key SM4 + ENV_REF + 列表脱敏。
- 知识库 RAG、入库/检索与工作流 LLM/分类/参数抽取改为经 **`AiModelResolver`** 解析模型（全局默认 → 资源绑定 → YAML 兜底）。
- 新建 **「AI 能力」** 一级菜单；新增大模型管理页；**MCP 管理迁入** 该菜单。
- **BREAKING**：MCP API 前缀由 `/knowledge/mcp` 迁至 **`/ai/mcp`**；MCP 权限由 `knowledge:mcp:*` 改为 **`ai:mcp:*`**（前端旧路由 redirect）。
- 新增 `qc.ai.*` 配置；可选「从 YAML 导入」预置模型草稿。

## Capabilities

### New Capabilities

- `ai-model`：大模型配置 CRUD、连接测试、全局默认 slot、密钥安全、导出、Registry 运行时与维度校验

### Modified Capabilities

- `knowledge-rag`：知识库支持 `chatModelId` / `embeddingModelId` 绑定；RAG / 入库 / 检索经 Resolver 解析模型
- `knowledge-mcp`：API 前缀与权限前缀迁移至 `ai:mcp` 域；菜单迁至「AI 能力」
- `workflow-engine`：工作流支持 `chatModelId` 绑定；LLM 等节点经 Resolver 解析 ChatModel

## Impact

- **后端**：新建 `quickboot-ai`；`quickboot-web` / `quickboot-knowledge` / `quickboot-workflow` 依赖调整；Flyway V63 菜单与权限；MCP Controller 路径变更。
- **前端**：`views/ai/model/`、`views/ai/mcp/`；知识库/工作流表单增加模型选择；路由 `/ai/*`。
- **配置**：`application.yml` 增加 `qc.ai`；`qc.ai.enabled=false` 时 Registry 不注册，可回落 YAML Bean。
- **兼容**：`qc.ai.registry.fallback-to-yaml=true` 时 DB 无默认与现网行为一致；KB 切换 Embedding 需 UI 提示重建索引。
- **参考设计**：`docs/superpowers/specs/2026-06-07-ai-model-management-design.md`
