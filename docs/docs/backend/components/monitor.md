# monitor

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.monitor`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/monitor/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| IgnoreLogger | monitor/operlog/IgnoreLogger.java | /** * 控制操作日志切面是否记录或记录粒度。 */ |
| OperLogBusinessType | monitor/operlog/OperLogBusinessType.java | /** * 操作日志业务类型，与字典 &#123;@code sys_oper_business_type} 数值一致。 */ |
| OperLogCaptureAutoConfiguration | monitor/operlog/OperLogCaptureAutoConfiguration.java | /** * 注册操作日志采集切面。 */ |
| OperLogCapturedEvent | monitor/operlog/OperLogCapturedEvent.java | /** * 操作日志已采集、待持久化的事件（载荷为 &#123;@link OperLogCapturePayload}）。 */ |
| OperLogCapturePayload | monitor/operlog/OperLogCapturePayload.java | /** * 操作日志采集事件载荷；在切面线程组装，含 &#123;@link #traceId} 以便与异步化演进解耦。 */ |
| OperLogConsolePrintListener | monitor/operlog/OperLogConsolePrintListener.java | /** * 参照旧栈 &#123;@code WebPrintLoggerEventListener}：监听 &#123;@link OperLogCapturedEvent}，将请求摘要打印到控制台。 */ |
| OperLogMeta | monitor/operlog/OperLogMeta.java | /** * 显式指定写入操作日志的模块标题与业务类型（宽切面无若依 &#123;@code @Log} 时的优先元数据）。 */ |
| OperLogProperties | monitor/operlog/OperLogProperties.java | /** * 操作日志采集与导出相关配置。 */ |
| OperLogPublishingAspect | monitor/operlog/OperLogPublishingAspect.java | /** * 宽切面：环绕 &#123;@link org.springframework.web.bind.annotation.RestController} 的 public 方法， * 在 &#123;@code finally} 中发布 &#123;@link  |
| OperLogSensitiveMasker | monitor/operlog/OperLogSensitiveMasker.java | /** * 操作日志入库前字符串脱敏：对常见敏感键的 JSON 片段做掩码。 */ |
| SlowSqlCaptureAutoConfiguration | monitor/slowsql/SlowSqlCaptureAutoConfiguration.java | /** * 慢 SQL 采集：事件发布支持类与 MyBatis mapper_id 标记拦截器（JDBC 落库在 web 模块 Druid Filter）。 */ |
| SlowSqlCapturedEvent | monitor/slowsql/SlowSqlCapturedEvent.java | /** * 慢 SQL 已采集、待持久化的事件。 */ |
| SlowSqlCapturePayload | monitor/slowsql/SlowSqlCapturePayload.java | /** * 单次慢 SQL 采集快照（JDBC 执行线程构造，供异步落库）。 */ |
| SlowSqlCaptureSupport | monitor/slowsql/SlowSqlCaptureSupport.java | /** * JDBC 层慢 SQL 判定、来源解析与事件发布。 */ |
| SlowSqlMapperContext | monitor/slowsql/SlowSqlMapperContext.java | /** * 当前线程 MyBatis &#123;@code MappedStatement#getId()}，供 Druid JDBC 采集器读取后清除。 */ |
| SlowSqlMapperIdInnerInterceptor | monitor/slowsql/SlowSqlMapperIdInnerInterceptor.java | /** * 仅在当前线程标记 MyBatis &#123;@code mapperId}，由 Druid JDBC 过滤器统一落库。 */ |
| SlowSqlProperties | monitor/slowsql/SlowSqlProperties.java | /** * 慢 SQL 采集配置（JDBC / Druid 层统一落库；MyBatis 仅补充 mapper_id）。 */ |
| SlowSqlSource | monitor/slowsql/SlowSqlSource.java | /** * 慢 SQL 来源分类（落库 &#123;@code sql_source} 字段取值）。 */ |
| SlowSqlType | monitor/slowsql/SlowSqlType.java | /** * 慢 SQL 语句操作类型（落库 &#123;@code sql_type} 列取值）。 */ |
| SlowSqlTypeResolver | monitor/slowsql/SlowSqlTypeResolver.java | /** * 从 SQL 文本解析操作类型（SELECT / INSERT / UPDATE / DELETE 等）。 */ |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
