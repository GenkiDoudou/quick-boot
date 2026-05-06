## ADDED Requirements

### Requirement: FileTemplate 上传与相对路径

系统 SHALL 提供 `FileTemplate`，并 SHALL 提供 `upload(MultipartFile, String classify)` 与 `upload(byte[] content, String filename, String classify)`（或语义等价的重载）。当 `classify` 为空或仅空白时，系统 MUST 使用配置项 `qc.file.defaultClassify`（默认 `default`）。上传成功后，系统 SHALL 返回相对路径字符串，格式为 `{classify}/{yyyy/MM}/{uuid}.{ext}`，其中 `{yyyy/MM}` 为上传时区下年月，`{uuid}` 为全局唯一，`{ext}` 为自 `filename` 解析并 **转成小写** 的扩展名（无点或无法解析时的行为由实现与单测固定并文档化）。系统 SHALL NOT 将绝对路径或含路径穿越的字符串作为返回值。

#### Scenario: 成功上传得到规范相对路径

- **WHEN** 上传合法文件且分类校验通过
- **THEN** 返回路径匹配 `{classify}/{yyyy/MM}/{uuid}.{ext}` 模式，且扩展名段为小写或无扩展名时的约定形式

#### Scenario: 默认分类

- **WHEN** `classify` 为 null 或空字符串且已配置 `defaultClassify=images`
- **THEN** 生成路径前缀以 `images/` 起始

### Requirement: 分类后缀与大小限制

系统 SHALL 按 `classify` 读取 `qc.file.classifies` 中对应项的 `limitExt`（逗号分隔，不含点或含点均可，比对前 SHALL 归一为小写后缀）与 `limitSize`（字节，默认 10MB）。若不存在该分类配置且为默认分类回退，系统 SHALL 使用内置通用后缀白名单（图片与常见办公文档，具体列表在实现或默认配置中可查）。系统 MUST 在写入存储前拒绝：后缀不在白名单，或 **有效内容大小** 超过 `limitSize`。拒绝时系统 SHALL 抛出异常（由全局异常处理转换），错误信息 SHALL 可区分后缀非法与超大。

#### Scenario: 后缀不在白名单

- **WHEN** `limitExt` 仅允许 `png,jpg` 且上传文件归一化后缀为 `exe`
- **THEN** 抛出异常，且不产生存储对象

#### Scenario: 超出大小

- **WHEN** `limitSize=1024` 且实际上传内容大于 1024 字节
- **THEN** 抛出异常，且不产生存储对象

### Requirement: 相对路径安全

系统 SHALL 拒绝作为存储键使用的相对路径：包含 `..` 段、以 `/` 开头、或经规范化后逾越约定前缀的路径；`exists` / `download` / `delete` / `view` 等读取类操作在收到非法相对路径时 SHALL 抛出异常或返回不存在（二者择一并在实现与单测中一致，且 MUST NOT 访问存储根目录之外）。

#### Scenario: 路径穿越输入

- **WHEN** `download("foo/../../../etc/passwd")`（或等价非法串）
- **THEN** 不返回受保护文件内容；行为符合实现选定的「抛异常或视为不存在」且与单测一致

### Requirement: 本地存储模式

当 `qc.file.type=local` 时，系统 SHALL 以 `qc.file.local.path` 为根目录存储对象；目录不存在时 MUST 自动创建。`qc.file.local.path` 未配置或为空时，系统 SHALL 使用系统临时目录或文档明确的回退路径。

#### Scenario: 根目录自动创建

- **WHEN** 根路径配置指向尚不存在的目录且上传成功
- **THEN** 持久化后该目录与对象文件存在

### Requirement: MinIO 存储模式与 presigned

当 `qc.file.type=minio` 时，系统 SHALL 使用配置的 `endpoint`、`accessKey`、`secretKey`、`bucket`（及 `pathStyleAccess` 等兼容项）访问 MinIO。系统 SHALL 提供 `getPresignedUrl(String relativePath, long expireSeconds)`：在 MinIO 模式下返回可在有效期内访问的签名 URL；在 `local` 模式下 SHALL 回落为与 `view(relativePath)` 相同字符串语义。系统 SHALL 提供 `download(relativePath)`：**从 MinIO 以流式方式** 构造 `Resource`，MUST NOT 在常规路径将整对象读入内存字节数组。

#### Scenario: presigned 非空

- **WHEN** MinIO 已正确配置且对象已存在
- **THEN** `getPresignedUrl` 返回 http(s) 签名 URL 字符串

