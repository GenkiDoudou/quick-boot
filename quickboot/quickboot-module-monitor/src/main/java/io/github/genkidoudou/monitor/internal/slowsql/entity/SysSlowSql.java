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

    /** 主键 */
    @TableId(value = "slow_id", type = IdType.ASSIGN_ID)
    private Long slowId;

    /** {@link io.github.genkidoudou.common.monitor.slowsql.SlowSqlSource} 取值。 */
    private String sqlSource;

    /** {@link io.github.genkidoudou.common.monitor.slowsql.SlowSqlType} 取值。 */
    private String sqlType;

    /** MyBatis Mapper 标识 */
    private String mapperId;

    /** 完整 SQL 文本 */
    private String sqlText;

    /** 耗时毫秒 */
    private Long costTime;

    /** 链路标识 */
    private String traceId;

    /** 客户端操作 ID */
    private String clientOperationId;

    /** OAuth 客户端 ID */
    private String clientId;

    /** 触发请求的 HTTP 方法 */
    private String requestMethod;

    /** 触发请求 URI */
    private String requestUri;

    /** 操作人用户名 */
    private String operName;

    /** 记录创建时间 */
    private LocalDateTime createTime;
}
