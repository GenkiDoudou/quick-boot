## Context

原始需求见 `原始需求/后端/文件上传与存储模块.md`。工程为 Spring Boot 3，Web 模块已配置 `spring.servlet.multipart`（全局上限可能高于单分类限制，需在业务/模板内二次校验）。`quickboot-common` 已声明 **optional** 的 `io.minio:minio`。统一 API 响应与全局异常处理已在其他模块约定；本模块校验失败或钩子否决应 **抛异常** 进入既有异常链路。

## Goals / Non-Goals

**Goals:**

- 提供 `FileTemplate`，支持 `local` / `minio`，方法包含：`upload(MultipartFile|bytes)`、`download`、`view`、`getPresignedUrl`、`getShortUrl`、`delete`、`exists`（语义以 spec 为准）。
- 分类 `classify`、默认分类、`limitExt` / `limitSize` 与路径模板 `{classify}/{yyyy/MM}/{uuid}.{ext}`；扩展名参与白名单时 **归一为小写**。
- `FileUploadHook` 支持排序；上传异常触发 `onError`。
- `@FileUrl`：出参拼接 domain、入参剥离 domain；**null 保留在 JSON 中**（与全局 `non_null` 共存时需字段级 ALWAYS）。
- MinIO：`getPresignedUrl` 可用；`download` **流式**。
- **第一期** `getShortUrl` **实现为委托** `view`。

**Non-Goals:**

- 不在 `quickboot-common` 或本变更内提供 **对外** `Controller`（下载 URL 由业务暴露，内部调用 `FileTemplate.download`）。
- 不实现真实短链存储（Redis/DB）；配置可预留。
- 不包办病毒扫描、图片压缩实现，仅提供钩子扩展点。

## Decisions

1. **下载由后端接口提供**  
   业务层编写下载入口，调用 `FileTemplate.download(relativePath)` 返回 `Resource` 并写回响应；**不依赖** 仅静态映射/CDN 直链作为唯一手段。

2. **MinIO `download` 流式**  
   使用 **流式** `Resource`（例如基于 `InputStream`），**不得**为常态路径将整对象读入字节数组。

3. **钩子否决与错误**  
   `beforeUpload` 等若拒绝上传，**抛出异常**；由项目 **全局异常处理** 转换为统一响应。**不在** `FileTemplate` 内吞异常或以特殊返回值表示否决。

4. **`@FileUrl` 与 null**  
   字段值为 `null` 时，序列化 **保留属性且值为 null**（需 `@JsonInclude(JsonInclude.Include.ALWAYS)` 或等效）；非空相对路径再拼接 domain。

5. **后缀校验**  
   从文件名解析扩展名后 **转为小写** 再与 `limitExt` 白名单比对。

6. **短链**  
   **当前版本** `getShortUrl(relativePath)` **直接等价于** `view(relativePath)`；配置项 `shortUrl.*` 可预留，不强制接存储。

7. **`view` 与 domain**  
   未配置 `qc.file.domain`（且注解无 domain）时，`view` **返回相对路径字符串**（与原始需求一致）。

## Risks / Trade-offs

- **[Risk]** Spring multipart 全局上限大于分类上限时，大文件已占用 IO 后才被拒绝。  
  → **Mitigation**：文档说明；可选在网关或前置 Filter 限制（超出本模块）。

- **[Risk]** 响应已开始流式写出后出错，全局异常处理器可能无法改写已提交的 body。  
  → **Mitigation**：业务 Controller 使用流式时注意异常边界；必要时在 design/tasks 中记录最佳实践。

- **[Risk]** `non_null` 与 ALWAYS 并存时若遗漏注解，会破坏「保留 null」验收。  
  → **Mitigation**：单测覆盖 `@FileUrl` 序列化；文档突出字段级注解。

- **[Trade-off]** `presigned` 与「后端下载」并存：预览可走 presigned 或业务下载接口，由业务选择。

## Migration Plan

1. 合并后引入自动配置与 `FileTemplate` Bean；未配置 `qc.file.enabled=false` 时按默认 local 行为可用。  
2. 业务逐步注入 `FileTemplate`；数据库继续仅存相对路径。  
3. 回滚：移除自动配置与实现类；无调用方则无运行时依赖。

## Open Questions

- 通用后缀白名单具体列表（图片/办公文档）是否在 `application.yml` 默认值中维护并由 spec 引用，或硬编码于代码常量 — **实现阶段在 tasks 中落定**。
