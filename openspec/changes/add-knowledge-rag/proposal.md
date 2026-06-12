## Why

`quickboot2` 已具备企业后台基础能力（Sa-Token 鉴权、文件上传登记、Flyway 迁移等），但缺少面向业务文档的**知识管理与智能检索**能力。团队需要在**本地部署、数据不出内网**的前提下，对 PDF/Office/Markdown 等文档进行统一管理，并支持语义检索与基于检索增强生成（RAG）的问答，以提升内部知识复用效率。

## What Changes

- 新增 Maven 模块 **`quickboot-knowledge`**，集成 **Spring AI 1.0.x**，对接本地 **Ollama**（Chat + Embedding）与 **PGVector** 向量库。
- 新增 MySQL 元数据表：`kb_knowledge_base`、`kb_document`、`kb_document_chunk`、`kb_ingest_task`（Flyway 迁移）。
- 新增知识库 CRUD、文档上传与**异步入库**（解析 → 分块 → 向量化）、语义检索、RAG 问答 REST API。
- 复用现有 `FileTemplate` 与 `sys_file` 登记，新增上传分类 `knowledge`（pdf/docx/md/txt 等）。
- 新增管理端四页：**知识库 / 文档 / 语义检索 / RAG 问答**，及对应菜单与 Sa-Token 权限。
- 新增 `qc.knowledge.*` 配置与 PGVector 独立数据源；提供开发用 Docker Compose 片段。
- P0 **不做**：部门/库级数据隔离、SSE 流式问答、Agent 工作流、公网 SaaS LLM。

## Capabilities

### New Capabilities

- `knowledge-rag`：知识库管理、文档入库流水线、PGVector 向量检索、Ollama RAG 问答、管理端四页与权限

### Modified Capabilities

（无。文件上传复用 `common-file-storage` 既有分类机制，仅新增 `knowledge` 配置项，不改变规范级行为。）

## Impact

- **后端**：
  - 新增 `quickboot-knowledge` 模块；`quickboot-web` 引入依赖。
  - 父 `pom.xml` 引入 Spring AI BOM 与 Ollama/PGVector/Tika 依赖。
  - Flyway：业务表 + 菜单/权限种子数据。
  - 新增外部运行时依赖：**Ollama**（宿主机或独立节点）、**PostgreSQL + pgvector**（独立实例）。
- **前端**：`quick-ui` 新增 `views/knowledge/` 四页与 `api/knowledge/` 封装。
- **配置**：`application*.yml` 增加 `spring.ai.*`、`qc.knowledge.*`；`qc.file.classifies` 增加 `knowledge` 分类。
- **部署**：开发/生产需额外维护 PGVector 实例；`qc.knowledge.enabled=false` 时可跳过 AI Bean 注册以便 CI 构建。
