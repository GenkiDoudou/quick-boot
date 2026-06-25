# Spring AI 1.0→2.0 + Boot 4 迁移方案与影响面扫描清单

> **编写日期**：2026-06-24  
> **扫描范围**：`quickboot/` 全后端（含 AI、知识库、工作流、智能体应用）  
> **基线**：Spring Boot `3.5.3` · Spring AI `1.0.0` · MCP SDK `0.17.1`（手工 pin）· JDK 17  
> **目标**：Spring Boot `4.0+` · Spring AI `2.0.0` · MCP SDK `2.0.0`（随 Spring AI BOM）  
> **参考**：[Spring AI 2.0 Release](https://github.com/spring-projects/spring-ai/releases/tag/v2.0.0) · [Upgrade Notes](https://docs.spring.io/spring-ai/reference/upgrade-notes.html) · [AgentScope Java 2.0](https://java.agentscope.io/v2/zh/docs/index.html)

---

## 详细迁移方案

### 1. 目标与策略

**总体结论**：推荐 **Spring AI 2.0 + Boot 4** 升级知识库 / 工作流 / 模型工厂管线，并 **并行引入 AgentScope Java 2.0** 增强智能体应用——**混合双栈、分阶段迁移**，不做 Spring AI 全量替换。

| 策略 | 说明 | 是否采用 |
|------|------|----------|
| A 全量替换为 AgentScope | RAG/PGVector/工作流代价过大 | ❌ |
| B 仅升 Spring AI 2.0 | 平台现代化，智能体体验提升有限 | 部分 |
| **C 双栈渐进（推荐）** | Spring AI 管 RAG/工作流；AgentScope 管智能体 | ✅ |

**版本目标**：

| 组件 | 当前 | 目标 |
|------|------|------|
| Spring Boot | 3.5.3 | 4.0.x（Spring AI 2.0 硬依赖） |
| Spring AI | 1.0.0 | 2.0.0 |
| MCP Java SDK | 0.17.1（手工 pin） | 2.0.0（随 Spring AI BOM） |
| Jackson | 2.x | 3.x（`tools.jackson`，随 Boot 4） |
| AgentScope | 未引入 | 2.x（`agentscope-harness`，可与 Boot 3.5 先 POC） |
| JDK | 17 | 17（Boot 4 最低）；生产建议 21 |

---

### 2. 目标架构

```text
┌─────────────────────────────────────────────────────────────┐
│  前端：工作流设计/运行 · AI 智能体 · 知识库 RAG              │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  quickboot-ai-port（新增抽象层，可选但强烈建议）               │
│  AiChatPort · AiEmbeddingPort · AiStreamPort · AiToolPort   │
│  AiModelBridge（ai_model 表 → 双框架模型实例）               │
└───────┬─────────────────────────────────────┬───────────────┘
        │                                     │
┌───────▼──────────────┐           ┌──────────▼────────────────┐
│  Spring AI 2.0       │           │  AgentScope 2.0           │
│  ChatModel/Embedding │           │  HarnessAgent / ReActAgent│
│  PGVector VectorStore│           │  记忆/压缩/技能/子 Agent   │
│  ChatClient + Tool   │           │  streamEvents 真流式       │
│  MCP Client (SDK 2)  │           │  RedisAgentStateStore      │
└───────┬──────────────┘           └──────────┬────────────────┘
        │                                     │
        │    知识库检索 / 工作流 Tool 桥接      │
        └─────────────────┬───────────────────┘
                          │
              ai_model 表 · PGVector · MySQL 业务库
```

**分工原则**：上层业务只依赖 Port 或领域 Service，避免 Controller/Handler 直接 `import org.springframework.ai.*` 与 `io.agentscope.*` 混用。

---

### 3. 能力分工（Spring AI 2.0 vs AgentScope 2.0）

| 能力域 | 负责框架 | 主要模块 | 说明 |
|--------|----------|----------|------|
| 模型工厂 / DB 配置 | Spring AI（+ Bridge） | `quickboot-ai` | `AiModelFactory` 继续从 `ai_model` 表构建实例 |
| 知识库向量入库 / Hybrid 检索 | **Spring AI 2.0** | `quickboot-knowledge` | PGVector 管线不迁 AgentScope RAG |
| RAG 问答 | **Spring AI 2.0** | `RagService` | 检索 + 单次生成 |
| 工作流 LLM / 意图 / 参数提取 | **Spring AI 2.0** | `quickboot-workflow` | 确定性 DAG，不上 Harness |
| 工作流 MCP 工具 | **Spring AI 2.0 MCP** | `LlmNodeHandler` | SDK 2.0 须回归 ModelScope |
| **AI 智能体应用** | **AgentScope 2.0** | `quickboot-ai-app` | 真流式、Tool 循环、会话恢复、记忆 |
| 工作流/KB 作为 Agent Tool | AgentScope Tool 桥接 | `WorkflowToolFactory` 等 | 内部仍调现有 Service |
| Embedding / VectorStore | **保留 Spring AI** | 长期 | AgentScope Simple RAG 仅作可选，不替代 PGVector |

---

### 4. 分阶段实施计划

#### 阶段 0：抽象层 + 依赖治理（2–3 周）

**目标**：隔离双框架，为后续切换留接口。

| 任务 | 产出 | 验证 |
|------|------|------|
| 新增 `quickboot-ai-port`（或置于 `quickboot-ai` 内） | `AiChatPort`、`AiEmbeddingPort`、`AiStreamPort`、`AiToolPort` | 接口单测 |
| 实现 `AiModelBridge` | `ai_model.provider` → Spring AI / AgentScope 模型字符串或显式 Model | DeepSeek/通义/Ollama 连通 |
| Spring AI 1.0.0 → **1.1.x**（Boot 3.5 不动） | 消 deprecation、修 CVE | `mvn clean install -DskipTests` |
| 梳理三方 Boot4 兼容矩阵 | MyBatis-Plus / Sa-Token / JimuReport / Druid 版本表 | 文档记录可用坐标 |

**`AiModelBridge` 映射示例**：

| `ai_model.provider` | AgentScope model 字符串 | Spring AI 路径 |
|---------------------|---------------------------|----------------|
| TONGYI | `dashscope:{modelName}` | OpenAi 兼容 + baseUrl |
| OPENAI / DEEPSEEK | `openai:{modelName}` | `OpenAiApi` + completions-path |
| OLLAMA | `ollama:{modelName}` | `OllamaChatModel` |

---

#### 阶段 1：智能体应用 AgentScope POC（3–4 周，可与阶段 0 尾重叠）

**目标**：`quickboot-ai-app` 跑在 Harness 上，SSE 真流式。

| 任务 | 改动要点 |
|------|----------|
| 引入 `agentscope-harness`、`agentscope-extensions-redis` | 根 `pom.xml` BOM |
| `HarnessAgentFactory` | 按 `appId` + `AgentAppConfigDto` 构建 Agent |
| 改造 `AiAppChatServiceImpl` | `call()` → `streamEvents()`；事件映射现有 `delta`/`done`/`error` |
| Tool 桥接 | `WorkflowToolFactory` / `KnowledgeSearchToolFactory` → AgentScope `Tool` |
| 会话状态 | 生产 `RedisAgentStateStore`；DB `ai_app_message` 仍存展示用消息 |
| 记忆变量 | 短期保留 `AiAppVariableService`；中期可迁 Harness `MEMORY.md` |

**SSE 事件映射**：

| 现有协议 | AgentScope 事件 |
|----------|-----------------|
| `delta` | `TEXT_BLOCK_DELTA` |
| （可新增） | `TOOL_CALL_START` / 工具结果 |
| `done` / `error` | 回复结束 / 异常 |

**验收**：同 session 多轮对话；Tool 调工作流/KB；重启后会话可恢复（Redis）；知识库/workflow **零改动**。

---

#### 阶段 2：Spring Boot 4 + Jackson 3 平台升级（4–6 周）

**目标**：满足 Spring AI 2.0 硬依赖，全项目编译通过。

| 任务 | 范围 |
|------|------|
| Boot 3.5.3 → 4.0.x | 根 `pom.xml`、全模块 starter 坐标 |
| Jackson 2 → 3 | `quickboot-common` 脱敏/FileUrl/防火墙/OperLog（约 40 文件） |
| 三方 starter 升级 | `mybatis-plus-spring-boot4-starter`、`sa-token-spring-boot4-starter` 等 |
| `JacksonTimeConfig` 等 | `quickboot-web` |
| JimuReport | 确认 Boot4 版或临时禁用 `quickboot-report` 模块 |

**验证**：`mvn clean install`；登录/OAuth/OperLog/FileUrl 序列化；Flyway 迁移；非 AI 回归。

---

#### 阶段 3：Spring AI 2.0 + MCP SDK 2.0（3–4 周）

**目标**：AI 管线全量迁 2.0 API。

| 任务 | 关键文件 |
|------|----------|
| BOM 升 2.0.0 | 根 `pom.xml`；**移除** `mcp-sdk.version=0.17.1` 覆盖 |
| 模型工厂 | `AiModelFactory`：Options 全 Builder 化；`OpenAiCommonProperties` |
| PGVector | `KnowledgeVectorStoreConfiguration`：包名 `org.springframework.ai.pgvector.vectorstore` |
| 知识库入库/检索 | `DocumentIngestionService`、`KnowledgeSearchService`、`KbDocumentChunkServiceImpl` |
| ChatClient 节点 | `LlmNodeHandler`、`QuestionClassifierNodeHandler`、`ParameterExtractorNodeHandler` |
| Token Trace | `WorkflowTokenUsageSupport` |
| 智能体（若未切 AgentScope） | `AiAppChatServiceImpl` 等 |
| YAML 配置 | `application-dev.yml`：`spring.ai.*` 结构；`PgVectorStoreAutoConfiguration` exclude 全名 |

**MCP 2.0 专项（P0）**：

| 任务 | 说明 |
|------|------|
| `McpTransportFactory` | `JacksonMcpJsonMapper` + Jackson 3 `JsonMapper` |
| `StatelessStreamableHttpTransport` | 对齐 SDK 2.0 接口；**ModelScope GET /mcp JSON 全量回归** |
| `McpClientManager` / `McpToolCallbackProvider` | `SyncMcpToolCallbackProvider` 新包名 |
| STDIO / SSE / Streamable HTTP | 各传输方式连通性测试 |

**验证**：模型连通性测试；文档入库+向量检索；Hybrid 搜索；工作流 LLM 流式+MCP；RAG 问答。

---

#### 阶段 4：MCP 统一与 Port 收口（2 周，可选）

| 任务 | 说明 |
|------|------|
| 业务改走 `AiChatPort` / `AiToolPort` | workflow / knowledge / ai-app |
| 移除重复 MCP 胶水 | 评估是否仍需自研 Transport |
| `quickboot-ai-app` 完全脱离 `ChatClient` | 若阶段 1 已完成 |

---

#### 阶段 5：全量回归与上线（1–2 周）

```text
回归清单：
□ ai_model 后台：DeepSeek / 通义 / Ollama 连通性测试
□ 知识库：上传→分块→入库→向量检索→Hybrid→RAG 问答→删库删文档
□ 工作流：LLM 节点流式 delta + MCP 工具 + Token trace
□ 工作流：意图识别 / 参数提取节点 JSON 输出
□ 智能体：多轮对话 + 工作流 Tool + 知识库 Tool/注入 + SSE
□ 智能体：会话恢复（Redis）+ 预览模式
□ MCP 管理：各传输方式注册、测试、工作流绑定
□ 非 AI：登录、权限、OperLog、导出、报表（若启用）
```

**回滚策略**：按阶段打 Git tag；Boot4+AI2 与 AgentScope 可独立回滚（Port 抽象前提下）。

---

### 5. Maven 与配置变更要点

**根 `pom.xml` 目标片段（示意）**：

```xml
<spring-boot.version>4.0.x</spring-boot.version>
<spring-ai.version>2.0.0</spring-ai.version>
<agentscope.version>2.x.x</agentscope.version>
<!-- 删除 mcp-sdk.version 手工 pin，由 spring-ai-bom 管理 -->
```

**模块依赖**：

| 模块 | 保留/新增 |
|------|-----------|
| `quickboot-ai` | `spring-ai-starter-model-openai`、`ollama`；可选 `agentscope-harness` |
| `quickboot-knowledge` | `pgvector`、`tika`、`mcp-client`、`advisors-vector-store` |
| `quickboot-workflow` | `spring-ai-starter-model-openai` |
| `quickboot-ai-app` | 逐步减 `spring-ai`；增 `agentscope-harness`、`agentscope-extensions-redis` |

**`application-dev.yml` 注意**：

- `spring.ai.openai.*` 按 2.0 `OpenAiCommonProperties` 结构调整
- `spring.autoconfigure.exclude` 中 PGVector 自动配置类全名更新
- `qc.*` 业务配置（`knowledge`/`workflow`/`ai-app`）原则上不变

---

### 6. 验收标准（总览）

| 阶段 | 必达标准 |
|------|----------|
| 0 | Port 接口可用；Spring AI 1.1.x 编译通过；Bridge 映射单测通过 |
| 1 | 智能体真流式 SSE；Tool 调工作流/KB；Redis 会话恢复 |
| 2 | Boot 4 全项目编译；Jackson 3 核心链路 JSON 正常 |
| 3 | 知识库零回归；工作流 LLM/MCP/流式与现网一致；MCP ModelScope 可用 |
| 5 | 上表回归清单全部通过 |

---

### 7. 人力与排期（粗估）

| 阶段 | 人力 | 周期 | 可并行 |
|------|------|------|--------|
| 0 抽象层 + 1.1.x | 1 后端 | 2–3 周 | — |
| 1 AgentScope 智能体 | 1 后端 + 0.5 前端 | 3–4 周 | 与 0 尾部重叠 |
| 2 Boot 4 + Jackson 3 | 1–2 后端 | 4–6 周 | 与 1 部分并行 |
| 3 Spring AI 2.0 + MCP 2 | 1 后端 | 3–4 周 | 依赖 2 |
| 4–5 收口与回归 | 1 后端 + QA | 2–3 周 | — |
| **合计** | — | **约 3–5 月** | MVP（0+1）约 6–8 周 |

---

### 8. 风险与对策

| 风险 | 等级 | 对策 |
|------|------|------|
| MCP 0.17.1→2.0，ModelScope 兼容失效 | **高** | 阶段 3 前单独 MCP POC；保留 `StatelessStreamableHttpTransport` |
| Boot 4 三方 starter 未就绪 | **高** | 阶段 0 兼容矩阵；JimuReport 可暂缓 |
| Jackson 3 自定义序列化回归 | **高** | 阶段 2 专项测试：脱敏/FileUrl/防火墙/OperLog |
| 双栈模型配置不一致 | 中 | `AiModelBridge` 单点映射 + 集成测试 |
| 智能体 DB 消息 vs AgentState 双写 | 中 | 约定：State 负责恢复，DB 负责展示 |
| ChatClient Tool 行为 2.0 变化 | 中 | `LlmNodeHandler` OpenAiChatOptions + defaultToolCallbacks 专项测 |

---

### 9. 不建议迁移的范围

| 项 | 原因 |
|----|------|
| PGVector → AgentScope RAG 集成 | 架构换道，非版本升级 |
| 工作流节点 → HarnessAgent | 与可视化 DAG 重复，仅需替换 Chat 调用层 |
| 知识库 Hybrid 检索逻辑 | 业务自研，仅 `VectorStore` API 随 Spring AI 变 |

---

## 影响面扫描清单

> 以下为 **Spring AI 1.0→2.0 + Boot 4** 文件级改动点，实施时与上文阶段 2、3 对照使用。

---

## 图例
| 标记 | 含义 |
|------|------|
| **P0** | 必改 |
| **P1** | 高概率改 |
| **P2** | 编译/运行后验证 |
| **BOOT** | Spring Boot 4 平台 |
| **AI** | Spring AI 2.0 API |
| **MCP** | MCP SDK 2.0 |
| **JK** | Jackson 3（`com.fasterxml.jackson` → `tools.jackson`） |
| **DEPS** | Maven 依赖坐标/版本 |

---

## 一、总体结论

| 维度 | 数量（约） | 说明 |
|------|-----------|------|
| 直接 `org.springframework.ai.*` import | **42** | 必跑编译 + AI 回归 |
| MCP SDK `io.modelcontextprotocol.*` | **9** | SDK 2.0 全量回归，含 ModelScope |
| Jackson `com.fasterxml.jackson` | **40+** | Boot 4 连带，非 AI 专属 |
| `pom.xml` 涉及 | **7** | 根 POM + 5 个 AI 子模块 + report |

**最大不确定性**：

1. **MCP SDK 0.17.1→2.0**：项目为 ModelScope 等场景自研 `StatelessStreamableHttpTransport`，须单独 POC。
2. **三方 Boot4 starter**：MyBatis-Plus、Sa-Token、JimuReport、Druid 等是否已有 Boot4 坐标。
3. **Jackson 3**：`quickboot-common` 脱敏/FileUrl/防火墙等自定义序列化模块全量迁移。

**推荐执行顺序**（与上文「详细迁移方案」阶段 0→5 一致，此处为速查）：

| 步 | 动作 | 影响文件量级 |
|----|------|-------------|
| A | Boot 3.5 上先升 Spring AI **1.1.x**，消 deprecation | ~42 AI 文件部分 |
| B | 验证三方 Boot4 兼容矩阵 | pom 7 处 |
| C | Boot 4 + Jackson 3 全项目 | ~40 JK 文件 |
| D | Spring AI 2.0 + MCP 2.0 | ~42 AI + 9 MCP |
| E | 全量回归：知识库入库检索、工作流 LLM/MCP、智能体 Tool、模型连通性测试 | E2E |

---

## 二、根 POM / 平台（全项目闸门）

| 文件 | 级别 | 改动点 |
|------|------|--------|
| `quickboot/pom.xml` | P0 | `spring-boot.version` 3.5.3→4.0.x；`spring-ai.version` 1.0.0→2.0.0；**移除** `mcp-sdk.version=0.17.1` 手工 pin，改由 Spring AI BOM 管理 MCP 2.0 |
| `quickboot/pom.xml` | P0 | 三方 starter 换 Boot4 坐标：`mybatis-plus-spring-boot3-starter`→`boot4`；`sa-token-spring-boot3-starter`→`boot4`；`druid-spring-boot-3-starter`→Boot4 版；`jimureport/jimubi-spring-boot3-starter` 查官方兼容 |
| 全模块 `pom.xml` | P1 | Spring AI starter artifact 名可能变（如 `spring-ai-starter-mcp-client` transport 迁入 Spring AI） |
| — | P0 | **前置步**：建议先升 Spring AI `1.1.x`（仍 Boot 3.5）消 deprecation，再上 Boot 4 |

---

## 三、quickboot-ai（模型工厂核心，12 文件）

| 文件 | 级别 | 类型 | 改动点 |
|------|------|------|--------|
| `registry/AiModelFactory.java` | P0 | AI | `OpenAiApi.builder()` / `OpenAiChatOptions.builder()` API 变更；`OpenAi*Properties` 不再继承 options；`OllamaOptions` builder 字段名核对；`OpenAiEmbeddingModel` 构造签名 |
| `registry/AiModelRegistry.java` | P2 | AI | `ChatModel`/`EmbeddingModel` 接口若变，缓存类型跟随 |
| `registry/AiModelResolver.java` | P2 | AI | YAML 回落 `ObjectProvider<ChatModel>` 自动配置 Bean 名/条件变更 |
| `registry/AiModelConnectionTester.java` | P1 | AI | `Prompt`/`UserMessage`/`ChatResponse` 包路径或 API；Embedding 探针 |
| `prompt/support/AiPromptChatSupport.java` | P2 | AI | 仅 `ChatModel` 类型引用 |
| `prompt/service/impl/AiPromptOptimizeServiceImpl.java` | P1 | AI | `ChatClient.builder().prompt().call()` 链路与 advisor 行为 |
| `support/DeepSeekThinkingDisableInterceptor.java` | P1 | JK+BOOT | `com.fasterxml.jackson`→`tools.jackson`；`RestClient` 拦截器在 Framework 7 下验证 |
| `config/AiProperties.java` | P2 | — | 注释/YAML 回落语义，无直接 AI import |
| `pom.xml` | P0 | DEPS | `spring-ai-starter-model-openai`、`spring-ai-starter-model-ollama` 版本随 BOM |

---

## 四、quickboot-knowledge（最重：RAG + PGVector + MCP，28+ 文件）

### 4.1 向量 / RAG / 入库

| 文件 | 级别 | 类型 | 改动点 |
|------|------|------|--------|
| `config/KnowledgeVectorStoreConfiguration.java` | P0 | AI | `PgVectorStore` 包迁：`org.springframework.ai.vectorstore.pgvector`→`org.springframework.ai.pgvector.vectorstore`；`PgVectorStore.builder()` API |
| `config/KnowledgeAiHttpConfiguration.java` | P1 | BOOT | `@Primary RestClient.Builder` 与 Boot4 / Spring AI 2 自动配置冲突再评估 |
| `rag/KnowledgeSearchService.java` | P0 | AI | `SearchRequest.builder()`；`VectorStore.similaritySearch`；`Document` 包路径 |
| `rag/RagService.java` | P1 | AI | `ChatClient` + `ToolCallback` + MCP 聚合 |
| `support/KnowledgeVectorSupport.java` | P1 | AI | `VectorStore.delete(filter)` 过滤表达式语法 |
| `support/KnowledgeAiGuard.java` | P2 | AI | `ChatModel`/`EmbeddingModel` 解析委托 |
| `ingest/DocumentIngestionService.java` | P1 | AI | `Document` metadata API；`vectorStore.add()` |
| `ingest/SegmentPreviewService.java` | P2 | AI | `Document` |
| `ingest/chunk/ChunkStrategy.java` | P1 | AI | `TokenTextSplitter` 包路径/API |
| `ingest/chunk/DelimiterTokenChunkSplitter.java` | P1 | AI | 同上 |
| `ingest/preprocess/TextPreprocessor.java` | P2 | AI | `Document` |
| `ingest/source/TikaDocumentLoader.java` | P1 | AI | `TikaDocumentReader` 包/构造变更 |
| `ingest/source/DocumentSourceAdapter.java` | P2 | AI | `Document` |
| `ingest/source/FileDocumentSourceAdapter.java` | P2 | AI | `Document` |
| `ingest/source/LibraryDocumentSourceAdapter.java` | P2 | AI | `Document` |
| `ingest/source/ManualDocumentSourceAdapter.java` | P2 | AI | `Document` |
| `ingest/source/WebDocumentSourceAdapter.java` | P2 | AI | `Document` |
| `service/impl/KbDocumentChunkServiceImpl.java` | P1 | AI | `VectorStore` 单条删/改 |
| `constants/KbSegmentMode.java` | P2 | AI | 若引用 Spring AI 枚举 |

### 4.2 MCP（最高风险区）

| 文件 | 级别 | 类型 | 改动点 |
|------|------|------|--------|
| `mcp/runtime/McpTransportFactory.java` | P0 | MCP+JK | `JacksonMcpJsonMapper(new ObjectMapper())`→Jackson 3 `JsonMapper`；Transport builder API（`HttpClientSseClientTransport` 等） |
| `mcp/transport/StatelessStreamableHttpTransport.java` | P0 | MCP | 自研 Transport 对齐 MCP SDK 2.0：`McpClientTransport`/`McpSchema`/`ProtocolVersions` 签名变更；**ModelScope GET /mcp JSON 兼容须回归** |
| `mcp/runtime/McpClientManager.java` | P0 | MCP | `McpClient.sync()`/`McpSyncClient` 构造与生命周期 API |
| `mcp/runtime/McpToolCallbackProvider.java` | P0 | AI+MCP | `SyncMcpToolCallbackProvider` 包名/类名变更；`getToolCallbacks()` |
| `mcp/runtime/McpTrackingToolCallbacks.java` | P1 | AI | `ToolCallback`/`ToolDefinition` 接口 |
| `mcp/runtime/McpTextualToolCallSupport.java` | P1 | AI | Tool 调用链 |
| `mcp/runtime/McpConnectionTester.java` | P1 | MCP | 客户端初始化/工具列表 API |
| `mcp/runtime/McpToolInvoker.java` | P1 | MCP | `callTool` 参数校验（SDK 2.0 服务端 schema 校验更严） |
| `mcp/support/McpTransportUrlSupport.java` | P2 | — | 纯 URL 逻辑，无 SDK 类型 |
| `pom.xml` | P0 | DEPS | `spring-ai-starter-vector-store-pgvector`、`spring-ai-advisors-vector-store`、`spring-ai-tika-document-reader`、`spring-ai-starter-mcp-client`；**删除** 对 `mcp:0.17.1` 的 dependencyManagement 覆盖 |

---

## 五、quickboot-workflow（LLM 三节点，8 文件）

| 文件 | 级别 | 类型 | 改动点 |
|------|------|------|--------|
| `handler/LlmNodeHandler.java` | P0 | AI | `ChatClient` 流式 `.stream().content()`；`OpenAiChatOptions` + `defaultToolCallbacks` 合并逻辑（2.0 Tool 行为）；MCP 注入 |
| `handler/QuestionClassifierNodeHandler.java` | P1 | AI | `ChatClient` 单次 call + `WorkflowTokenUsageSupport` |
| `handler/ParameterExtractorNodeHandler.java` | P1 | AI | 同上 |
| `support/WorkflowTokenUsageSupport.java` | P1 | AI | `ChatClient.CallResponseSpec`/`chatResponse()`/`Usage` metadata API |
| `support/WorkflowAiGuard.java` | P2 | AI | `ChatModel` 委托 |
| `util/QuestionClassifierDataUtil.java` | P2 | — | 注释提及 ChatModel，无 import |
| `pom.xml` | P0 | DEPS | `spring-ai-starter-model-openai` |

---

## 六、quickboot-ai-app（智能体 Chat + Tool，8 文件）

| 文件 | 级别 | 类型 | 改动点 |
|------|------|------|--------|
| `service/impl/AiAppChatServiceImpl.java` | P0 | AI | `ChatClient`；`Message`/`UserMessage`/`AssistantMessage`；`defaultToolCallbacks`；`ChatOptions`；Tool 循环与 `maxToolCalls` |
| `service/impl/AiAppVariableServiceImpl.java` | P1 | AI | `ChatClient` 变量抽取 call |
| `tool/WorkflowToolFactory.java` | P1 | AI | 自定义 `ToolCallback` + `ToolDefinition.builder()` |
| `tool/KnowledgeSearchToolFactory.java` | P1 | AI | 同上 |
| `support/QwenWebSearchSupport.java` | P1 | AI | `OpenAiChatOptions.builder()` 联网搜索扩展字段 |
| `support/AiAppAiGuard.java` | P2 | AI | `ChatModel` + YAML 回落 Provider |
| `pom.xml` | P0 | DEPS | `spring-ai-starter-model-openai` |

---

## 七、quickboot-web（配置 + 测试）

| 文件 | 级别 | 类型 | 改动点 |
|------|------|------|--------|
| `resources/application-dev.yml` | P0 | AI | `spring.ai.openai.*` 配置结构（`OpenAiCommonProperties` 拆分）；`completions-path` 等字段名；exclude 类全名：`PgVectorStoreAutoConfiguration` 新包路径 |
| `resources/application.yml` | P2 | — | `qc.knowledge.mcp` 等业务配置不变 |
| `config/JacksonTimeConfig.java` | P0 | JK | `com.fasterxml.jackson`→`tools.jackson`；`ObjectMapper`→`JsonMapper` |
| `test/.../JacksonTimeConfigTest.java` | P0 | JK | 同上 |
| `test/.../DelimiterTokenChunkSplitterTest.java` | P1 | AI | `Document` import |
| `test/.../TextPreprocessorTest.java` | P1 | AI | `Document` import |
| `pom.xml` | P1 | BOOT | `spring-boot-maven-plugin` 版本；`spring-boot-devtools` Boot4 兼容 |

---

## 八、quickboot-common（Jackson 3 波及面，Boot4 连带，20+ 文件）

> Boot 4 强制 Jackson 3，与 Spring AI 无直接关系，但是全项目最大改动面之一。

| 文件 | 级别 | 类型 | 改动点 |
|------|------|------|--------|
| `desensitization/SensitiveJacksonAutoConfiguration.java` | P0 | JK | 自定义 Module/Serializer 迁 `tools.jackson` |
| `desensitization/SensitiveJacksonModule.java` | P0 | JK | 同上 |
| `desensitization/SensitiveStringSerializer.java` | P0 | JK | 同上 |
| `desensitization/SensitiveBeanSerializerModifier.java` | P0 | JK | 同上 |
| `file/url/FileUrl.java` | P0 | JK | 注解/import |
| `file/url/FileUrlSerializer.java` | P0 | JK | Serializer |
| `file/url/FileUrlDeserializer.java` | P0 | JK | Deserializer |
| `file/url/FileUrlAnnotationIntrospector.java` | P0 | JK | Introspector |
| `file/FileStorageAutoConfiguration.java` | P1 | JK | ObjectMapper Bean |
| `cache/DynamicTtlRedisCacheManager.java` | P1 | JK | Redis 序列化 ObjectMapper |
| `cache/QuickbootCacheAutoConfiguration.java` | P1 | JK | 同上 |
| `security/firewall/xss/XssFirewallFilter.java` | P1 | JK | JSON body 解析 |
| `security/firewall/xss/XssFirewallAutoConfiguration.java` | P1 | JK | ObjectMapper 注入 |
| `security/firewall/sqlinjection/SqlInjectionFirewallFilter.java` | P1 | JK | 同上 |
| `security/firewall/sqlinjection/SqlInjectionFirewallAutoConfiguration.java` | P1 | JK | 同上 |
| `security/firewall/sensitiveword/SensitiveWordFirewallFilter.java` | P1 | JK | 同上 |
| `security/firewall/sensitiveword/SensitiveWordFirewallAutoConfiguration.java` | P1 | JK | 同上 |
| `security/firewall/sensitiveword/SensitiveWordJsonBodyProcessor.java` | P1 | JK | 同上 |
| `monitor/operlog/OperLogConsolePrintListener.java` | P1 | JK | 日志 JSON 序列化 |
| `monitor/operlog/OperLogPublishingAspect.java` | P1 | JK | 同上 |
| `monitor/operlog/OperLogCaptureAutoConfiguration.java` | P1 | JK | 同上 |
| `servlet/ServletUtils.java` | P1 | JK | 读写 JSON |
| `test/**`（约 10 个测试类） | P1 | JK | import + 断言跟随 |

---

## 九、quickboot-system / 其他模块

| 文件 | 级别 | 类型 | 改动点 |
|------|------|------|--------|
| `system/operlog/support/OperLogAssembler.java` | P1 | JK | ObjectMapper |
| `system/exporttask/support/ExportSubmitResponseWriter.java` | P1 | JK | 同上 |
| `monitor/clienttrack/.../SysClientTrackServiceImpl.java` | P1 | JK | 同上 |
| `monitor/tracechain/.../SysTraceChainServiceImpl.java` | P1 | JK | 同上 |
| `quickboot-report/pom.xml` | P0 | BOOT | JimuReport Boot3 starter 是否有 Boot4 版 |
| `quickboot-common/pom.xml` | P1 | BOOT | `sa-token-spring-boot3-starter`→boot4 |

---

## 十、风险优先级与建议验证顺序

```text
① 三方生态   MyBatis-Plus / Sa-Token / JimuReport / Druid 是否有 Boot4 starter
② MCP 2.0    StatelessStreamableHttpTransport + ModelScope + STDIO 全场景
③ PGVector   入库/检索/Hybrid/删库删文档
④ ChatClient Tool 循环   ai-app + workflow LlmNode + RagService
⑤ 流式 SSE   LlmNodeHandler llm_delta
⑥ Jackson 3  脱敏/FileUrl/防火墙/OperLog 全链路 JSON
⑦ 模型工厂   DeepSeek completions-path / 通义 embedding dimensions / Ollama
```

---

## 十一、双栈策略交叉引用

Spring AI 2.0 + Boot 4 与 **AgentScope Java 2.0** 的分工、阶段划分、验收标准已写入本文档最上方 **「详细迁移方案」**（§1 目标与策略、§3 能力分工、§4 分阶段实施、§8 风险与对策）。影响面扫描清单仅覆盖 Spring AI / Boot 4 / MCP 2.0 / Jackson 3 侧改动点。
---

## 十二、文档维护

| 项 | 说明 |
|----|------|
| 来源 | 基于 `quickboot/` 仓库静态扫描（2026-06-24） |
| 更新时机 | Boot / Spring AI / MCP SDK 版本 bump 后；或新增 AI 模块文件后 |
| 下一步可选 | 落 OpenSpec change tasks；或拆「仅 Spring AI 1.1.x（Boot 3.5 不动）」缩小范围子文档 |
