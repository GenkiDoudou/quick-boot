## ADDED Requirements

### Requirement: 知识库默认分段策略

系统 MUST 在 `kb_knowledge_base` 持久化默认分段与预处理配置：`segment_mode`（`AUTO`/`CUSTOM`，默认 `AUTO`）、`chunk_delimiter`（`SINGLE_NEWLINE`/`DOUBLE_NEWLINE`，默认 `DOUBLE_NEWLINE`）、`preprocess_normalize_ws`（默认 1）、`preprocess_remove_url`（默认 0）、`preprocess_remove_email`（默认 0）。现有 `chunk_size`、`chunk_overlap`（Token，默认 800/120）MUST 保留。

#### Scenario: 创建知识库带默认分段配置
- **WHEN** 用户创建知识库且未指定分段字段
- **THEN** 记录中 `segment_mode=AUTO` 且预处理默认值与设计一致

### Requirement: 文档来源类型与策略快照

系统 MUST 在 `kb_document` 持久化：`source_type`（`FILE`/`MANUAL`/`WEB`/`LIBRARY`）、可选 `source_url`、可选 `library_file_id`、以及入库时快照的分段与预处理字段（`segment_mode`、`chunk_size`、`chunk_overlap`、`chunk_delimiter`、`preprocess_*`）。`file_id` MAY 为空仅当来源尚未产生归档文件；入库流程完成后 MUST 可关联 `sys_file`（手动/网页归档）或共享文档库 `file_id`（LIBRARY）。

#### Scenario: 手动录入保存快照
- **WHEN** 用户添加手动文档并覆盖 `chunkSize=600`
- **THEN** `kb_document` 记录 `source_type=MANUAL` 且 `chunk_size=600` 作为快照

#### Scenario: 从文档库选文件溯源
- **WHEN** 用户从文档库选 `libFileId` 入库
- **THEN** `kb_document.source_type=LIBRARY` 且 `library_file_id` 非空，`file_id` 与文档库条目共享

### Requirement: 多来源文档入库 API

系统 SHALL 在 `/knowledge/doc` 提供以下入库入口（均可选 `SegmentConfigBo`，缺省继承知识库默认并快照到文档）：

| 路径 | 说明 |
|------|------|
| `POST /upload` | 文件上传（现有，扩展 SegmentConfig） |
| `POST /addManual` | `{ kbId, title, content }`，content ≤512KB |
| `POST /addFromWeb` | `{ kbId, url, title? }`，后端抓取 |
| `POST /addFromLibrary` | `{ kbId, libFileId, title? }` |

各接口 MUST 同步返回 `docId` 与 `taskId`，并触发异步入库。

#### Scenario: 网页 URL 入库成功
- **WHEN** 用户提交可访问的公网 https URL
- **THEN** 创建 `source_type=WEB` 的文档且 `source_url` 非空，异步入库后可达 `INDEXED`

#### Scenario: 内网 URL 被拒绝
- **WHEN** 用户提交指向 RFC1918 或 loopback 的 URL
- **THEN** 请求 MUST 失败且 `error_msg` 含网页抓取失败说明

### Requirement: 文本预处理

入库流水线 MUST 在分块前按文档快照的预处理开关顺序执行：归一化连续空白（空格/换行/制表符）、删除 URL、删除电子邮箱。自动与自定义分段模式 MUST 共用同一预处理配置。

#### Scenario: 开启去 URL 后入库
- **WHEN** 文档快照 `preprocess_remove_url=1` 且正文含 `https://example.com`
- **THEN** 写入向量的分块文本 MUST NOT 含该 URL 字符串

### Requirement: 自定义分段（CUSTOM）

当文档快照 `segment_mode=CUSTOM` 时，系统 MUST 先按 `chunk_delimiter` 切分文本，再按 Token 上限 `chunk_size` 合并或切分段落，并应用 Token 重叠 `chunk_overlap`。当 `segment_mode=AUTO` 时，系统 MUST 使用 `TokenTextSplitter` 且参数取自文档快照。

#### Scenario: 双换行自定义分段
- **WHEN** 文档快照为 CUSTOM 且 `chunk_delimiter=DOUBLE_NEWLINE`
- **THEN** 分块边界 MUST 优先对齐双换行段落，且每块 Token 不超过 `chunk_size`

### Requirement: 重建索引使用文档快照

`POST /knowledge/doc/reindex` MUST 使用目标文档上的分段与预处理快照字段，MUST NOT 在重建时自动改用知识库当前默认配置。

#### Scenario: 修改库默认后 reindex 仍用快照
- **WHEN** 文档入库后管理员修改知识库 `chunk_size`，再对该文档 reindex
- **THEN** 重建分块仍按文档原快照参数执行

### Requirement: 添加文档向导页面

系统前端 MUST 改造 `views/knowledge/document/index.vue`：提供「添加文档」三步向导（来源 Tab：文件/手动/网页/文档库 → 分段与清洗 → 提交），列表 MUST 展示 `sourceType`（及 WEB 的 `sourceUrl` 摘要）。

#### Scenario: 四来源均可从向导提交
- **WHEN** 用户依次完成向导三步并提交
- **THEN** 调用对应 API 且列表出现带正确来源标签的 PENDING 文档

