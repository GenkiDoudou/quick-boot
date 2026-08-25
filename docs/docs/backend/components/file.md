# file

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.file`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/file/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| DefaultFileTemplate | file/DefaultFileTemplate.java | /** * &#123;@link FileTemplate} 默认实现：分类校验、路径规则、钩子与存储委派。 * &lt;p&gt; * 分类 &#123;@code compressEnabled=1} 时，对 jpg/png/bmp 等图片按分类压缩参数（回退 qc |
| DisabledFileTemplate | file/DisabledFileTemplate.java | /** * &#123;@code qc.file.enabled=false} 时注入，防止误用存储能力。 */ |
| EmptyFileClassifyRuleResolver | file/EmptyFileClassifyRuleResolver.java | /** * 无 system 实现时的占位：不返回任何分类（上传将因分类不存在失败）。 */ |
| FileAccessService | file/FileAccessService.java | /** * 通用文件上传/预览门面：分类规则、绝对路径拼装；&#123;@code anonymous} 仅控制上传是否可匿名。 */ |
| FileClassifyRule | file/FileClassifyRule.java | /** * 运行时分类规则（由 &#123;@link FileClassifyRuleResolver} 从 DB 等来源解析）。 */ |
| FileClassifyRuleResolver | file/FileClassifyRuleResolver.java | /** * 分类规则解析 SPI：由 system 模块基于 &#123;@code sys_file_classify} 提供实现。 */ |
| FileClassifyVo | file/FileClassifyVo.java | /** * 上传分类配置（对外展示）。压缩参数优先取分类表字段，空则回退 &#123;@code qc.file.compress}。 */ |
| FilePathSupport | file/FilePathSupport.java | /** * 相对路径生成、扩展名小写归一、分类白名单与安全校验。 */ |
| FileStorageAutoConfiguration | file/FileStorageAutoConfiguration.java | /** * 文件存储自动配置：本地 &#123;@link FileTemplate}、&#123;@link FileAccessService}、Jackson &#123;@code @FileUrl}。 */ |
| FileStorageException | file/FileStorageException.java | /** * 文件存储模块异常（路径不安全、读写失败等）。 */ |
| FileStorageOperations | file/FileStorageOperations.java | /** * 实际读写存储介质（本期仅本地磁盘），由 &#123;@link DefaultFileTemplate} 编排路径校验与钩子。 */ |
| FileStorageType | file/FileStorageType.java | /** * &#123;@code qc.file.type} 取值。本期仅支持 &#123;@link #local}；&#123;@link #minio} 选中时启动失败。 */ |
| FileTemplate | file/FileTemplate.java | /** * 统一文件门面：上传、下载、访问 URL、预签名、删除等；不暴露 Web Controller。 */ |
| FileUploadAfterContext | file/FileUploadAfterContext.java | /** * &#123;@link FileUploadHook#afterUpload} 入参。 */ |
| FileUploadBeforeContext | file/FileUploadBeforeContext.java | /** * &#123;@link FileUploadHook#beforeUpload} 入参。 */ |
| FileUploadErrorContext | file/FileUploadErrorContext.java | /** * &#123;@link FileUploadHook#onError} 入参。 */ |
| FileUploadHook | file/FileUploadHook.java | /** * 上传生命周期钩子，多 Bean 时使用 Spring &#123;@link org.springframework.core.annotation.Order} 排序。 * &lt;p&gt; * 保留接口以供扩展；本期不注册全局 &#123;@code s |
| FileUploadResult | file/FileUploadResult.java | /** * 文件上传结果：相对路径入库，绝对路径供前端直接访问。 */ |
| ImageCompressSupport | file/ImageCompressSupport.java | /** * 分类开启压缩时，对常见位图做等比缩小与重编码；非图片或失败时返回 empty（调用方保留原字节）。 */ |
| LocalFileStorageBackend | file/LocalFileStorageBackend.java | /** * 本地磁盘存储。 */ |
| QcFileProperties | file/QcFileProperties.java | /** * &#123;@code qc.file.*} 配置绑定：本地存储基础项、域名与 &#123;@link io.github.genkidoudou.common.file.url.FileUrl}。 * &lt;p&gt; * 分类规则不以本配置列表为权威来源 |
| FileUrl | file/url/FileUrl.java | /** * 标记仅存相对路径的字段：JSON 写出时拼接访问域名；入参若为带 domain 前缀的完整 URL 则剥离为相对路径。 * &lt;p&gt; * 与全局 &#123;@code default-property-inclusion: non_nul |
| FileUrlAnnotationIntrospector | file/url/FileUrlAnnotationIntrospector.java | /** * 识别 &#123;@link FileUrl}，注册专用序列化/反序列化并强制保留 null。 */ |
| FileUrlDeserializer | file/url/FileUrlDeserializer.java | /** * &#123;@link FileUrl} 字段反序列化：剥掉配置/注解 domain 前缀，存相对路径。 */ |
| FileUrlSerializer | file/url/FileUrlSerializer.java | /** * &#123;@link FileUrl} 字段序列化：&#123;@code null} 写 JSON null；已是 http(s) 则原样；否则拼接 domain。 */ |
| FileUrlSupport | file/url/FileUrlSupport.java | /** * &#123;@link io.github.genkidoudou.common.file.FileTemplate#view} 与 &#123;@link FileUrl} 共用的 domain 拼接/剥离。 */ |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
