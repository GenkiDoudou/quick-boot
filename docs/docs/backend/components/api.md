# api

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.api`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/api/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| ClientIds | api/ClientIds.java | /** * 从 SLF4J &#123;@link MDC} 读取 OAuth / Client HMAC 的 &#123;@code clientId}（与 &#123;@link ClientOperationIds} 分离）。 * &lt;p&gt; * 值由 &#123;@link  |
| ClientOperationIds | api/ClientOperationIds.java | /** * 从 SLF4J &#123;@link MDC} 读取前端一次用户操作的 &#123;@code operationId}（与 &#123;@link TraceIds} 分离）。 * &lt;p&gt; * 值由 &#123;@link io.github.genkidoudo |
| HttpCodes | api/HttpCodes.java | /** * 与 HTTP 语义对齐的&lt;strong&gt;业务响应码&lt;/strong&gt;常量，用于 &#123;@link R} 的 &#123;@code code} 字段。 * &lt;p&gt; * 约定：对外 API 的 HTTP 状态码保持 200，由客户端依据本处整型 |
| PageInfo | api/PageInfo.java | /** * Controller 层分页出参契约，由持久层分页结果回填。 * &lt;p&gt; * &#123;@link #pages} 使用 &#123;@code (total + size - 1) / size} 计算（要求 &#123;@code size &gt;= 1} |
| PageRequest | api/PageRequest.java | /** * Controller 层分页入参契约；Service 内可再转为 MyBatis-Plus &#123;@link com.baomidou.mybatisplus.extension.plugins.pagination.Page}。  |
| R | api/R.java | /** * 统一 JSON 响应体：&#123;@code code} / &#123;@code msg} / &#123;@code data} / &#123;@code traceId} / &#123;@code timestamp}。 * &lt;p&gt; * 成功码固定为 &#123;@link |
| TraceIds | api/TraceIds.java | /** * 从 SLF4J &#123;@link MDC} 读取链路 &#123;@code traceId}，与 &#123;@code logback} 中 &#123;@code %X{traceId&#125;&#125; 约定一致。 * &lt;p&gt; * &#123;@code traceId} 依赖  |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
