# AI 大模型管理 — 设计说明

**日期**：2026-06-07  
**状态**：已定稿（brainstorming 澄清结论 — 1E、2B、3E、4C、5D、6B、7D、8E）  
**关联**：OpenSpec 变更 `add-ai-model-management`（待创建）

---

## 1. 背景与目标

### 1.1 背景

QuickBoot 已通过 Spring AI 集成 Chat / Embedding（知识库 RAG、工作流入库与 LLM 节点），但模型配置分散在 `application*.yml`，运行时注入**单一** `ChatModel` / `EmbeddingModel` Bean。无法按知识库或工作流选择模型，变更需改 YAML 并重启。

外部 MCP 管理（`docs/superpowers/specs/2026-06-07-mcp-management-design.md`）已定稿 DB 台账 + 运行时动态接入模式。大模型管理与之对称，并升格为独立 **「AI 能力」** 菜单域（含大模型管理、MCP 管理）。

### 1.2 目标（本期一次交付）

| 能力 | 说明 |
|------|------|
| 模型 CRUD | 维护 Chat / Embedding：厂商、base-url、API Key、模型名、参数 |
| 双 Provider | **OpenAI 兼容**（DeepSeek、百炼等）+ **Ollama**（本地） |
| 连接测试 | Chat probe；Embedding 单向量 + 维度校验 |
| 全局默认 | 默认 Chat、默认 Embedding、默认工作流 Chat（三者可不同） |
| 资源绑定 | 知识库可选覆盖 Chat/Embedding；工作流可选覆盖 Chat |
| 运行时接入 | RAG、入库/检索、工作流 LLM/分类/参数抽取走 Registry 解析 |
| 导出 | Spring AI YAML 片段 + `.env` 变量清单 |
| 密钥安全 | SM4 加密 + ENV_REF + 列表脱敏（对齐 MCP） |
| 维度校验 | Embedding 设默认或绑定时须与 `qc.knowledge.vectorDimensions` 一致 |
| 菜单域 | 新建「AI 能力」一级菜单；MCP 管理迁入；API/权限前缀统一为 `/ai/*` |

### 1.3 非目标（本期不做）

- 模型微调/训练管理
- 用量计费 / Token 统计大盘
- Prompt 模板管理（与工作流节点 prompt 分离）
- 流式输出配置台（SSE 沿用现有能力）
- `jeecg.jmreport.ai` 纳入统一 Registry
- 修改全局 `vectorDimensions` 后的自动重建索引（仅保存时校验一致性）

---

## 2. 已定稿产品决策（Q&A）

| 题号 | 选项 | 结论 |
|------|------|------|
| 1 | E | CRUD + 测试 + 业务绑定 + 导出 |
| 2 | B | Chat + Embedding |
| 3 | E | OpenAI 兼容 + Ollama |
| 4 | C | 新建「AI 能力」一级菜单（大模型管理 + MCP 管理） |
| 5 | D | 全局默认 + 知识库/工作流可选覆盖 |
| 6 | B | SM4 + ENV_REF + 列表脱敏 |
| 7 | D | 一次全量交付（含维度校验） |
| 8 | E | 微调/计费/Prompt/流式管理均不做 |

**补充确认（brainstorming 评审）**：

- MCP API 路径本期迁至 `/ai/mcp/**`
- MCP 权限前缀改为 `ai:mcp:*`
- MCP 前端路由迁至 `/ai/mcp`，组件 `views/ai/mcp/index.vue`

---

## 3. 方案对比与选型

| 方案 | 概要 | 优点 | 缺点 |
|------|------|------|------|
| **A（推荐）** | 新建 `quickboot-ai`；DB + `AiModelRegistry` 程序化创建 Spring AI 模型实例 | 热更新；与 MCP 同构；知识库/工作流共用 | 需维护 Factory 与缓存生命周期 |
| B | DB 变更写回 YAML + Context Refresh | 复用自动配置 | 多实例不一致、热更新风险高 |
| C | 仅 CRUD 台账，运行时仍读 YAML | 改动最小 | 无法满足运行时绑定与导出 |

**采用方案 A**：依赖 Spring AI 1.0.x 程序化 API（`OpenAiApi` / `OllamaApi` + 对应 Chat/Embedding Model）。

---

## 4. 模块与菜单

### 4.1 新模块 `quickboot-ai`

