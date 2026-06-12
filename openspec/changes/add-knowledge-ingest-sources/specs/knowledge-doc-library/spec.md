## ADDED Requirements

### Requirement: 知识文档库目录（kb_doc_library_folder）

系统 MUST 持久化知识文档库目录树（表 `kb_doc_library_folder`），包含 `folder_id`、`parent_id`（0 为根）、`name`、`order_num`、标准审计字段与 `deleted` 逻辑删除。目录名在同一父目录下 MUST 唯一（未删除记录）。

#### Scenario: 创建子目录成功
- **WHEN** 具备 `knowledge:library:add` 权限的用户在父目录下提交合法目录名
- **THEN** 新增一条 `deleted=0` 的目录记录且返回 `folderId`

#### Scenario: 删除非空目录被拒绝
- **WHEN** 目录下仍存在未删除的文件或子目录
- **THEN** 删除请求 MUST 被拒绝并返回可识别业务错误

### Requirement: 知识文档库文件（kb_doc_library_file）

系统 MUST 持久化文档库文件条目（表 `kb_doc_library_file`），包含 `lib_file_id`、`folder_id`、`file_id`（关联 `sys_file`）、`title`、`file_ext`、`file_size`、`remark`、审计与 `deleted`。物理上传 MUST 使用 `FileTemplate.upload(..., "knowledge-library")` 且 `sys_file` 自动登记。

#### Scenario: 上传到文档库成功
- **WHEN** 用户向某目录上传允许扩展名内的文件
- **THEN** 创建 `kb_doc_library_file` 记录且关联非空 `file_id`

#### Scenario: 不允许的扩展名被拒绝
- **WHEN** 上传文件扩展名不在 `qc.knowledge.library.allowed-extensions` 配置内
- **THEN** 上传 MUST 失败并返回可识别业务错误

### Requirement: 文档库管理 API

系统 SHALL 提供以下接口（前缀 `/knowledge/library`），使用 Sa-Token 权限校验：

| 路径 | 方法 | 权限 |
|------|------|------|
| `/folder/tree` | GET | `knowledge:library:list` |
| `/folder/add` | POST | `knowledge:library:add` |
| `/folder/update` | POST | `knowledge:library:edit` |
| `/folder/remove` | POST | `knowledge:library:remove` |
| `/file/list` | GET | `knowledge:library:list` |
| `/file/upload` | POST | `knowledge:library:upload` |
| `/file/remove` | POST | `knowledge:library:remove` |

#### Scenario: 分页列出目录下文件
- **WHEN** 用户请求某 `folderId` 的文件列表
- **THEN** 返回该目录下未删除文件的分页结果

### Requirement: 文档库管理端页面

系统前端 MUST 提供 `views/knowledge/library/index.vue`：左侧目录树、右侧文件列表，支持目录 CRUD、文件上传与删除，并遵循 `DESIGN.md` 与列表页模板规范。

#### Scenario: 文档库菜单可访问
- **WHEN** 用户具备 `knowledge:library:list` 权限
- **THEN** 侧边栏「知识管理」下可见「文档库」菜单并可打开页面

### Requirement: 文件分类 knowledge-library

系统 MUST 在 `qc.file.classifies` 中注册 `knowledge-library` 分类，扩展名与大小限制与 `qc.knowledge.library` 配置一致，且与直接上传到知识库的 `knowledge` 分类分离。

#### Scenario: 文档库上传走独立分类
- **WHEN** 用户通过文档库上传接口上传文件
- **THEN** `sys_file.classify` 值为 `knowledge-library`
