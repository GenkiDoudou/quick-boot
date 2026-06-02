## ADDED Requirements

### Requirement: 文件元数据登记（sys_file）

系统 MUST 在数据库中持久化上传文件的元数据记录（表名可为 `sys_file`），记录至少包含：

- `fileId`（主键）
- `originalName`（原始文件名）
- `ext`（扩展名，小写）
- `sizeBytes`（字节大小）
- `contentType`（可空）
- `classify`（分类）
- `relativePath`（`FileTemplate.upload` 返回的相对路径，且 MUST 唯一）
- `uploaderUserId`、`uploaderUserName`（上传人审计）
- `uploadTime`
- `deleted`、`deleteBy`、`deleteTime`（删除审计）

#### Scenario: 上传成功后产生唯一登记
- **WHEN** 任意模块调用 `FileTemplate.upload(...)` 成功返回 `relativePath`
- **THEN** 数据库中存在一条 `relativePath` 等于该值的元数据记录，且 `relativePath` 在表中唯一

### Requirement: 文件管理分页列表

系统 SHALL 提供文件管理分页查询接口，返回未删除文件的列表（默认过滤 `deleted=1`），并包含字段：文件名、大小、扩展名、上传时间、上传人。

#### Scenario: 默认仅返回未删除
- **WHEN** 调用分页列表接口且未显式传入 `deleted` 过滤条件
- **THEN** 返回结果中不包含 `deleted=1` 的记录

### Requirement: 文件管理上传接口

系统 SHALL 提供文件管理上传接口，接收 `MultipartFile`（参数名为 `file`），并可选接收 `classify`；当 `classify` 为空时 MUST 走 `FileTemplate` 的默认分类规则。上传成功后接口 MUST 返回可定位文件的 `fileId`，并包含 `relativePath`。

#### Scenario: 文件管理上传成功返回 fileId
- **WHEN** 管理端调用上传接口上传合法文件
- **THEN** 响应中包含非空 `fileId` 且 `relativePath` 为合法相对路径

### Requirement: 预览 URL 获取

系统 SHALL 提供预览接口以 `fileId` 获取可访问 URL。该 URL MUST 与 `FileTemplate.view(relativePath)`（或等价能力）一致的字符串语义。

#### Scenario: 预览接口返回可访问 URL
- **WHEN** `fileId` 对应的文件记录存在且未删除
- **THEN** 预览接口返回 `url` 字符串，且该字符串可用于浏览器访问（以存储后端能力为准）

### Requirement: 下载接口（附件）

系统 SHALL 提供下载接口以 `fileId` 返回附件内容，且响应头中的文件名 MUST 以 `originalName` 为准（需处理编码与特殊字符）。

#### Scenario: 下载文件名与 originalName 一致
- **WHEN** 下载接口返回附件响应
- **THEN** 响应头中的文件名与登记记录的 `originalName` 语义一致（不乱码）

### Requirement: 删除语义（逻辑删 + 同步删对象）

系统 MUST 提供批量删除接口。删除时系统 MUST：

1. 将元数据记录标记为删除（写入 `deleted=1` 并记录 `deleteBy/deleteTime`）
2. 调用存储后端删除该 `relativePath` 对应对象

#### Scenario: 删除后列表不可见且对象不存在
- **WHEN** 对某 `fileId` 执行删除后再调用分页列表接口
- **THEN** 该记录不出现在默认列表中，且对其 `relativePath` 执行 `exists`（或等价探测）为 false（若对象原已不存在也视为满足）

### Requirement: 管理端预览交互（图片/视频弹窗 + 其它新窗口）

系统前端 MUST 提供文件管理页面，且预览交互满足：

- 图片/视频：以弹窗方式预览（关闭弹窗后视频停止播放）
- 其它文件：以新窗口打开 URL（`window.open(url, '_blank', 'noopener,noreferrer')` 或等价）

#### Scenario: 非图片/视频走新窗口打开
- **WHEN** 用户点击“预览”且文件类型不是图片/视频
- **THEN** 前端使用新窗口打开预览 URL

