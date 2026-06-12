## Context

- 仓库为 Spring Boot 3.5.3 + MySQL + Sa-Token + Vue 3 管理端，尚无 Spring AI 集成。
- 已定稿设计见 `docs/superpowers/specs/2026-06-06-knowledge-rag-design.md`；技术选型：**PGVector + Ollama + 四页管理端 + 全局可见（4A）**。
- 已有能力可复用：`FileTemplate`/`sys_file` 文件登记、导入/导出异步任务模式（线程池 + Semaphore）、Flyway 迁移、C7JsonTable 列表页模板。

## Goals / Non-Goals

**Goals:**

- P0 交付：知识库 CRUD、文档上传异步入库、语义检索、非流式 RAG 问答、管理端四页、菜单权限。
- 向量与业务元数据分离：MySQL 存映射，PGVector 存 embedding。
- 本地部署：Ollama 提供 Chat（`qwen2.5:7b`）与 Embedding（`nomic-embed-text`，768 维）。
- 可通过 `qc.knowledge.enabled` 开关在无 AI 环境构建。

**Non-Goals:**

- 部门/库级 ACL、SSE 流式、混合检索 BM25、Agent、云端 LLM、多模态 OCR、会话持久化表。

## Decisions

### D1：模块边界 — 新增 `quickboot-knowledge`

- **选择**：独立 Maven 模块，由 `quickboot-web` 依赖引入。
- **理由**：AI/RAG 与 system/tools 域解耦；便于按 `qc.knowledge.enabled` 条件装配。
- **备选**：放入 `quickboot-tools` — 拒绝，职责混杂。

### D2：向量库 — PGVector 独立 Postgres

- **选择**：专用 Postgres（pgvector 扩展），Spring AI `spring-ai-starter-vector-store-pgvector`；独立 `DataSource`，不与 MyBatis 主库混用。
- **理由**：Spring AI 一等支持；HNSW 索引适合生产；与现有 MySQL 架构无冲突。
- **备选**：SimpleVectorStore — 仅 dev 可选 profile，不作为 P0 主路径。

### D3：LLM 运行时 — Ollama

- **选择**：`spring-ai-starter-model-ollama`；Chat + Embedding 均走 Ollama。
- **配置**：`base-url: http://127.0.0.1:11434`；`ollama-required: false` 时 AI 接口返回明确业务错误而非启动失败。

### D4：文档入库 — 异步流水线

- **选择**：上传同步返回 `docId`/`taskId`；后台 `IngestTaskExecutor`（参考 `ImportAsyncConfiguration`：独立线程池 + Semaphore）。
- **流水线**：读文件 → TikaDocumentReader → TokenTextSplitter（按库 `chunk_size`/`chunk_overlap`）→ EmbeddingModel → VectorStore.add → 写 `kb_document_chunk`。
- **重索引/删除**：先按 metadata `docId` 删 PGVector 向量，再更新/逻辑删 MySQL。

### D5：RAG — QuestionAnswerAdvisor + metadata 过滤

- **选择**：`ChatClient` + `QuestionAnswerAdvisor`；检索 `SearchRequest` 带 `kbId` FilterExpression。
- **P0**：非流式 `POST /knowledge/chat`；返回答案 + `citations[]`。
- **系统提示词**：仅依据检索上下文；无相关内容时明确说明；附带引用。

### D6：权限 — 全局可见（4A）

- **选择**：Sa-Token 菜单/按钮权限；**不做** `dept_id` 或库级成员 ACL。
- **检索/RAG**：请求必须带 `kbId`，向量 metadata 过滤防跨库串数据。

### D7：文件存储 — classify `knowledge`

- **选择**：复用 `FileTemplate.upload(file, "knowledge")`；`qc.file.classifies` 新增项：`limitExt=pdf,doc,docx,md,txt`，`limitSize=52428800`（50MB）。
- **关联**：`kb_document.file_id` → `sys_file.file_id`。

### D8：PGVector Document Metadata

```json
{ "kbId", "docId", "chunkId", "fileName", "pageNumber" }
```

### D9：前端四页

| 路由 | 页面 | 模式 |
|------|------|------|
| `/knowledge/base` | 知识库 CRUD | C7JsonTable，参照 `system/config/index.vue` |
| `/knowledge/document` | 文档管理 | 列表 + 上传 + 状态轮询 + 重索引 |
| `/knowledge/search` | 语义检索 | 选库 + 查询 + 片段列表 |
| `/knowledge/chat` | RAG 问答 | 选库 + 问答 + 引用卡片 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| Ollama/PG 未就绪 | `ollama-required=false`；接口层检测并返回业务错误码 |
| 大文件入库耗时 | 异步 + 并发限流；前端展示 `doc_status`/task 进度 |
| MySQL 与 PGVector 不一致 | 删除/重索引「先删 vector」；提供 `reindex` |
| Spring AI 与 Boot 3.5.3 兼容性 | 锁定 Spring AI 1.0.x BOM；实施首任务做依赖冒烟 |
| 额外 PG 运维 | 提供 docker-compose；生产独立备份 |
| 全局可见（4A） | P0 简化实现；Phase 2 可加 `dept_id` + metadata 扩展 |

## Migration Plan

1. **开发**：`docker compose up pgvector`；本地安装 Ollama 并 `pull` 模型。
2. **Flyway**：先跑 MySQL 表与菜单；PGVector schema 由 `spring.ai.vectorstore.pgvector.initialize-schema=true` 自动初始化。
3. **部署**：生产增加 PGVector 实例与环境变量 `VECTOR_DB_PASSWORD`；Ollama 可部署于 GPU 节点。
4. **回滚**：`qc.knowledge.enabled=false` 关闭功能；MySQL 表可保留；PGVector 数据可独立清理。

## Open Questions

- （无阻塞 P0 的开放项；SSE 流式、部门隔离留 Phase 2。）
