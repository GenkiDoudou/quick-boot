# tracing

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.tracing`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/tracing/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| ClientOperationFilter | tracing/ClientOperationFilter.java | /** * 读取 &#123;@link ClientOperationIds#HEADER_NAME}、&#123;@link ClientIds#HEADER_NAME} 写入对应 MDC，请求结束清除。 * &lt;p&gt; * 与 Micrometer &#123;@li |
| ClientOperationTracingAutoConfiguration | tracing/ClientOperationTracingAutoConfiguration.java | /** * 注册 &#123;@link ClientOperationFilter}，在 CORS 之后、业务 Filter 之前写入 clientOperationId MDC。 */ |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
