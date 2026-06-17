## Context

- 依据：`docs/superpowers/specs/2026-06-15-ai-agent-app-design.md`（brainstorming 定稿 1:B–7:A）。
- 现状：已有 `quickboot-workflow`、`quickboot-knowledge`、`quickboot-ai`；无 AI 应用域。
- 约束：不修改 `/knowledge/chat`；`qc.ai-app.enabled` 默认 false；嵌入仅已发布应用。

## Goals / Non-Goals

**Goals:**

- 新建 `quickboot-ai-app` 模块，交付智能体 + 高级编排两种应用类型。
- 智能体：Tool Calling（知识库 + 工作流）、变量记忆、三栏编排 UI。
- 共用：多会话、SSE 聊天、发布（演示/嵌入/菜单）、千问联网搜索。

**Non-Goals:**

- 长期记忆库、绘画、第三方渠道、插件市场、AI 一键创建

## Decisions

### D1：独立模块 `quickboot-ai-app`（采用）

- **理由**：会话/发布/嵌入与 workflow DAG 职责分离；对齐 `quickboot-workflow` 模式。
- **备选**：塞入 workflow 模块 — 拒绝，智能体非 DAG 语义。

### D2：智能体流程调用 — Spring AI ToolCallback（采用）

- 每个 `workflowBindings` 注册一个 Tool；模型通过 `toolName` + `description` 选择。
- 内部调用 `WorkflowEngine` + 已发布 `graph_json`；入参 `{ "query": string }` 映射 start 节点。
- **参考**：`RagService` + `McpToolCallbackProvider` 已有 Tool 循环模式。

### D3：知识库 — 固定 Tool `search_knowledge`

- 按 `kbIds` 调 `KnowledgeSearchService`；citations 写入 message metadata。

### D4：变量记忆 — 会话级 `variables_json` + 提示词注入

- 非长期记忆库；发布配置声明 `memoryVariables`；每轮后轻量抽取更新。
- **备选**：独立记忆服务 — P1。

### D5：高级编排 — 直接 WorkflowEngine，无 Tool Calling

- `app_type=workflow` 绑定单个 `workflowId`；消息 → `inputs.query` → SSE/同步运行。

### D6：嵌入鉴权 — `embed_token` + `allowed_origins`

- 公开路径 `/ai/embed/{token}/**`；访客 `user_key` 存 localStorage UUID。
- 管理端演示仍走 `Sa-Token`。

### D7：千问联网 — ChatOptions 扩展参数

- 仅当模型 provider 为 dashscope/千问且用户勾选 `webSearch` 时注入；其他模型忽略该开关。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| Tool 循环超时/费用 | 上限 5 次 tool call；工作流同步超时对齐 `syncDebugTimeoutMs` |
| 工作流 start inputs 字段不一致 | 文档约定默认 `query`；引擎按 key 模糊匹配 |
| 嵌入滥用 | token + 域名白名单；仅 published 应用 |
| 变量抽取不准 | P0 规则+轻量 LLM 抽取；允许人工在会话重置变量 |

## Migration Plan

- Flyway 新增 4 表；`qc.ai-app.enabled=true` 后注册菜单与权限。
- 回滚：关开关；表保留（无破坏性变更）。

## Open Questions

（无。brainstorming 已闭合。）