## MODIFIED Requirements

### Requirement: 知识库元数据（kb_knowledge_base）

系统 MUST 在 MySQL 中持久化知识库记录（表 `kb_knowledge_base`），至少包含：`kb_id`、`name`、`description`、`chunk_size`（默认 800）、`chunk_overlap`（默认 120）、`segment_mode`（默认 AUTO）、`chunk_delimiter`（默认 DOUBLE_NEWLINE）、`preprocess_normalize_ws`（默认 1）、`preprocess_remove_url`（默认 0）、`preprocess_remove_email`（默认 0）、`status`（0 正常 / 1 停用）、标准审计字段与 `deleted` 逻辑删除标记。

#### Scenario: 创建知识库成功
- **WHEN** 具备 `knowledge:base:add` 权限的用户提交合法名称与分块参数
- **THEN** 数据库新增一条 `deleted=0` 的知识库记录且返回 `kbId`

#### Scenario: 停用知识库不可用于入库
- **WHEN** 知识库 `status=1`（停用）
- **THEN** 向该库上传或添加文档的请求 MUST 被拒绝并返回可识别业务错误

### Requirement: 文档元数据与文件关联（kb_document）

系统 MUST 持久化文档记录（表 `kb_document`），关联 `kb_id`，包含 `source_type`（FILE/MANUAL/WEB/LIBRARY）、`title`、`doc_status`（PENDING/PARSING/INDEXED/FAILED）、`chunk_count`、`error_msg`、分段与预处理快照字段、可选 `file_id`、可选 `library_file_id`、可选 `source_url` 及逻辑删除字段。FILE 来源 MUST 通过 `FileTemplate.upload(..., "knowledge")` 完成且 `sys_file` 自动登记；MANUAL/WEB 归档文件与 LIBRARY 共享 `file_id` 规则见 ADDED「文档来源类型与策略快照」。

#### Scenario: 文件上传后产生 PENDING 文档
- **WHEN** 用户向正常知识库上传合法 pdf 文件
- **THEN** 创建 `kb_document` 记录且 `doc_status=PENDING`、`source_type=FILE`，并关联非空 `file_id`

#### Scenario: 手动录入产生 PENDING 文档
- **WHEN** 用户提交合法 title 与 content
- **THEN** 创建 `source_type=MANUAL` 的 PENDING 文档且 title 与请求一致

### Requirement: 文档管理 API

系统 SHALL 提供以下接口（前缀 `/knowledge/doc`）：

| 路径 | 方法 | 权限 |
|------|------|------|
| `/list` | GET | `knowledge:doc:list` |
| `/upload` | POST | `knowledge:doc:upload` |
| `/addManual` | POST | `knowledge:doc:upload` |
| `/addFromWeb` | POST | `knowledge:doc:upload` |
| `/addFromLibrary` | POST | `knowledge:doc:upload` |
| `/reindex` | POST | `knowledge:doc:reindex` |
| `/remove` | POST | `knowledge:doc:remove` |

`/upload` MUST 接受 `kbId` 与 `MultipartFile`，可选 `SegmentConfigBo`。`/reindex` MUST 先删除该 `docId` 的旧向量再按文档快照重建。`/remove` MUST 删除向量并逻辑删文档记录。

#### Scenario: 重索引替换旧向量
- **WHEN** 对已 INDEXED 文档执行 reindex
- **THEN** 旧 `vector_id` 对应向量被移除且新向量写入，`chunk_count` 反映最新分块数

### Requirement: 管理端四页

系统前端 MUST 提供以下页面，并遵循 `DESIGN.md` 与列表页模板（默认参照 `views/system/config/index.vue`）：

- `views/knowledge/base/index.vue` — 知识库 CRUD（含默认分段/预处理配置）
- `views/knowledge/library/index.vue` — 知识文档库（目录 + 文件）
- `views/knowledge/document/index.vue` — 文档列表、添加文档向导、状态展示、重索引、删除
- `views/knowledge/search/index.vue` — 选库 + 检索 + 片段结果
- `views/knowledge/chat/index.vue` — 选库 + 问答 + 引用展示

文档状态为 `PENDING` 或 `PARSING` 时，前端 SHOULD 轮询任务进度或刷新列表直至终态。

#### Scenario: 文档入库进度可见
- **WHEN** 用户通过任一来添加入口创建文档并返回 taskId
- **THEN** 文档列表或任务查询可展示从 PENDING/PARSING 到 INDEXED/FAILED 的状态变化

## MODIFIED Requirements (配置)

### Requirement: 功能开关 qc.knowledge.enabled

当 `qc.knowledge.enabled=false` 时，系统 MUST NOT 注册依赖 Embedding/PGVector 的 Knowledge 业务 Bean（或提供安全空实现），且 MUST NOT 暴露知识管理 REST 端点（含 `/knowledge/library/**`）。

#### Scenario: 关闭开关后无 AI 端点
- **WHEN** 配置 `qc.knowledge.enabled=false` 且应用启动成功
- **THEN** `/knowledge/**` 路由不可用或返回功能未启用错误
