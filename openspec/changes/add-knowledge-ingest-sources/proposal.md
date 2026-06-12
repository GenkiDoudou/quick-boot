## Why

P0 知识库（`add-knowledge-rag`）仅支持**本地文件上传 + Token 自动分块**，无法满足多来源内容录入（手动文本、网页、预置文档库）及可配置分段/清洗需求。用户希望对标 Dify 类产品的入库体验，在**不破坏现有检索/RAG 链路**的前提下，一次扩展四种来源与两种分段策略。

## What Changes

- 扩展 `kb_knowledge_base`：默认分段模式（AUTO/CUSTOM）、分隔符、预处理开关（归一化空白 / 去 URL / 去邮箱）。
- 扩展 `kb_document`：来源类型（FILE/MANUAL/WEB/LIBRARY）、策略快照字段、`source_url` / `library_file_id`；`file_id` 改为可空（手动/网页归档后仍有 file）。
- 新增独立**知识文档库**表与 API（目录树 + 文件管理，`classify=knowledge-library`）。
- 新增入库入口：`/doc/addManual`、`/doc/addFromWeb`、`/doc/addFromLibrary`；`/upload` 支持可选 `SegmentConfigBo`。
- 重构 `DocumentIngestionService`：SourceAdapter → TextPreprocessor → ChunkStrategy（AUTO/CUSTOM）→ 向量化。
- 新增 `WebContentFetcher`（URL 抓取 + SSRF 防护）。
- 前端：新增「文档库」页；文档管理改为「添加文档」三步向导；知识库表单增加默认分段配置。
- Flyway `V59`、菜单权限 `knowledge:library:*`、配置 `qc.knowledge.web-fetch` / `library`。
- 历史数据迁移：`source_type=FILE`，策略字段从所属 KB 回填。

## Capabilities

### New Capabilities

- `knowledge-doc-library`：独立知识文档库（目录树、文件上传/删除/列表），物理文件 `classify=knowledge-library`，供知识库文档「从文档库选取」入库

### Modified Capabilities

- `knowledge-rag`：多来源文档入库、分段策略与预处理、策略快照、扩展 API 与管理端页面（文档库页 + 添加向导 + 知识库默认策略）

## Impact

- **后端**：`quickboot-knowledge` 模块（ingest 重构、library 包、Flyway V59）；`application.yml` 增加 web-fetch/library 配置；`qc.file.classifies` 增加 `knowledge-library`。
- **前端**：`quick-ui` 新增 `views/knowledge/library/`；改造 `document/index.vue`、`base/index.vue`；新增 `api/knowledge/library.js`。
- **数据库**：MySQL 表扩展 + 新表；无 PGVector 表结构变更。
- **依赖**：网页抓取可能引入 Jsoup（若 Tika 正文提取不足）；无 Spring AI 版本升级。
- **兼容**：P0 文件上传与异步入库行为保持；`reindex` 使用文档快照策略。
