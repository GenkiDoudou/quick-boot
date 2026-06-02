# 文件管理（全系统上传文件登记、上传/预览/下载/删除）设计说明

**日期**：2026-06-02  
**状态**：已定稿（经 brainstorming 澄清与确认：方案 1、删除A、预览A）  
**依据**：`原始需求/需求.md`（文件管理：存储所有上传文件 + 上传/下载按钮）

---

## 1. 背景与目标

- **背景**：仓库已具备通用文件存储能力（`quickboot-common` 的 `FileTemplate`），但缺少面向管理端的「文件管理」能力：无法统一看到系统所有上传文件、无法提供统一的预览/下载/删除入口，也缺少审计信息（上传人、时间等）。
- **目标**：新增「文件管理」模块，实现：
  - 全系统上传文件 **自动登记**（不要求各业务模块逐个改造）。
  - 管理端提供 **上传、列表、预览、下载、删除** 能力。
  - 列表字段满足：**文件名、大小、扩展名、上传时间、上传人**（3A）。
- **非目标**：不做业务关联（业务单号/模块维度追踪）、不做复杂文件转换预览（如 Office 转 PDF）、不做分片断点续传与版本管理、不做回收站与延迟清理（可作为后续迭代）。

---

## 2. 总体方案（方案 1）

### 2.1 分层与职责

- **存储层（已有）**：`quickboot-common` 提供 `FileTemplate` 与存储后端（本地/MinIO），上传返回相对路径 `relativePath`，并可按相对路径下载/生成可访问 URL。
- **登记层（新增）**：在 `FileTemplate` 上传成功后通过 `FileUploadHook` 写入文件元数据表 `sys_file`，实现“全系统上传文件自动登记”（1B）。
- **管理 API（新增）**：`quickboot-web/quickboot-system` 提供文件管理 Controller/Service：分页查询、上传入口、下载、预览 URL、删除。
- **管理端页面（新增）**：`quick-ui` 新增「系统管理 / 文件管理」页面，使用 `C7JsonTable` 风格实现列表与操作按钮。

### 2.2 关键原则

- **统一主键**：管理端以 `fileId` 操作，避免前端直接拼接/传递路径字符串导致的安全与耦合问题。
- **URL 不入库**：表里只保存 `relativePath` 等元数据；可访问 URL 由后端按当前配置与域名规则计算返回。
- **删除语义**：采用 **逻辑删 + 同步删除存储对象**（删除A），保证审计与空间回收。

---

## 3. 数据模型（新增 `sys_file`）

### 3.1 字段（最小集，满足 3A + 管理能力）

建议字段如下（最终字段名、审计字段风格以仓库现有表规范对齐）：

- `fileId`：主键
- `originalName`：原始文件名（上传时的 `MultipartFile.getOriginalFilename()`）
- `ext`：扩展名（小写、去点；无法解析时为空字符串或固定占位，需在实现中固定行为）
- `sizeBytes`：文件大小（字节）
- `contentType`：MIME（可空）
- `classify`：分类（`FileTemplate` 的 classify；未传时为默认分类）
- `relativePath`：相对路径（`FileTemplate.upload` 返回值），**唯一**
- `uploaderUserId`：上传人 id（从登录态获取；无登录态时约定为 0 或空，需在实现固定）
- `uploaderUserName`：上传人名称（便于列表展示）
- `uploadTime`：上传时间
- `deleted`：逻辑删除标记（0/1）
- `deleteBy` / `deleteTime`：删除审计（批量删除也记录）

### 3.2 约束与索引建议

- **唯一约束**：`relativePath` 唯一，避免重复登记。
- **查询索引**（建议）：
  - `uploadTime`（倒序分页常用）
  - `uploaderUserId`
  - `originalName`（若支持按名称模糊检索）

---

## 4. 登记与删除流程

### 4.1 全系统上传自动登记（1B）

触发点：`FileTemplate.upload(...)` **成功写入存储并返回 `relativePath`** 后。

实现方式：新增一个 `FileUploadHook`（或等价 hook），在 `after` 阶段执行：

- 输入：`relativePath`、`classify`、原始文件名、size、contentType、当前登录人信息（id/name）
- 行为：写入 `sys_file`（若 `relativePath` 已存在则幂等处理：跳过/更新上传人或时间；实现时固定一种）

