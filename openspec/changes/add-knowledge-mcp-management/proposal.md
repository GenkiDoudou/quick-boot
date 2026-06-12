## Why

QuickBoot 知识库已支持 RAG 问答，但无法连接外部 **Model Context Protocol (MCP)** 工具；团队只能在 Cursor 侧手工维护 `.cursor/mcp.json`，缺少统一台账、权限管控与业务运行时接入。需要在知识管理模块内提供 MCP 配置管理、连接测试、导出及 RAG 对话中的工具调用能力，采用 **方案 A**（DB 配置 + MCP Java SDK 动态建连）。

## What Changes

- 新增 MySQL 表：`kb_mcp_server`、`kb_mcp_env`、`kb_knowledge_base_mcp`（Flyway V62）。
- 新增 `quickboot-knowledge` 内 MCP 子域：`McpClientManager`、CRUD/测试/导出 API（前缀 `/knowledge/mcp`）。
- 支持 **STDIO** 与 **远程 HTTP**（SSE / Streamable-HTTP）两种传输；密钥 SM4 加密、环境变量引用、列表脱敏。
- 知识库可绑定多个启用的 MCP；RAG 问答扩展 `useMcpTools` 与 `mcpToolsUsed` 响应字段。
- 新增管理端 **MCP 管理** 页（知识管理菜单下）；知识库表单增加 MCP 多选绑定。
- 新增 `qc.knowledge.mcp.*` 配置；`quickboot-knowledge` 引入 `spring-ai-starter-mcp-client`。
- 导出 Cursor / Claude Desktop 兼容的 `mcp.json` 片段。

## Capabilities

### New Capabilities

- `knowledge-mcp`：外部 MCP 配置 CRUD、连接测试、密钥安全、导出、动态客户端管理与知识库绑定

### Modified Capabilities

- `knowledge-rag`：RAG 问答支持可选 MCP 工具调用；知识库 CRUD 支持 `mcpIds` 绑定（delta spec）

## Impact

- **后端**：`quickboot-knowledge` 新增 `mcp` 包与依赖；`RagService` / `KnowledgeChatBo` / 知识库 Service 扩展；Flyway V62 菜单权限种子。
- **前端**：`quick-ui` 新增 `views/knowledge/mcp/`、`api/knowledge/mcp.js`；改造知识库表单与对话测试 Tab。
- **配置**：`application.yml` 增加 `qc.knowledge.mcp`；`qc.knowledge.mcp.enabled=false` 时 MCP Bean 与端点不注册。
- **安全**：STDIO 子进程限额与命令白名单；远程 URL SSRF 防护；敏感操作记 operlog。
- **部署**：STDIO 型 MCP 需运行环境具备 `npx`/Node 等；远程 MCP 需网络可达。
