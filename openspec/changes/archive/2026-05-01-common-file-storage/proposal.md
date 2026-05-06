## Why

业务需要统一的文件上传、下载与 URL 出参处理，且要在 **本地磁盘** 与 **MinIO** 之间切换而不污染业务代码；数据库仅存相对路径，JSON 需自动补全/剥离域名。仓库已在 `quickboot-common` 中引入可选 **MinIO** 依赖，但尚未落地 `FileTemplate` 与 `@FileUrl`，需以 OpenSpec 固化范围与验收，避免实现阶段语义漂移。

## What Changes

- 在 `quickboot-common` 实现 **`FileTemplate`**（本地 + MinIO）、**分类校验**（后缀白名单 **小写** 比较、大小上限）、**相对路径生成** `{classify}/{yyyy/MM}/{uuid}.{ext}`、**路径安全校验**（禁止 `..`、禁止以 `/` 开头的存储路径）。
- 实现 **`FileUploadHook`**（`beforeUpload` / `afterUpload` / `onError`），`beforeUpload` 否决时 **抛异常** 交由全局异常处理。
- 实现 **`@FileUrl`** 与 Jackson 集成：序列化补全 domain、反序列化剥离 domain；**`null` 保留**（字段键存在且值为 null，不因全局 `non_null` 被省略）。
- **第一期** `getShortUrl` **等同于** `view`；短链存储预留配置不强制实现。
- **下载**：`download` 返回 **可流式读取** 的 `Resource`（MinIO 流式，避免整文件加载）；**对外下载 HTTP 接口由业务 Controller 调用** `FileTemplate`，本模块不新增公共 Controller。

## Capabilities

### New Capabilities

- `common-file-storage`：`FileTemplate`、存储抽象、分类与路径规则、上传钩子、`@FileUrl`、与 `qc.file` 配置及自动装配相关的契约与验收。

### Modified Capabilities

- （无）

## Impact

- 代码：`quickboot-common` 新增包（如 `...common.file` 或与项目命名一致的子包）、Spring Boot 自动配置与 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（若与现有 common 模块模式一致）。
- 配置：新增 `qc.file.*`（见原始需求）；可选在 `application.yml` 增加示例（以 tasks 为准）。
- 依赖：沿用已有 `io.minio:minio`（optional）；本地模式仅需 JDK/Spring。
- 业务：`quickboot-web` 需在需要时编写下载等接口并注入 `FileTemplate`（本变更可在 tasks 中列为示例或非必须）。
