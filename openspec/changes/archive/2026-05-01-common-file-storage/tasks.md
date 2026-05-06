## 1. 配置与属性绑定



- [x] 1.1 新增 `qc.file` 配置属性类（`@ConfigurationProperties`），覆盖 `enabled`、`type`、`domain`、`defaultClassify`、`classifies`、`local`、`minio`、`shortUrl` 等 proposal/design 所列键；校验 `type=minio` 时必填项。

- [x] 1.2 在 `quickboot-common` 注册自动配置类，并通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 导出；`qc.file.enabled=false` 时不注册存储 Bean 或注册空实现（与 design 一致）。



## 2. FileTemplate 与存储实现



- [x] 2.1 定义 `FileTemplate` 接口，方法覆盖 spec：`upload`（两种重载）、`download`、`view`、`getPresignedUrl`、`getShortUrl`、`delete`、`exists`。

- [x] 2.2 实现路径生成 `{classify}/{yyyy/MM}/{uuid}.{ext}`、扩展名 **小写**、分类校验（默认白名单 + `limitExt`/`limitSize`），非法时 **抛异常**。

- [x] 2.3 实现路径规范化与安全校验（拒绝 `..`、前导 `/` 等），`download`/`delete`/`exists` 与之一致。

- [x] 2.4 实现 `local`：`local.path` 根目录自动创建；`upload` 写入文件；`download` 返回可读 **流式** `Resource`。

- [x] 2.5 实现 `minio`：基于已有 optional 依赖；`upload`/`delete`/`exists`；`download` **流式**；`getPresignedUrl` 签名 URL；`local` 模式 presigned **回落** `view`。

- [x] 2.6 实现 `getShortUrl` **直连委托** `view`；预留 `shortUrl` 配置但不接存储。

- [x] 2.7 注册 `FileUploadHook` 列表（`@Order`），在 `upload` 流程调用；`onError` 在上传失败时触发。



## 3. @FileUrl 与 Jackson



- [x] 3.1 实现 `@FileUrl` 注解及 Jackson 序列化/反序列化逻辑（或 `JsonSerializer`/`ContextualSerializer`）；**`null` 使用 `ALWAYS`**，确保全局 `non_null` 下仍输出 `null`。

- [x] 3.2 提供 domain 上下文（注解覆盖优先，否则 `qc.file.domain`）；反序列化剥离 domain 前缀。



## 4. 测试与文档



- [x] 4.1 为路径生成、分类校验、路径安全、`getShortUrl`≡`view` 编写单元测试（`quickboot-common`，JUnit 5）；MinIO 可用 mock 或 Testcontainers（择一并在注释中说明）。

- [x] 4.2 （可选）在 `application.yml` 或文档中增加 `qc.file` 示例块，不写具体业务 Controller。



## 5. 验证



- [ ] 5.1 在 `quickboot` 根目录使用 **JDK 17+** 执行 `mvn -pl quickboot-common test`，全部通过。