```
quickboot-ai/
  config/           AiProperties (qc.ai.*)、条件装配
  model/            Entity、Enum、DTO、Mapper、Service、Controller
  registry/         AiModelFactory、AiModelRegistry、AiModelResolver
  support/          AiSecretSupport
```

依赖关系：

- `quickboot-web` → `quickboot-ai`
- `quickboot-knowledge` → `quickboot-ai`（RAG / 入库 / 检索）
- `quickboot-workflow` → `quickboot-ai`（LLM / 分类 / 参数抽取）

Maven（`quickboot-ai/pom.xml`）：

- `spring-ai-starter-model-openai`
- `spring-ai-starter-model-ollama`

包路径：`io.github.genkidoudou.web.ai.*`

开关：`qc.ai.enabled=false` 时不注册 Registry 与 Controller；消费者回落 YAML Bean（若 `fallback-to-yaml=true`）。

### 4.2 菜单调整（Flyway **V63**）

| menu_id | 父级 | 类型 | 名称 | 路由 | 组件 | order | 权限 |
|---------|------|------|------|------|------|-------|------|
| 2320 | -1 | M | AI 能力 | `/ai` | Layout | 5 | — |
| 2321 | 2320 | C | 大模型管理 | `model` | `ai/model/index` | 1 | `ai:model:list` |
| 2301 | 2320 | C | MCP 管理 | `mcp` | `ai/mcp/index` | 2 | `ai:mcp:list` |

- **迁移**：`UPDATE sys_menu SET parent_id=2320, path='mcp', component='ai/mcp/index', order_num=2 WHERE menu_id=2301`
- **知识管理**（2280）保留：知识库、文档库等业务页
- 大模型管理按钮权限：`ai:model:list|query|add|edit|remove|test|export|export:secrets`（2322+）
- MCP 菜单 `perms` 批量更新：`knowledge:mcp:*` → `ai:mcp:*`

旧路由 `/knowledge/mcp` 前端保留 redirect → `/ai/mcp`（一版兼容）。

---

## 5. 领域模型与表结构

Flyway **`V63__ai_model_management.sql`**（编号以仓库当前最大版本为准；若 V63 已占用则顺延）。

### 5.1 主表 — `ai_model`

| 字段 | 类型 | 说明 |
|------|------|------|
| `model_id` | BIGINT PK | 主键 |
| `name` | VARCHAR(100) | 展示名称 |
| `code` | VARCHAR(64) UNIQUE | 稳定标识，如 `deepseek-chat` |
| `description` | VARCHAR(500) | 备注 |
| `model_type` | VARCHAR(16) | `CHAT` / `EMBEDDING` |
| `provider` | VARCHAR(24) | `OPENAI_COMPAT` / `OLLAMA` |
| `base_url` | VARCHAR(2048) | API 根地址 |
| `api_key_type` | VARCHAR(16) | `PLAIN` / `SECRET` / `ENV_REF` |
| `api_key` | VARCHAR(2000) | 按 `api_key_type` 解释 |
| `model_name` | VARCHAR(128) | 厂商模型名 |
| `completions_path` | VARCHAR(128) NULL | OpenAI 兼容 Chat 路径（如 DeepSeek `/chat/completions`） |
| `embeddings_path` | VARCHAR(128) NULL | 默认由 SDK 拼接；可覆盖 |
| `dimensions` | INT NULL | **EMBEDDING 必填** |
| `temperature` | DECIMAL(4,2) NULL | Chat 默认温度 |
| `max_tokens` | INT NULL | Chat 可选上限 |
| `request_timeout_ms` | INT | 默认 60000 |
| `default_slot` | VARCHAR(24) NULL | `CHAT` / `EMBEDDING` / `WORKFLOW_CHAT`；启用项全局每 slot 唯一 |
| `status` | TINYINT | 0 正常 / 1 停用 |
| `last_test_status` | VARCHAR(16) NULL | `SUCCESS` / `FAILED` / `UNTESTED` |
| `last_test_msg` | VARCHAR(1000) NULL | 最近探测摘要 |
| `last_test_time` | DATETIME NULL | 最近探测时间 |
| 审计 + `deleted` | | 与项目规范一致 |

设默认时：应用层先将同 `default_slot` 的旧记录置 NULL，再写入新记录（避免 MySQL 多 NULL 唯一索引歧义）。

### 5.2 知识库绑定 — 扩展 `kb_knowledge_base`

| 字段 | 类型 | 说明 |
|------|------|------|
| `chat_model_id` | BIGINT NULL | NULL 表示使用全局默认 Chat |
| `embedding_model_id` | BIGINT NULL | NULL 表示使用全局默认 Embedding |

