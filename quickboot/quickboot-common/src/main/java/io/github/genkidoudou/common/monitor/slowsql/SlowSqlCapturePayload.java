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

    String mapperId;

    String sqlText;

    long costTimeMs;

    String traceId;

    String clientOperationId;

    String clientId;

    String requestMethod;

    String requestUri;

    String operName;

    LocalDateTime createTime;
}
