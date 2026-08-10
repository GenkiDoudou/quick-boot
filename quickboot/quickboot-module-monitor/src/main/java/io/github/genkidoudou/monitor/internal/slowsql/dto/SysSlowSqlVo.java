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

    private Long slowId;

    private String sqlSource;

    @Schema(description = "SQL 操作类型：SELECT/INSERT/UPDATE/DELETE 等")
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