### 5.3 工作流绑定 — 扩展 `wf_workflow`

| 字段 | 类型 | 说明 |
|------|------|------|
| `chat_model_id` | BIGINT NULL | NULL 表示使用 WORKFLOW_CHAT 默认 → 再回落 CHAT 默认 |

### 5.4 配置项 — `qc.ai`

```yaml
qc:
  ai:
    enabled: true
    registry:
      fallback-to-yaml: true    # DB 无可用默认时沿用 spring.ai 自动配置 Bean
      client-cache-ttl-seconds: 300
      test-timeout-ms: 20000
    export:
      include-secrets: false
```

知识库 PGVector 维度仍由 `qc.knowledge.vectorDimensions` 控制（默认 768）。

---

## 6. 运行时架构

```mermaid
flowchart TB
  subgraph AdminUI
    ModelCRUD[大模型管理页]
    Test[连接测试]
    Export[导出 YAML/env]
    KbBind[知识库选模型]
    WfBind[工作流选模型]
  end

  subgraph quickboot-ai
    API[/ai/model/**]
    Factory[AiModelFactory]
    Registry[AiModelRegistry]
    Resolver[AiModelResolver]
  end

  subgraph Consumers
    RAG[RagService]
    Ingest[DocumentIngestionService]
    Search[KnowledgeSearchService]
    LLM[LlmNodeHandler 等]
  end

  ModelCRUD --> API
  Test --> API
  Export --> API
  API --> Registry
  Factory --> Registry
  KbBind --> Resolver
  WfBind --> Resolver
  RAG --> Resolver
  Ingest --> Resolver
  Search --> Resolver
  LLM --> Resolver
  Resolver --> Registry
```

### 6.1 解析优先级（5D）

```
resolveChat(kbId?):
  kb.chat_model_id → default_slot=CHAT → yaml ChatModel Bean

resolveEmbedding(kbId?):
  kb.embedding_model_id → default_slot=EMBEDDING → yaml EmbeddingModel Bean

resolveWorkflowChat(workflowId):
  wf.chat_model_id → default_slot=WORKFLOW_CHAT → default_slot=CHAT → yaml Bean
```

### 6.2 `AiModelRegistry`

- **获取实例**：按 `model_id` 缓存 `ChatModel` 或 `EmbeddingModel`；miss 时读库经 `AiModelFactory` 构建。
- **失效**：update / remove / status 变更 / default_slot 变更时 `evict(modelId)`；可选 TTL 刷新。
- **停用模型**：解析链跳过 `status=1` 记录，继续回落下一优先级。

### 6.3 `AiModelFactory`

| Provider | Chat | Embedding |
|----------|------|-----------|
| `OPENAI_COMPAT` | `OpenAiApi` + `OpenAiChatModel` | `OpenAiEmbeddingModel` |
| `OLLAMA` | `OllamaApi` + `OllamaChatModel` | `OllamaEmbeddingModel` |

- API Key 经 `AiSecretSupport.resolvePlainValue()` 解析
- `request_timeout_ms` 映射到 RestClient / API builder
- Ollama 无 Key 时 `api_key` 可空

### 6.4 连接测试

`POST /ai/model/test`（`modelId`）：

| model_type | 动作 | 成功条件 |
|------------|------|----------|
| CHAT | 极简 prompt（如 `ping`） | 非空回复 |
| EMBEDDING | `embed("test")` | 返回向量维度 == 表字段 `dimensions` |

失败写 `last_test_status=FAILED`，不修改 `status`。

### 6.5 Embedding 维度校验（7D）

保存或设默认时：

- `model_type=EMBEDDING` 且（`default_slot=EMBEDDING` 或将被 KB 引用）→ `dimensions` 必须等于 `qc.knowledge.vectorDimensions`
- 不等则拒绝并提示调整模型 dimensions 或 `vectorDimensions`（重建索引不在本期）

### 6.6 密钥处理 — `AiSecretSupport`

复用 `PasswordCodec` + SM4，模式对齐 `McpSecretSupport`：

| api_key_type | 存储 | 运行时解析 |
|--------------|------|------------|
| PLAIN | 明文 | 原值 |
| SECRET | `{sm4:...}` | `codec.decrypt` |
| ENV_REF | 变量名 | `System.getenv(name)`，缺失则测试/调用失败并提示 |

