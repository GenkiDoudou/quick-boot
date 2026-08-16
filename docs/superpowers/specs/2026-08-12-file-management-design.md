# 文件上传迁移与文件管理设计说明

**日期**：2026-08-12  
**状态**：已定稿（brainstorming 确认：方案 1；澄清 1B / 2D / 3A / 4A / 5B / 6A）  
**来源**：从 `bak` 迁移 C7 上传/预览能力；在 `quick-ui` 增加文件分类管理与文件管理；后端补齐本地存储与管理 API。

---

## 1. 背景与目标

### 1.1 背景

- `bak` 已具备：`FileTemplate`（本地/MinIO）、YAML `qc.file.classifies`、`sys_file` 登记、通用 `/file/**`、管理端 `/system/file/**`、`C7Upload` / `C7Preview`、文件管理页。
- 当前仓库：`quick-ui` 已有部分 `C7Upload` / `C7Preview` 与 API 封装，但 **后端无文件模块**，无文件管理页，分类亦非可配置 CRUD。
- 需求：迁移上传/图片预览相关组件；后端增加 **文件分类管理**（后缀、大小、压缩开关等）与 **文件管理**（预览、下载等）。

### 1.2 目标（本期）

1. 迁入 **本地** 文件存储（`FileTemplate` 等），分类规则改为 **数据库 CRUD**。
2. 管理端：**文件分类管理** + **文件管理** 两页。
3. 校准/补齐 `C7Upload`、`C7Preview`（不新增独立图片上传组件；图片 = 图片类 classify + Upload + Preview）。
4. **仅**「文件管理」页上传写入 `sys_file`；业务通用上传不进文件管理列表。

### 1.3 非目标

- MinIO、分片断点续传、Office 转 PDF、回收站恢复、业务关联追踪。
- WebP/GIF 动画压缩与前端预压缩。
- 业务上传自动进入文件管理列表。

### 1.4 已确认选型

| # | 议题 | 选择 |
|---|------|------|
| 1 | 分类配置 | DB CRUD（`sys_file_classify`）；YAML 仅保留存储基础配置 |
| 2 | 压缩 | 字段落库，本期不实现压缩 |
| 3 | 存储 | 仅本地磁盘 |
| 4 | 前端组件 | 对齐 bak：`C7Upload` + `C7Preview` + 管理页 |
| 5 | 登记范围 | 仅文件管理页上传登记 |
| 6 | 交付 | 一次做完：存储 + 分类 + 文件管理 + 组件/页面 |

**总体方案**：方案 1 — 迁 `FileTemplate` + 分类改读库 + 管理端显式登记（无全局 `FileUploadHook`）。

---

## 2. 架构与职责

| 层 | 位置 | 职责 |
|----|------|------|
| 存储 | `quickboot-common`（迁自 bak，本期仅 local） | `FileTemplate` 上传/读流/删对象；按分类校验后缀与大小 |
| 分类配置 | `quickboot-module-system` | `sys_file_classify` CRUD；供上传校验与 `/file/classifies` 读取（短缓存，变更失效） |
| 文件登记 | `quickboot-module-system` | `sys_file`；**仅** `SysFileService.upload` 成功后显式插入 |
| 通用 API | `/file/**` | 分类查询、业务上传、按路径预览（不登记） |
| 管理 API | `/system/file/**`、`/system/fileClassify/**` | 文件管理 + 分类管理 |
| 前端 | `quick-ui` | `C7Upload` / `C7Preview`；分类管理页；文件管理页 |

### 2.1 数据流

```text
业务表单 C7Upload
  → POST /file/upload/{classify}
  → FileTemplate（读 DB 分类规则校验）→ 本地落盘
  → 返回 relativePath/url   【不写 sys_file】

文件管理页上传
  → POST /system/file/upload/{classify}
  → FileTemplate 落盘
  → 显式写 sys_file（上传人/时间等）
  → 列表可预览/下载/删除（软删 + 删本地对象）
```

