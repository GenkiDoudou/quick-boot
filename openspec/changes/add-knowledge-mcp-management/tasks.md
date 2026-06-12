## 1. 依赖与配置

- [x] 1.1 `quickboot-knowledge/pom.xml` 增加 `spring-ai-starter-mcp-client` 依赖
- [x] 1.2 新增 `KnowledgeMcpProperties`（`qc.knowledge.mcp.*`）并纳入 `KnowledgeProperties` 或独立 `@ConfigurationProperties`
- [x] 1.3 `application.yml` / `application-dev.yml` 增加 `qc.knowledge.mcp` 默认配置块
- [x] 1.4 新增 `@ConditionalOnProperty(qc.knowledge.mcp.enabled)` 自动配置类骨架
- [x] 1.5 验证 `mvn -pl quickboot-web -am clean compile -DskipTests` 通过

## 2. 数据库与权限

- [x] 2.1 Flyway `V62__knowledge_mcp.sql`：创建 `kb_mcp_server`、`kb_mcp_env`、`kb_knowledge_base_mcp`（含注释、索引、唯一约束）
- [x] 2.2 Flyway 插入「MCP 管理」菜单（parent `2280`，menu_id `2301+`）及 `knowledge:mcp:*` 按钮权限（含 `export:secrets`）
- [x] 2.3 实现 Entity/Mapper：`KbMcpServer`、`KbMcpEnv`、`KbKnowledgeBaseMcp`

## 3. 领域模型与密钥

- [x] 3.1 实现枚举：`McpTransport`、`McpEnvValueType`、`McpTestStatus`
- [x] 3.2 实现 Bo/Vo/QueryBo（含 env 列表、脱敏 VO）及 Jakarta Validation 分组
- [x] 3.3 实现 `McpSecretSupport`（对齐 `Oauth2SecretSupport`：SM4 加解密、ENV_REF 解析）
- [x] 3.4 实现 headers_json 敏感值编解码辅助

## 4. MCP 客户端运行时（方案 A）

- [x] 4.1 实现 `McpTransportFactory`：按 transport 组装 STDIO / SSE / Streamable-HTTP Transport
- [x] 4.2 实现 `McpClientManager`：缓存、TTL、evict、STDIO 并发限额、远程 URL SSRF 校验
- [x] 4.3 实现 `McpConnectionTester`：`initialize` + `listTools` + 更新 `last_test_*`
- [x] 4.4 实现 `McpToolCallbackProvider`：聚合多 MCP 的 Spring AI `ToolCallback`
- [x] 4.5 单测：`McpSecretSupport`、SSRF 校验、STDIO 命令白名单（可 mock）

## 5. MCP 管理 API

- [x] 5.1 实现 `KbMcpServerService`（分页、详情 revealSecrets、增删改、options 下拉）
- [x] 5.2 实现 `KbMcpServerController`（`/knowledge/mcp/**`）及 OpenAPI 注解
- [x] 5.3 实现连接测试接口 `POST /test`
- [x] 5.4 实现导出接口 `GET /export`（默认占位符；`includeSecrets` 权限校验）
- [x] 5.5 配置变更/删除时调用 `McpClientManager.evict`

## 6. 知识库绑定与 RAG 接入

- [x] 6.1 扩展 `KbKnowledgeBase` Service/Bo/Vo：维护 `mcpIds` 与 `kb_knowledge_base_mcp`
- [x] 6.2 扩展 `KnowledgeChatBo`（`useMcpTools`）与 `KnowledgeChatVo`（`mcpToolsUsed`）
- [x] 6.3 改造 `RagService.ask`：绑定 MCP 时注入 `ToolCallback`，补充系统 Prompt
- [x] 6.4 删除知识库时级联清理 `kb_knowledge_base_mcp` 绑定

## 7. 前端

- [x] 7.1 新增 `api/knowledge/mcp.js` 封装全部 MCP 接口
- [x] 7.2 新增 `views/knowledge/mcp/index.vue`（C7JsonTable + 三 Tab 表单 + 测试/导出）
- [x] 7.3 改造知识库表单：MCP 多选（`/knowledge/mcp/options`）
- [x] 7.4 改造文档管理对话测试 Tab：`useMcpTools` 开关 + `mcpToolsUsed` 展示
- [x] 7.5 验证 `pnpm build:prod` 通过

## 8. 联调与验收

- [ ] 8.1 手工验收：STDIO MCP 新增 → 测试成功 → 导出 JSON
- [ ] 8.2 手工验收：知识库绑定 MCP → RAG 问答返回 `mcpToolsUsed`（或纯 RAG 降级）
- [ ] 8.3 手工验收：`qc.knowledge.mcp.enabled=false` 时 MCP 端点不可用
- [ ] 8.4 对照 spec 测试要点 TC_MCP_001–041 记录结果