列表 VO：`SECRET` 显示 `******`；详情 `revealSecrets=false` 默认脱敏，`true` 需 `ai:model:query` 且记操作日志。

### 6.7 消费者改造要点

| 模块 | 改造 |
|------|------|
| `KnowledgeAiGuard` | 改为委托 `AiModelResolver`；错误信息指向「大模型管理」 |
| `RagService` | 按 `kbId` 解析 ChatModel |
| `DocumentIngestionService` / `KnowledgeSearchService` / `KbDocumentChunkServiceImpl` | 按 `kbId` 解析 EmbeddingModel |
| `WorkflowAiGuard` / `LlmNodeHandler` / 分类 / 参数抽取 | 按 `workflowId` 解析 ChatModel |
| `KnowledgeVectorStoreConfiguration` | 全局 VectorStore 仍用**默认 Embedding** 或启动时默认模型；KB 级 Embedding 差异仅影响该库入库/检索路径（P0 若 VectorStore 单实例，绑定 Embedding 变更需 re-index 提示） |

**VectorStore 说明**：PGVector 单向量维度；不同 KB 使用不同 Embedding 模型时，向量空间不可混用。UI 在 KB 修改 `embedding_model_id` 时提示「建议对该库文档重建索引」。运行时检索/入库均使用解析后的 EmbeddingModel 写入/查询同一 store（metadata 含 `kbId` 过滤）。

---

## 7. API 设计

### 7.1 大模型 — 前缀 `/ai/model`

修改/删除仍 `@PostMapping`。

| 路径 | 权限 | 说明 |
|------|------|------|
| `GET /list` | `ai:model:list` | 分页；name、code、modelType、provider、status、defaultSlot |
| `GET /getInfo` | `ai:model:query` | 详情；`revealSecrets` |
| `POST /add` | `ai:model:add` | 新增 |
| `POST /update` | `ai:model:edit` | 修改；SECRET 空串表示不修改 |
| `POST /remove` | `ai:model:remove` | 逻辑删；校验 default_slot 与 KB/WF 引用 |
| `POST /test` | `ai:model:test` | 连接测试 |
| `POST /setDefault` | `ai:model:edit` | `{ modelId, defaultSlot }`；清旧 slot |
| `POST /clearDefault` | `ai:model:edit` | `{ defaultSlot }` |
| `GET /export` | `ai:model:export` | `format=yaml\|env`；`ids` 可选 |
| `GET /options` | `ai:model:list` | 下拉；`modelType=CHAT\|EMBEDDING` |
| `POST /importFromYaml` | `ai:model:add` | 可选：从当前 `spring.ai` 生成预置条目（不落库密钥明文） |

### 7.2 MCP 路径迁移 — 前缀 `/ai/mcp`

原 `/knowledge/mcp/**` Controller 迁至 `quickboot-ai` 或保留包名仅改 `@RequestMapping` 为 `/ai/mcp`；权限改为 `ai:mcp:*`。

### 7.3 业务 API 扩展

- **知识库** `/knowledge/base`：`add` / `update` / `getInfo` 增加 `chatModelId`、`embeddingModelId`
- **工作流** `/workflow/**`：`add` / `update` / `getInfo` 增加 `chatModelId`

### 7.4 导出格式

**YAML**（`GET /ai/model/export?format=yaml`）：

```yaml
spring:
  ai:
    openai:
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        completions-path: /chat/completions
        options:
          model: deepseek-v4-pro
          temperature: 0.3
      embedding:
        base-url: https://dashscope.aliyuncs.com/compatible-mode
        api-key: ${DASHSCOPE_API_KEY}
        options:
          model: text-embedding-v4
          dimensions: 768
```

**ENV**（`format=env`）：每模型一行 `KEY=value` 或 `KEY=${REF}`。

- `SECRET` → `${CODE}_API_KEY` 占位
- `includeSecrets=true` 需 `ai:model:export:secrets`

---

## 8. 前端（quick-ui）

### 8.1 大模型管理页 — `views/ai/model/index.vue`

- 列表：`C7JsonTable`（参照 `views/system/config/index.vue`）
- 列：名称、编码、类型、Provider、默认角色、状态、最近测试、更新时间
- 操作：新增/编辑、测试、设为默认、清除默认、导出、删除
- 表单：
  - **基本**：名称、编码、描述、类型、Provider、模型名、状态
  - **连接**：base-url、api-key（类型 + 值）、请求超时
  - **Chat**：temperature、max_tokens、completions_path
  - **Embedding**：dimensions（旁注当前 `vectorDimensions`）、embeddings_path
