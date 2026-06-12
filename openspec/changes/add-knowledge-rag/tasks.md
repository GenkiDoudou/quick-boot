## 1. 模块与依赖



- [x] 1.1 在父 `pom.xml` 引入 Spring AI 1.0.x BOM，并注册 `quickboot-knowledge` 子模块

- [x] 1.2 创建 `quickboot-knowledge` 模块骨架（config/controller/service/mapper/domain/dto/ingest/rag）

- [x] 1.3 添加依赖：`spring-ai-starter-model-ollama`、`spring-ai-starter-vector-store-pgvector`、`spring-ai-tika-document-reader`；`quickboot-web` 引入该模块

- [x] 1.4 验证 `mvn -pl quickboot-web -am clean compile -DskipTests` 通过（依赖兼容性冒烟）



## 2. 配置与基础设施



- [x] 2.1 新增 `KnowledgeProperties`（`qc.knowledge.*`）与 `@ConditionalOnProperty(enabled=true)` 自动配置

- [x] 2.2 配置 PGVector 独立 `DataSource`（勿与 MyBatis 主库混用）及 Spring AI vectorstore 属性

- [x] 2.3 在 `application-dev.yml` 增加 Ollama、PGVector、`qc.knowledge` 示例配置

- [x] 2.4 在 `qc.file.classifies` 增加 `knowledge` 分类（pdf/doc/docx/md/txt，50MB）

- [x] 2.5 提供 `docker-compose` 或文档片段用于本地 PGVector（pg16 + pgvector）



## 3. 数据库与权限种子



- [x] 3.1 Flyway 迁移：创建 `kb_knowledge_base`、`kb_document`、`kb_document_chunk`、`kb_ingest_task` 表（含注释与索引）

- [x] 3.2 Flyway 迁移：插入「知识管理」菜单（四页）及按钮权限，与后端 `@SaCheckPermission` 标识一致



## 4. 领域模型与 Mapper



- [x] 4.1 实现 Entity/Mapper：`KbKnowledgeBase`、`KbDocument`、`KbDocumentChunk`、`KbIngestTask`

- [x] 4.2 实现 Bo/Vo/QueryBo 及分组校验（AddGroup/UpdateGroup）



## 5. 知识库 CRUD



- [x] 5.1 实现 `KbKnowledgeBaseService`（分页、详情、增删改、停用校验）

- [x] 5.2 实现 `KbKnowledgeBaseController`（`/knowledge/base/**`）及 OpenAPI 注解

- [x] 5.3 删除知识库时级联删文档向量与关联记录



## 6. 文档入库流水线



- [x] 6.1 实现 `IngestAsyncConfiguration`（线程池 + Semaphore，参考 import 模块）

- [x] 6.2 实现 `DocumentIngestionService`：Tika 解析 → TokenTextSplitter → Embedding → VectorStore.add

- [x] 6.3 实现 `IngestTaskExecutor`：更新 task/doc 状态、写 chunk 记录、失败写 error_msg

- [x] 6.4 实现 `KbDocumentService`：upload（FileTemplate + 创建 doc/task）、reindex、remove（先删 vector）

- [x] 6.5 实现 `KbDocumentController` 与 `KbIngestTaskController`（`/knowledge/doc/**`、`/knowledge/task/getInfo`）



## 7. 检索与 RAG



- [x] 7.1 实现 `KnowledgeSearchService`：`POST /knowledge/search`（kbId filter、topK、threshold）

- [x] 7.2 实现 `RagService`：`ChatClient` + `QuestionAnswerAdvisor`，系统提示词与 citations 组装

- [x] 7.3 实现 `KnowledgeChatController`：`POST /knowledge/chat`（P0 非流式）

- [x] 7.4 Ollama 不可用时返回明确业务错误码（`ollama-required=false` 场景）



## 8. 前端 API 与页面



- [x] 8.1 新增 `quick-ui/src/api/knowledge/`（base、doc、task、search、chat）

- [x] 8.2 实现 `views/knowledge/base/index.vue`（C7JsonTable CRUD）

- [x] 8.3 实现 `views/knowledge/document/index.vue`（上传、状态、重索引、删除、进度轮询）

- [x] 8.4 实现 `views/knowledge/search/index.vue`（选库 + 检索结果）

- [x] 8.5 实现 `views/knowledge/chat/index.vue`（选库 + 问答 + 引用卡片）

- [x] 8.6 菜单路由与 `v-hasPermi` 与后端权限对齐



## 9. 验证与文档



- [ ] 9.1 联调：Ollama + PGVector 下完成「建库 → 上传 PDF → INDEXED → 检索 → RAG 问答」全链路

- [ ] 9.2 验证删除文档/知识库后向量不可检索

- [x] 9.3 后端 `mvn -pl quickboot-web -am clean install -DskipTests` 与前端 `pnpm build:prod` 通过

