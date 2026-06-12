## ADDED Requirements

### Requirement: 知识库元数据（kb_knowledge_base）

系统 MUST 在 MySQL 中持久化知识库记录（表 `kb_knowledge_base`），至少包含：`kb_id`、`name`、`description`、`chunk_size`（默认 800）、`chunk_overlap`（默认 120）、`status`（0 正常 / 1 停用）、标准审计字段与 `deleted` 逻辑删除标记。

#### Scenario: 创建知识库成功
- **WHEN** 具备 `knowledge:base:add` 权限的用户提交合法名称与分块参数
- **THEN** 数据库新增一条 `deleted=0` 的知识库记录且返回 `kbId`

#### Scenario: 停用知识库不可用于入库
- **WHEN** 知识库 `status=1`（停用）
- **THEN** 向该库上传文档的请求 MUST 被拒绝并返回可识别业务错误

### Requirement: 文档元数据与文件关联（kb_document）

系统 MUST 持久化文档记录（表 `kb_document`），关联 `kb_id` 与 `sys_file.file_id`，包含 `title`、`doc_status`（`PENDING`/`PARSING`/`INDEXED`/`FAILED`）、`chunk_count`、`error_msg` 及逻辑删除字段。上传 MUST 通过 `FileTemplate.upload(..., "knowledge")` 完成且 `sys_file` 自动登记。

#### Scenario: 上传后产生 PENDING 文档
- **WHEN** 用户向正常知识库上传合法 pdf 文件
- **THEN** 创建 `kb_document` 记录且 `doc_status=PENDING`，并关联非空 `file_id`

### Requirement: 异步入库任务（kb_ingest_task）

系统 MUST 为每次文档入库创建异步任务（表 `kb_ingest_task`），状态为 `QUEUED`/`RUNNING`/`SUCCESS`/`FAILED`，包含 `progress`（0–100）、`retry_count`、`error_msg`、`start_time`/`end_time`。上传接口 MUST 同步返回 `docId` 与 `taskId`，向量化在后台执行。

#### Scenario: 入库成功更新状态
- **WHEN** 异步任务完成解析、分块与向量化
- **THEN** `kb_ingest_task.status=SUCCESS` 且 `kb_document.doc_status=INDEXED`，`chunk_count` 大于 0

#### Scenario: 入库失败记录原因
- **WHEN** 解析或向量化过程中抛出异常
- **THEN** `kb_ingest_task.status=FAILED` 且 `kb_document.doc_status=FAILED`，`error_msg` 非空

### Requirement: 分块记录与向量映射（kb_document_chunk）

系统 MUST 为每个成功入库的分块写入 `kb_document_chunk`，包含 `chunk_index`、`content_preview`（摘要，建议 ≤500 字符）、`vector_id`（PGVector 中 Document id）、可选 `token_count` 与 `page_number`。

#### Scenario: 分块与向量一一映射
- **WHEN** 文档入库成功且产生 N 个分块
- **THEN** `kb_document_chunk` 中存在 N 条对应记录且每条 `vector_id` 非空

### Requirement: PGVector 向量存储与 metadata 过滤

系统 MUST 使用 PGVector 存储 chunk embedding；每个 Spring AI `Document` 的 metadata MUST 包含 `kbId`、`docId`、`chunkId`、`fileName`，可选 `pageNumber`。语义检索与 RAG MUST 通过 FilterExpression 按请求的 `kbId` 过滤，MUST NOT 返回其它知识库的片段。

#### Scenario: 跨库检索隔离
- **WHEN** 用户对 `kbId=1` 发起检索而向量库中存在 `kbId=2` 的数据
- **THEN** 返回结果中不包含 `kbId=2` 的片段

### Requirement: 知识库 CRUD API

系统 SHALL 提供以下接口（前缀 `/knowledge/base`），并使用 Sa-Token 权限校验：

| 路径 | 方法 | 权限 |
|------|------|------|
| `/list` | GET | `knowledge:base:list` |
| `/getInfo` | GET | `knowledge:base:query` |
| `/add` | POST | `knowledge:base:add` |
| `/update` | POST | `knowledge:base:edit` |
| `/remove` | POST | `knowledge:base:remove` |

删除知识库 MUST 级联删除其文档的 PGVector 向量及关联 MySQL 记录（逻辑删）。

#### Scenario: 删除知识库清除向量
- **WHEN** 管理员删除某知识库
- **THEN** 该库下所有文档的向量不可再被检索到

