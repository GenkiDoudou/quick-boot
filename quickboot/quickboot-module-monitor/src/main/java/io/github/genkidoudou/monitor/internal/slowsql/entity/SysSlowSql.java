package io.github.genkidoudou.monitor.internal.slowsql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 慢 SQL 记录，与表 {@code sys_slow_sql} 对应。
 */
@Data
@TableName("sys_slow_sql")
public class SysSlowSql implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "slow_id", type = IdType.ASSIGN_ID)
    private Long slowId;

    /** {@link io.github.genkidoudou.common.monitor.slowsql.SlowSqlSource} 取值。 */
    private String sqlSource;

    /** {@link io.github.genkidoudou.common.monitor.slowsql.SlowSqlType} 取值。 */
    private String sqlType;

    private String mapperId;

    private String sqlText;

    private Long costTime;

    private String traceId;

    private String clientOperationId;

    private String clientId;

    private String requestMethod;

    private String requestUri;

    private String operName;

    private LocalDateTime createTime;
}
