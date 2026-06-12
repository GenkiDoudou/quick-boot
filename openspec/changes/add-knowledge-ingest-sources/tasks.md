## 1. 数据库与配置



- [x] 1.1 编写 Flyway `V59__knowledge_ingest_sources.sql`：扩展 `kb_knowledge_base`、`kb_document`；新建 `kb_doc_library_folder`、`kb_doc_library_file`；历史数据回填；菜单 2295+ 与 `knowledge:library:*` 权限

- [x] 1.2 在 `qc.file.classifies` 增加 `knowledge-library` 分类

- [x] 1.3 扩展 `KnowledgeProperties`：`web-fetch`、`library` 配置块；更新 `application.yml` / `application-dev.yml` 示例



## 2. 领域模型与枚举



- [x] 2.1 新增枚举：`KbDocSourceType`、`KbSegmentMode`、`KbChunkDelimiter`

- [x] 2.2 扩展 Entity/Mapper/Vo/Bo：`KbKnowledgeBase`、`KbDocument`；新增 `KbDocLibraryFolder`、`KbDocLibraryFile` 及 Mapper

- [x] 2.3 新增 `SegmentConfigBo` 与快照合并工具 `SegmentConfigResolver`（KB 默认 + 请求覆盖 → 文档快照）



## 3. 入库流水线重构



- [x] 3.1 实现 `TextPreprocessor`（normalize_ws / remove_url / remove_email）及单元测试

- [x] 3.2 实现 `DelimiterTokenChunkSplitter`（CUSTOM 模式）及单元测试

- [x] 3.3 实现 `ChunkStrategy` 门面（AUTO → TokenTextSplitter；CUSTOM → DelimiterTokenChunkSplitter）

- [x] 3.4 实现 `DocumentSourceAdapter` 四种实现：File / Manual / Web / Library

- [x] 3.5 实现 `WebContentFetcher`（SSRF、超时、大小限制）及单元测试

- [x] 3.6 重构 `DocumentIngestionService`：读文档快照 → 适配器 → 预处理 → 分块 → 向量化

- [x] 3.7 验证 `reindex` 使用文档快照字段



## 4. 文档库 API



- [x] 4.1 实现 `KbDocLibraryFolderService` + Controller（tree/add/update/remove）

- [x] 4.2 实现 `KbDocLibraryFileService` + Controller（list/upload/remove）

- [x] 4.3 补充 OpenAPI 注解与权限 `@SaCheckPermission`



## 5. 文档入库 API 扩展



- [x] 5.1 扩展 `KbDocumentService.upload` 支持可选 `SegmentConfigBo` 与快照写入

- [x] 5.2 实现 `addManual`、`addFromWeb`、`addFromLibrary` 及 `KbDocumentController` 端点

- [x] 5.3 扩展 `GET /list` 返回 `sourceType`、`sourceUrl` 等字段



## 6. 知识库 API 扩展



- [x] 6.1 扩展 `KbKnowledgeBaseService` / Controller / 前端表单字段（默认分段与预处理）

- [x] 6.2 扩展知识库列表/详情 Vo 展示 `segmentMode`



## 7. 前端 — 文档库页



- [x] 7.1 新增 `api/knowledge/library.js`

- [x] 7.2 新增 `views/knowledge/library/index.vue`（左树右表、目录与文件 CRUD）

- [x] 7.3 注册路由与菜单（与 Flyway 菜单 id 一致；动态菜单 `knowledge/library/index`，无需静态路由）



## 8. 前端 — 文档管理与知识库表单



- [x] 8.1 改造 `views/knowledge/document/index.vue`：「添加文档」两步向导（四来源 Tab + 分段 + 提交）

- [x] 8.2 实现文档库选取弹窗（树+表，供 LIBRARY 来源）

- [x] 8.3 列表增加「来源」列与 `sourceUrl` 展示

- [x] 8.4 改造 `views/knowledge/base/index.vue`：默认分段/预处理表单项



## 9. 验证与文档



- [x] 9.1 后端：`mvn -pl quickboot-web -am test`（ingest 相关 4 个测试类通过）

- [x] 9.2 前端：`pnpm build:prod` 通过

- [ ] 9.3 联调：四来源各一条 + CUSTOM 分段 + 预处理 + reindex 快照（需本地启动后端/DB/PGVector 人工验证）

- [x] 9.4 更新 `docs/superpowers/specs/2026-06-07-knowledge-ingest-sources-design.md` 状态为已定稿（若与实现一致）

