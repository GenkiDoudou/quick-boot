package io.github.genkidoudou.web.system.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知公告列表/详情出参。
 */
@Data
@Schema(description = "通知公告视图对象")
public class SysNoticeVo {

    @Schema(description = "公告ID")
    private Long noticeId;

    @Schema(description = "公告标题")
    private String noticeTitle;

    @Schema(description = "公告类型")
    private String noticeType;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "公告内容（HTML）")
    private String noticeContent;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
