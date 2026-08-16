package io.github.genkidoudou.common.monitor.slowsql;

import lombok.Builder;
import lombok.Value;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 单次慢 SQL 采集快照（JDBC 执行线程构造，供异步落库）。
 */
@Value
@Builder
public class SlowSqlCapturePayload implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** {@link SlowSqlSource} 取值。 */
    String sqlSource;

    /** {@link SlowSqlType} 取值。 */
    String sqlType;

    /** MyBatis {@code MappedStatement#getId()}；非 MyBatis 路径为空串。 */
    String mapperId;

    /** 执行的 SQL 文本（可能已截断）。 */
    String sqlText;

    /** 执行耗时（毫秒）。 */
    long costTimeMs;

    /** 链路追踪 ID；与 MDC 同源，可空。 */
    String traceId;

    /** 客户端操作 ID；可空。 */
    String clientOperationId;

    /** OAuth 客户端 ID；可空。 */
    String clientId;

    /** 请求线程快照：HTTP 方法。 */
    String requestMethod;

    /** 请求线程快照：URI。 */
    String requestUri;

    /** 操作者标识（登录 ID 字符串）；未登录为空串。 */
    String operName;

    /** 采集时间。 */
    LocalDateTime createTime;
}