### Requirement: 文档管理 API

系统 SHALL 提供以下接口（前缀 `/knowledge/doc`）：

| 路径 | 方法 | 权限 |
|------|------|------|
| `/list` | GET | `knowledge:doc:list` |
| `/upload` | POST | `knowledge:doc:upload` |
| `/reindex` | POST | `knowledge:doc:reindex` |
| `/remove` | POST | `knowledge:doc:remove` |

`/upload` MUST 接受 `kbId` 与 `MultipartFile`。`/reindex` MUST 先删除该 `docId` 的旧向量再重建。`/remove` MUST 删除向量并逻辑删文档记录。

#### Scenario: 重索引替换旧向量
- **WHEN** 对已 INDEXED 文档执行 reindex
- **THEN** 旧 `vector_id` 对应向量被移除且新向量写入，`chunk_count` 反映最新分块数

### Requirement: 入库任务查询 API

系统 SHALL 提供 `GET /knowledge/task/getInfo?taskId=`，返回任务状态与进度，权限 `knowledge:doc:list`。

#### Scenario: 查询进行中的任务
- **WHEN** 任务处于 RUNNING 且 progress=50
- **THEN** 接口返回 `status=RUNNING` 与 `progress=50`

### Requirement: 语义检索 API

系统 SHALL 提供 `POST /knowledge/search`，接收 `kbId` 与 `query`，权限 `knowledge:search`。系统 MUST 对 query 做 embedding 并在 PGVector 中 similarity search，返回片段列表，每项含 content、score、docId、chunkId、fileName、pageNumber（若有）。

#### Scenario: 检索返回相关片段
- **WHEN** 知识库已 INDEXED 文档且 query 与文档主题相关
- **THEN** 响应包含至少一条 score 高于配置阈值（默认 0.65）的片段

### Requirement: RAG 问答 API

系统 SHALL 提供 `POST /knowledge/chat`，接收 `kbId` 与 `question`，权限 `knowledge:chat`。系统 MUST 使用检索增强生成（RAG）返回答案字符串与 `citations` 数组（含文档名、片段摘要、相似度）。P0 MUST NOT 要求 SSE 流式。

#### Scenario: 问答附带引用
- **WHEN** 知识库存在相关内容且用户提问
- **THEN** 响应中 `citations` 至少包含一条引用记录

#### Scenario: 无相关内容时明确告知
- **WHEN** 检索结果为空或低于阈值
- **THEN** 答案 MUST 明确说明无法从知识库找到依据，且 MUST NOT 捏造引用

### Requirement: 数据可见性（全局可见 4A）

具备知识管理菜单权限的用户 MUST 可查看与检索**全部**知识库；P0 MUST NOT 按部门或库成员过滤列表。内容维护权限由 `knowledge:base:*` 与 `knowledge:doc:*` 按钮权限控制。

#### Scenario: 有列表权限可见全部库
- **WHEN** 用户具备 `knowledge:base:list` 且无部门数据权限组件介入
- **THEN** 分页列表返回系统中全部未删除知识库

### Requirement: 功能开关 qc.knowledge.enabled

当 `qc.knowledge.enabled=false` 时，系统 MUST NOT 注册依赖 Ollama/PGVector 的 Knowledge 业务 Bean（或提供安全空实现），且 MUST NOT 暴露知识管理 REST 端点。

#### Scenario: 关闭开关后无 AI 端点
- **WHEN** 配置 `qc.knowledge.enabled=false` 且应用启动成功
- **THEN** `/knowledge/**` 路由不可用或返回功能未启用错误

### Requirement: 管理端四页

系统前端 MUST 提供以下页面，并遵循 `DESIGN.md` 与列表页模板（默认参照 `views/system/config/index.vue`）：

- `views/knowledge/base/index.vue` — 知识库 CRUD
- `views/knowledge/document/index.vue` — 文档列表、上传、状态展示、重索引、删除
- `views/knowledge/search/index.vue` — 选库 + 检索 + 片段结果
- `views/knowledge/chat/index.vue` — 选库 + 问答 + 引用展示

文档状态为 `PENDING` 或 `PARSING` 时，前端 SHOULD 轮询任务进度或刷新列表直至终态。

#### Scenario: 文档入库进度可见
- **WHEN** 用户上传文档后返回 taskId
- **THEN** 文档列表或任务查询可展示从 PENDING/PARSING 到 INDEXED/FAILED 的状态变化