### 2.2 相对 bak 的差异

| 项 | bak | 本期 |
|----|-----|------|
| 分类 | `qc.file.classifies` YAML | `sys_file_classify` DB CRUD |
| 登记 | 全局 `FileUploadHook` | 仅管理端上传显式登记 |
| 存储 | local + MinIO | 仅 local |
| 压缩 | 无 | `compress_enabled` 字段，不执行压缩 |
| `sys_file` 软删 | `deleted` 整型等 | 对齐 `BaseEntity.del_flag`（`CHAR(1)`） |

---

## 3. 数据模型

布尔/是否类字段统一 `CHAR(1)`：`0`=否，`1`=是。审计与软删对齐 `BaseEntity`（`create_by` / `create_time` / `update_by` / `update_time` / `del_flag` / `remark`）。

### 3.1 `sys_file_classify`

| 字段 | 类型 | 说明 |
|------|------|------|
| `classify_id` | BIGINT PK | 雪花 `ASSIGN_ID` |
| `classify` | VARCHAR(64) 唯一 | 分类键；禁止含 `/`；**创建后不可改** |
| `classify_name` | VARCHAR(128) | 展示名 |
| `limit_ext` | VARCHAR(512) | 允许后缀，逗号分隔；空=内置默认白名单 |
| `limit_size_bytes` | BIGINT | 单文件上限；默认 `10485760`（10MB） |
| `limit_count` | INT | 单次最多文件数；默认 `1` |
| `compress_enabled` | CHAR(1) | `0`/`1`；本期不执行压缩 |
| `anonymous` | CHAR(1) | `0`/`1`；是否允许未登录走通用上传 |
| `status` | CHAR(1) | `0` 正常 / `1` 停用（停用不可再上传） |
| 审计 + `del_flag` | | `BaseEntity` |

约束：未删记录上 `classify` 唯一。上传前必须存在且 `status='0'`。

种子数据：至少一条 `default`（展示名「默认」，10MB，`limit_count=1`，压缩关，匿名关，正常）。

### 3.2 `sys_file`

| 字段 | 类型 | 说明 |
|------|------|------|
| `file_id` | BIGINT PK | 雪花 |
| `original_name` | VARCHAR | 原始文件名 |
| `ext` | VARCHAR(32) | 小写无点；解析不到为 `''` |
| `size_bytes` | BIGINT | 大小 |
| `content_type` | VARCHAR | 可空 |
| `classify` | VARCHAR(64) | 上传时分类键（冗余，不强制 FK） |
| `relative_path` | VARCHAR 唯一 | 存储相对路径 |
| `uploader_user_id` | BIGINT | 无登录为 `0` |
| `uploader_user_name` | VARCHAR | 展示用 |
| `upload_time` | DATETIME | 上传时间 |
| 审计 + `del_flag` | | 软删；删除时同步删本地对象 |

不入库可访问 URL；预览/下载现算。

### 3.3 应用配置（非表）

`qc.file.*` 保留：`enabled`、`type=local`、`local.path`、`domain` / `viewUrlBase` 等。  
**不再**用 YAML 维护 `classifies` 列表。

---

## 4. 后端 API 与权限

### 4.1 通用文件（`/file`）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/classifies` | 登录即可 | 启用中的分类列表（供 `C7Upload`） |
| GET | `/classifies/{classify}` | 登录即可 | 单分类规则 |
| POST | `/upload/{classify}` | 登录；分类 `anonymous=1` 时可匿名 | multipart；**不写** `sys_file` |
| GET | `/preview/{*relativePath}` | 按安全配置（可匿名例外） | inline 预览流 |

上传：分类存在且未停用 → 校验后缀/大小；忽略压缩执行。