#### Scenario: MinIO 下载可读

- **WHEN** 对象存在且调用 `download`
- **THEN** 返回的 `Resource` 可打开 `InputStream` 并按存储内容顺序读取（流式）

### Requirement: view 与 getShortUrl

系统 SHALL 提供 `view(String relativePath)`：当值已为完整 URL（实现约定前缀检测）时直接返回；否则若存在可用 domain（注解或 `qc.file.domain`），则返回拼接后的完整 URL；若无 domain，则返回相对路径。系统 SHALL 提供 `getShortUrl(String relativePath)`：**在本版本中** 其行为 MUST 与 `view(relativePath)` 完全一致（短链存储不接）。

#### Scenario: 无 domain 时 view

- **WHEN** 未配置 `qc.file.domain` 且 `view` 入参为相对路径
- **THEN** 返回结果等于该相对路径字符串

#### Scenario: getShortUrl 等价 view

- **WHEN** 对同一 `relativePath` 调用 `getShortUrl` 与 `view`
- **THEN** 两者返回值相同

### Requirement: delete 与 exists

系统 SHALL 提供 `delete(relativePath)` 与 `exists(relativePath)`，语义分别对应移除对象与探测对象是否存在；对非法相对路径的处理 MUST 与「相对路径安全」要求一致。

#### Scenario: 删除后不存在

- **WHEN** 上传成功得到 `p` 后执行 `delete(p)` 再 `exists(p)`
- **THEN** `exists(p)` 为 false

### Requirement: FileUploadHook 与顺序

系统 SHALL 定义 `FileUploadHook` 接口，包含 `beforeUpload`、`afterUpload`、`onError`（方法签名与上下文对象以实现为准，但语义必须覆盖：上传前、成功后、失败回调）。系统 SHALL 支持多个 Bean 实现，并使用 Spring `@Order`（或等价排序）依次调用。`beforeUpload` 若拒绝上传，SHALL **抛出异常**（不设并行返回码路径）。`onError` SHALL 在上传失败且钩子链已执行到的范围内被调用。

#### Scenario: beforeUpload 否决

- **WHEN** 某钩子 `beforeUpload` 决定拒绝
- **THEN** 抛出异常且不产生存储对象

### Requirement: 注解 FileUrl 与 Jackson

系统 SHALL 提供字段注解 `@FileUrl`（可含可选 `domain` 覆盖）。JSON **序列化**：若字段值为 `null`，SHALL 输出 **JSON null**（属性键保留），MUST NOT 因工程全局 `non_null` 省略该字段；若为非空相对路径且非已完整 URL，SHALL 拼接 **有效 domain**（注解优先，否则 `qc.file.domain`）与路径。JSON **反序列化**：若入参为完整 URL 且以配置 domain 为前缀，SHALL 剥离为相对路径；否则按字符串原文或相对路径规则处理（与单测一致）。domain SHALL 在模块装配时注入到注解处理可读取的上下文（如属性 holder）。

#### Scenario: null 保留

- **WHEN** 某 DTO 字段标注 `@FileUrl` 且值为 `null` 并序列化
- **THEN** JSON 中含该字段且值为 null

### Requirement: 模块边界与启用开关

本模块 SHALL NOT 提供对外 `Controller`；仅提供可注入的 `FileTemplate` 与配置 `qc.file`。系统 SHALL 支持 `qc.file.enabled`（默认 `true`）：为 `false` 时 MUST NOT 注册强依赖存储的 Bean 或 MUST 提供安全空实现（具体策略在 tasks 实现，但不得在无配置时静默写出到不可预期路径）。

#### Scenario: 业务可注入 FileTemplate

- **WHEN** `qc.file.enabled=true` 且应用上下文启动成功
- **THEN** 业务组件可注入 `FileTemplate` 并调用 `upload`

### Requirement: 配置项 qc.file

系统 SHALL 支持 `qc.file` 下配置：`enabled`、`type`（`local|minio`）、`domain`、`defaultClassify`、`classifies[]`（`classify`、`limitExt`、`limitSize`）、`local.path`、MinIO 连接与 `pathStyleAccess`、以及预留的 `shortUrl.*`。系统 SHALL 在 `type=minio` 时要求 MinIO 必要配置齐备，否则启动失败或按 Spring Boot 绑定错误暴露（与 tasks 一致）。

#### Scenario: 可从环境绑定属性

- **WHEN** `application.yml` 提供 `qc.file.type` 与 `qc.file.local.path`
- **THEN** 本地实现使用该根路径
