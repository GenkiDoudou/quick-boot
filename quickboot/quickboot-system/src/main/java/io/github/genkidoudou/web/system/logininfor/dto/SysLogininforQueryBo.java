package io.github.genkidoudou.web.system.logininfor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 登录日志分页查询参数。
 */
@Data
@Schema(description = "登录日志列表查询")
public class SysLogininforQueryBo {

    @Min(1)
    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Min(1)
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "登录地址(IP)，模糊匹配")
    private String ipaddr;

    @Schema(description = "用户名，模糊匹配")
    private String userName;

    @Schema(description = "状态：0 成功 1 失败")
    private String status;

    @Schema(description = "访问时间起 yyyy-MM-dd")
    private String beginTime;

    @Schema(description = "访问时间止 yyyy-MM-dd")
    private String endTime;

    @Schema(description = "排序列：userName / loginTime")
    private String orderByColumn;

    @Schema(description = "是否升序")
    private Boolean isAsc;
}