- 测试：弹窗展示 probe 结果或向量维度

### 8.2 知识库表单

- 「Chat 模型」「Embedding 模型」：`el-select` clearable，数据源 `/ai/model/options`
- 修改 Embedding 时 confirm：「变更后建议重建该库索引」

### 8.3 工作流表单

- 「Chat 模型」：`el-select` clearable

### 8.4 MCP 页迁移

- `views/ai/mcp/index.vue`；API 改 `/ai/mcp/**`；权限 `ai:mcp:*`
- 路由 `/ai/mcp`；`/knowledge/mcp` → redirect

### 8.5 路由注册

`router` 增加 `/ai` Layout 子路由：`model`、`mcp`。

---

## 9. 权限与安全

| 项 | 策略 |
|----|------|
| 大模型 | `ai:model:*` |
| MCP | `ai:mcp:*`（自 `knowledge:mcp:*` 迁移） |
| 鉴权 | Sa-Token；配置全局可见（与知识库/MCP 一致） |
| 审计 | 导出含密钥、详情 `revealSecrets` 记 operlog |
| 删除 | 引用中或占 default_slot 的模型禁止删除 |

---

## 10. 迁移与兼容

1. **V63** 建表 + 扩展 KB/WF + 菜单 + MCP 权限/perms 更新
2. `application.yml` 增加 `qc.ai` 块
3. **YAML 兜底**：`qc.ai.registry.fallback-to-yaml=true` 时，DB 无默认行为与现网一致
4. **可选导入**：管理页「从 YAML 导入」生成 Chat/Embedding 草稿（API Key 用 ENV_REF），不 Flyway 种子
5. 无历史数据强制回填

---

## 11. 测试要点

| ID | 场景 |
|----|------|
| TC_AI_MODEL_001 | 登录后「AI 能力」下可见大模型管理、MCP 管理 |
| TC_AI_MODEL_010 | 新增 OpenAI 兼容 Chat → 测试 SUCCESS |
| TC_AI_MODEL_011 | 新增 Ollama Embedding → 测试维度匹配 |
| TC_AI_MODEL_012 | SECRET 入库加密；列表脱敏；详情默认不明文 |
| TC_AI_MODEL_013 | ENV_REF 运行时从环境变量解析 |
| TC_AI_MODEL_020 | 设全局默认 Chat → RAG 使用新模型 |
| TC_AI_MODEL_021 | 知识库绑定 Embedding → 入库使用该模型 |
| TC_AI_MODEL_022 | 工作流绑定 Chat → LLM 节点使用该模型 |
| TC_AI_MODEL_030 | Embedding dimensions ≠ vectorDimensions → 保存拒绝 |
| TC_AI_MODEL_040 | 导出 yaml/env 密钥为占位符 |
| TC_AI_MODEL_050 | DB 无配置 + fallback → YAML Bean 仍可用 |
| TC_AI_MODEL_060 | MCP 旧路径 `/knowledge/mcp` redirect 至 `/ai/mcp` |
| TC_AI_MODEL_061 | 删除被 KB 引用的模型 → 拒绝 |

---

## 12. 实施任务概览（供 writing-plans）

1. 新建 `quickboot-ai` 模块 + `qc.ai` 配置 + Flyway V63
2. `AiSecretSupport` + `AiModelFactory` + `AiModelRegistry` + `AiModelResolver` + 单测
3. 大模型 CRUD / test / setDefault / export API
4. MCP Controller 路径与权限迁移；菜单/perms Flyway
5. 知识库 / 工作流绑定字段 + 消费者改造（RAG、入库、LLM 节点）
6. 前端：大模型管理页 + KB/WF 表单 + MCP 迁路由 + redirect
7. 联调 + 可选 YAML 导入

---

## 13. 风险与缓解

| 风险 | 缓解 |
|------|------|
| KB 切换 Embedding 后向量不可比 | UI 警告 + 文档说明需 re-index |
| Spring AI API 版本差异 | 以 1.0.0 BOM 为准；集成测试覆盖 Factory |
| MCP 路径/权限迁移破坏现有集成 | Flyway 更新 perms；前端 redirect；后端保留短期双前缀可选 |
| 一次交付面大 | 按 §12 顺序；先后端 Registry+CRUD，再消费者，最后菜单与 MCP 迁移 |

---

**下一步**：编写实现计划（`writing-plans` / OpenSpec `add-ai-model-management` tasks）。