### 4.2 文件分类管理（`/system/fileClassify`）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/list` | `system:fileClassify:list` | 分页 |
| GET | `/{id}` | `system:fileClassify:query` | 详情 |
| POST | `/`（或与现网一致的无 path 新增） | `system:fileClassify:add` | `classify` 唯一 |
| POST | `/update` | `system:fileClassify:edit` | 不可改 `classify` 键 |
| POST | `/remove` | `system:fileClassify:remove` | 批量软删；**若仍有未删 `sys_file` 引用该 classify 则拒绝** |

分类变更后短缓存失效。

### 4.3 文件管理（`/system/file`）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/list` | `system:file:list` | 分页；文件名/上传人/分类/时间 |
| POST | `/upload/{classify}` | `system:file:upload` | 落盘后显式写 `sys_file` |
| GET | `/view/{*relativePath}` | `system:file:view` | 管理端预览流（对齐现有 `buildFileViewUrl`） |
| GET | `/download/{fileId}` | `system:file:download` | 附件下载，文件名用 `original_name` |
| POST | `/remove` | `system:file:remove` | 批量：软删 + 删本地对象；对象不存在视为成功 |

### 4.4 菜单

系统管理下：

1. **文件分类** — 分类 CRUD  
2. **文件管理** — 列表 / 上传 / 预览 / 下载 / 删除  

### 4.5 错误语义（固定）

- 分类不存在/停用 → 业务警告，不可上传。  
- 超大小/后缀不符 → 业务警告。  
- 管理上传登记失败 → 上传失败并尽量回滚已写对象。  
- 删除分类时仍有文件引用 → 拒绝并提示先处理文件。  
- `compress_enabled=1` → 本期行为与 `0` 相同（仅配置可见）。

---

## 5. 前端页面与组件

### 5.1 组件

| 组件 | 行为 |
|------|------|
| `C7Upload` | 必填 `classify`；拉分类规则做提示与前端校验；默认 `/file/upload/{classify}`；支持 `uploadFn` 覆盖（管理页走 `/system/file/upload/...`） |
| `C7Preview` | 按 URL 推断图片/视频/其它；与现有实现一致，联调缺口再修 |

不新增独立「图片上传」组件。

### 5.2 文件分类管理

- 路径：`quick-ui/src/views/system/fileClassify/index.vue`
- `C7JsonTable` + `C7Dialog`；列含分类键、展示名、后缀、大小、数量、压缩、匿名、状态
- 新增可填 `classify`；编辑禁用 `classify`
- `compress_enabled` 开关文案注明「仅配置，暂不压缩」
- 大小：UI 可用 MB 输入，提交前换算为 `limit_size_bytes`（实现统一一种）

### 5.3 文件管理

- 路径：`quick-ui/src/views/system/file/index.vue`（对齐 bak）
- 列：文件名、分类、大小、扩展名、上传人、上传时间
- 搜索：文件名 / 上传人 / 分类
- 上传弹窗：选分类 → `C7Upload`（`auto-upload=false` + system `uploadFn`）→ 确认提交并刷新
- 预览：图/视频弹窗，其它新窗口；下载；删除
- API：`api/common/file.js`、`api/system/file.js`；新增 `api/system/fileClassify.js`

### 5.4 权限展示

按钮 `v-hasPermi` 与第 4 节权限码一致。

---

## 6. 测试与验收

### 6.1 测试建议

- 后端：分类 CRUD；上传校验（后缀/大小/停用）；管理上传写 `sys_file`；通用上传不写 `sys_file`；下载文件名；删除软删+删盘；有引用时拒删分类。
- 前端：无分类/规则未就绪时上传禁用；文件管理预览三类行为；分类编辑不可改键。

### 6.2 验收清单

1. 可配置分类（后缀、大小、数量、压缩开关、匿名、启停）。  
2. 业务 `C7Upload` 可上传并可预览，**不出现**在文件管理列表。  
3. 文件管理页上传后列表可见，可预览/下载/删除。  
4. 删除文件后列表不可见且本地对象清除。  
5. 仅本地存储；无 MinIO；无真实压缩逻辑。

---

## 7. 后续流程

本设计确认无修改后，进入实现计划（`writing-plans`），再编码与联调。
