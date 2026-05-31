package io.github.genkidoudou.web.system.operlog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 操作日志分页查询参数。
 */
@Data
@Schema(description = "操作日志列表查询")
public class SysOperLogQueryBo {

    @Min(1)
    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Min(1)
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "操作地址(URI)，模糊匹配")
    private String operUrl;

    @Schema(description = "系统模块/标题，模糊匹配")
    private String title;

    @Schema(description = "操作人员，模糊匹配")
    private String operName;

    @Schema(description = "业务类型，精确匹配（字典值字符串）")
    private String businessType;

    @Schema(description = "操作状态：0 正常 1 异常")
    private String status;

    @Schema(description = "操作时间起 yyyy-MM-dd")
    private String beginTime;

    @Schema(description = "操作时间止 yyyy-MM-dd")
    private String endTime;

    @Schema(description = "链路 traceId，精确匹配")
    private String traceId;

    @Schema(description = "前端操作 ID clientOperationId，精确匹配")
    private String clientOperationId;

    @Schema(description = "OAuth 客户端 ID clientId，精确匹配")
    private String clientId;

    @Schema(description = "排序列：operName / operTime / costTime")
    private String orderByColumn;

    @Schema(description = "是否升序")
    private Boolean isAsc;
}
