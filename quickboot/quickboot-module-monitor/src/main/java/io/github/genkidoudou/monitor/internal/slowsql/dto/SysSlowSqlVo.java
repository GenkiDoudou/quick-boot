package io.github.genkidoudou.monitor.internal.slowsql.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 慢 SQL 列表/详情 VO。
 */
@Data
@Schema(description = "慢 SQL 记录")
public class SysSlowSqlVo {

    /** 主键 */
    private Long slowId;

    /** 来源：BUSINESS / JIMU / SYSTEM */
    private String sqlSource;

    @Schema(description = "SQL 操作类型：SELECT/INSERT/UPDATE/DELETE 等")
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