说明：
- 该 Hook 仅做“登记”，不影响上传主流程；登记失败的处理策略在实现计划中固定（建议：登记失败视为业务错误，返回上传失败，避免出现“文件存在但不可管理”的孤儿对象）。

### 4.2 删除（删除A：逻辑删 + 同步删对象）

- 管理端触发删除（支持批量）。
- 后端校验权限后：
  - 先对 `sys_file` 记录做逻辑删除并记录删除审计；
  - 再调用存储后端删除 `relativePath` 对应对象；
  - 若对象不存在，按“已删除”视为成功（实现时固定返回语义）。

---

## 5. 后端 API（系统管理）

说明：路径与权限码命名可按现有 `system:*` 习惯微调；接口建议均为 Spring Boot 3 Controller（后端修改/删除倾向使用 `@PostMapping`）。

### 5.1 接口列表

- **分页列表**
  - `GET /system/file/list`
  - 权限：`system:file:list`
  - 查询（建议）：`originalName`（模糊）、`uploaderUserName`（模糊）、`uploadTime` 区间、`deleted`（默认只查未删除）

- **上传**
  - `POST /system/file/upload`
  - 权限：`system:file:upload`
  - 入参：`@RequestPart("file") MultipartFile file`，可选 `classify`（为空时走默认分类）
  - 返回：`fileId` + `relativePath` + `url`（可选）+ 列表展示字段

- **预览（获取可访问 URL）**
  - `GET /system/file/view/{fileId}`
  - 权限：`system:file:view`
  - 返回：`url`（由 `FileTemplate.view(relativePath)` 或等价能力生成）

- **下载（附件）**
  - `GET /system/file/download/{fileId}`
  - 权限：`system:file:download`
  - 返回：文件流；`Content-Disposition` 使用 `originalName`（需处理编码/特殊字符）

- **删除（批量）**
  - `POST /system/file/remove`
  - 权限：`system:file:remove`
  - 入参：`fileIds: number[]`
  - 语义：逻辑删 + 同步删对象

### 5.2 权限与菜单

- 菜单位置：**系统管理** 下新增 **文件管理**
- 权限码：`system:file:list` / `system:file:upload` / `system:file:view` / `system:file:download` / `system:file:remove`
- 后续可扩展：导出、清理已删除、恢复等（本期不做）

---

## 6. 前端页面（quick-ui）

### 6.1 页面落点与风格

- 新增页面：`quick-ui/src/views/system/file/index.vue`
- 使用 `C7JsonTable` 风格（参考 `views/system/config/index.vue`）：
  - 列：文件名、大小、扩展名、上传时间、上传人
  - 操作：预览、下载、删除
  - 工具栏：上传、批量删除

### 6.2 上传交互

- 上传按钮打开弹窗：选择文件 + 可选输入 `classify`（默认不填）
- 上传成功后刷新列表，并可提示成功信息

### 6.3 预览交互（预览A）

规则：**图片/视频弹窗预览 + 其它新窗口打开**。

- 图片：使用 `el-image` 的预览能力（或统一组件能力）在弹窗内展示
- 视频：弹窗内 `video` 播放（关闭时暂停）
- 其它文件：使用 `window.open(url, '_blank', 'noopener,noreferrer')` 打开（由浏览器决定预览/下载）

---

## 7. 验收与测试建议

- **登记覆盖**：任意模块通过 `FileTemplate.upload` 上传成功后，`sys_file` 必须出现一条记录且 `relativePath` 唯一。
- **列表字段**：列表页展示字段满足 3A（文件名/大小/扩展名/上传时间/上传人），分页正常。
- **上传**：文件管理页上传成功后可在列表中立即看到并可预览/下载。
- **预览**：图片/视频走弹窗预览，其它文件新窗口打开；关闭弹窗后视频停止播放。
- **下载**：下载得到的文件名与 `originalName` 一致（特殊字符不乱码）。
- **删除**：删除后列表不可见（默认过滤 deleted），且存储对象被删除；重复删除或对象已不存在时行为明确且前端有可读提示。

---

## 8. 后续流程

- 本设计文档确认无修改后，进入实现计划阶段（`writing-plans`），再进入编码实现与联调验证。

