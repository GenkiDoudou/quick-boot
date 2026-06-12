## Context

QuickBoot `quickboot-knowledge` 模块已实现 RAG 问答（`RagService` + Spring AI `ChatClient` + `QuestionAnswerAdvisor`），但仅依赖向量检索上下文，无法调用外部 MCP 工具。团队目前在 Cursor 通过 `.cursor/mcp.json` 维护 MCP（如 `universal-db-mcp`），缺少后台统一配置、权限与运行时接入。

**已定稿决策**：方案 A — DB 配置 + MCP Java SDK 程序化创建 `McpSyncClient`；支持 STDIO 与远程 HTTP（SSE / Streamable-HTTP）；菜单挂在知识管理下；一次交付 CRUD + 测试 + RAG 接入 + 导出；密钥 SM4 + ENV_REF + 脱敏。

详细领域模型与 API 见 `docs/superpowers/specs/2026-06-07-mcp-management-design.md`。

## Goals / Non-Goals

**Goals:**

- 管理端维护外部 MCP 配置（CRUD、启停、连接测试、导出 `mcp.json`）
- `McpClientManager` 按库表动态建连、缓存与失效，注册为 Spring AI `ToolCallback`
- 知识库绑定 MCP；RAG 问答可选调用绑定工具
- 密钥安全：SM4、`ENV_REF`、列表脱敏

**Non-Goals:**

- MCP Server 托管/部署
- 工作流节点调 MCP（Phase 2 可复用 Manager）
- SSE 流式 RAG
- 与 Cursor 双向同步 `mcp.json`

## Decisions

### 1. 运行时：MCP Java SDK 程序化建连（方案 A）

**选择**：使用 `spring-ai-starter-mcp-client` 传递的 MCP Java SDK（`McpClient.sync` + `StdioClientTransport` / HttpClient Transport），由 `McpClientManager` 按 `kb_mcp_server` 记录组装参数。

**备选**：写回 `application.yml` 触发 Context Refresh — 多实例不一致、热更新风险高，不采用。

**备选**：独立 MCP 代理网关 — 本期过重，不采用。

### 2. 数据模型：三表

- `kb_mcp_server`：主配置（transport、stdio/remote 参数、探测结果）
- `kb_mcp_env`：环境变量行（`PLAIN` / `SECRET` / `ENV_REF`）
- `kb_knowledge_base_mcp`：知识库与 MCP 多对多绑定

`headers_json` 敏感值结构与 env 一致，经 `McpSecretSupport` 处理。

### 3. 密钥：`McpSecretSupport` 对齐 OAuth

复用 `PasswordCodec` SM4（`Oauth2SecretSupport` 模式）。列表/默认详情脱敏；`revealSecrets=true` 需 `knowledge:mcp:query` 并记 operlog。导出默认占位 `${ENV_KEY}`；`includeSecrets=true` 需 `knowledge:mcp:export:secrets`。

### 4. RAG 接入方式

扩展 `KnowledgeChatBo.useMcpTools`（默认 `true`）。有绑定且 `qc.knowledge.mcp.enabled=true` 时，`RagService` 聚合绑定 MCP 的 `ToolCallback` 注入 `ChatClient`，与 `QuestionAnswerAdvisor` 并存。响应增加 `mcpToolsUsed`。

无绑定或 `useMcpTools=false` 时行为与现网纯 RAG 一致。

### 5. STDIO 与远程安全

- STDIO：`max-stdio-processes` 全局限额；可选 `allowed-commands` 白名单；Windows 文档提示 `cmd.exe /c npx` 模式
- 远程 URL：复用知识库/工作流同类 SSRF 策略（禁内网、限重定向、超时）

### 6. 模块与开关

包路径 `io.github.genkidoudou.web.knowledge.mcp.*`，仍在 `quickboot-knowledge` 模块。`qc.knowledge.mcp.enabled=false` 时不注册 `McpClientManager`、MCP Controller；RAG 不加载工具。

### 7. 前端

- 新页 `views/knowledge/mcp/index.vue`（`C7JsonTable`，参照 `system/config`）
- 知识库表单多选 MCP；对话测试 Tab 增加「启用 MCP 工具」开关

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| STDIO 子进程泄漏 | `close()` / 缓存 TTL / 停机钩子 / 并发上限 |
| LLM 滥用 MCP | 系统 Prompt 约束；仅绑定库可用；超时 |
| Spring AI 1.0.0 MCP API 差异 | 以 SDK 程序化建连为准；连接测试 + 单测 |
| Windows/Linux stdio 差异 | 表单说明与示例模板 |
| 一次交付面大 | 按 tasks：Flyway → Manager → API → RAG → 前端 |

## Migration Plan

1. Flyway `V62__knowledge_mcp.sql` 建表 + 菜单（parent `2280`，menu_id `2301+`）
2. `application.yml` 增加 `qc.knowledge.mcp` 默认块
3. 无历史数据回填；未绑定 MCP 的知识库 RAG 行为不变
4. 回滚：停用 `qc.knowledge.mcp.enabled` 或还原 Flyway（需评估已存配置）

## Open Questions

（无。方案 A 与 superpowers 设计文档已评审确认。）
