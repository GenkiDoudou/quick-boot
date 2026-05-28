package io.github.genkidoudou.web.system.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 通知公告列表查询参数（与前端分页字段名一致）。
 */
@Data
@Schema(description = "通知公告列表查询参数")
public class SysNoticeQueryBo {

    @Min(1)
    @Schema(description = "页码，从 1 开始")
    private Integer pageNum = 1;

    @Min(1)
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "公告标题（模糊）")
    private String noticeTitle;

    @Schema(description = "公告类型（精确）")
    private String noticeType;

    @Schema(description = "创建人（模糊）")
    private String createBy;
}
