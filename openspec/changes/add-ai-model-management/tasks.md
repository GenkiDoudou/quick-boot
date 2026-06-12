## 1. 模块与配置



- [x] 1.1 新建 Maven 模块 `quickboot-ai`（pom、父工程 module 声明、`quickboot-web` / `quickboot-knowledge` / `quickboot-workflow` 依赖）

- [x] 1.2 `quickboot-ai/pom.xml` 引入 `spring-ai-starter-model-openai`、`spring-ai-starter-model-ollama`

- [x] 1.3 新增 `AiProperties`（`qc.ai.*`）及 `@ConditionalOnProperty(qc.ai.enabled)` 自动配置骨架

- [x] 1.4 `application.yml` / `application-dev.yml` 增加 `qc.ai` 默认配置块

- [x] 1.5 验证 `mvn -pl quickboot-web -am clean compile -DskipTests` 通过



## 2. 数据库与权限



- [x] 2.1 Flyway `V63__ai_model_management.sql`：创建 `ai_model` 表（含注释、索引、唯一约束）

- [x] 2.2 Flyway 扩展 `kb_knowledge_base`（`chat_model_id`、`embedding_model_id`）与 `wf_workflow`（`chat_model_id`）

- [x] 2.3 Flyway 新建「AI 能力」菜单（2320）、「大模型管理」（2321+）及 `ai:model:*` 按钮权限

- [x] 2.4 Flyway 迁移 MCP 菜单至「AI 能力」、`perms` 更新为 `ai:mcp:*`

- [x] 2.5 实现 Entity/Mapper：`AiModel`；扩展 KB/WF Entity 与 Mapper



## 3. 领域模型与密钥



- [x] 3.1 实现枚举：`AiModelType`、`AiProvider`、`AiApiKeyType`、`AiDefaultSlot`、`AiTestStatus`

- [x] 3.2 实现 Bo/Vo/QueryBo 及 Jakarta Validation 分组

- [x] 3.3 实现 `AiSecretSupport`（对齐 `McpSecretSupport`：SM4、ENV_REF、脱敏、keep-existing）

- [x] 3.4 实现 Embedding 维度校验（对比 `qc.knowledge.vectorDimensions`）



## 4. 模型 Registry 运行时



- [x] 4.1 实现 `AiModelFactory`：OPENAI_COMPAT / OLLAMA 构建 ChatModel / EmbeddingModel

- [x] 4.2 实现 `AiModelRegistry`：缓存、TTL、evict

- [x] 4.3 实现 `AiModelResolver`：KB/WF 绑定 → default_slot → YAML fallback 解析链

- [x] 4.4 实现 `AiModelConnectionTester`：Chat probe / Embedding 维度验证 + 更新 `last_test_*`

- [x] 4.5 单测：`AiSecretSupport`、维度校验、Resolver 回落逻辑（可 mock）



## 5. 大模型管理 API



- [x] 5.1 实现 `AiModelService`（分页、详情 revealSecrets、增删改、setDefault/clearDefault、options）

- [x] 5.2 实现 `AiModelController`（`/ai/model/**`）及 OpenAPI 注解

- [x] 5.3 实现连接测试 `POST /test`、导出 `GET /export`（yaml/env）

- [x] 5.4 实现可选 `POST /importFromYaml`（ENV_REF 草稿，不落明文密钥）

- [x] 5.5 配置变更/删除/设默认时调用 `AiModelRegistry.evict`



## 6. MCP 路径与权限迁移



- [x] 6.1 MCP Controller `@RequestMapping` 改为 `/ai/mcp`；权限注解改为 `ai:mcp:*`

- [x] 6.2 前端 `api/knowledge/mcp.js` 迁至 `api/ai/mcp.js` 并更新路径

- [x] 6.3 前端 MCP 页迁至 `views/ai/mcp/index.vue`；旧路由 redirect



## 7. 知识库与工作流绑定



- [x] 7.1 扩展知识库 Service/Bo/Vo：`chatModelId`、`embeddingModelId`

- [x] 7.2 扩展工作流 Service/Bo/Vo：`chatModelId`

- [x] 7.3 改造 `KnowledgeAiGuard`、`RagService`、入库/检索/分块：经 `AiModelResolver` 解析模型

- [x] 7.4 改造 `WorkflowAiGuard`、LLM/分类/参数抽取 Handler：经 Resolver 解析 ChatModel

- [x] 7.5 删除模型前校验 default_slot 与 KB/WF 引用



## 8. 前端



- [x] 8.1 新增 `api/ai/model.js` 封装全部大模型接口

- [x] 8.2 新增 `views/ai/model/index.vue`（C7JsonTable + 表单 + 测试/设默认/导出）

- [x] 8.3 改造知识库表单：Chat/Embedding 模型下拉 + Embedding 变更 confirm

- [x] 8.4 改造工作流列表/表单：Chat 模型下拉

- [x] 8.5 注册 `/ai` Layout 子路由（model、mcp）；验证 `pnpm build:prod` 通过



## 9. 联调与验收



- [ ] 9.1 手工验收：新增 Chat/Embedding → 测试 SUCCESS → 设全局默认

- [ ] 9.2 手工验收：知识库绑定模型 → RAG/入库走绑定模型

- [ ] 9.3 手工验收：工作流绑定 Chat → LLM 节点走绑定模型

- [ ] 9.4 手工验收：`qc.ai.enabled=false` 时 `/ai/model/**` 不可用；YAML fallback 仍可用

- [ ] 9.5 对照 spec 测试要点 TC_AI_MODEL_001–061 记录结果

