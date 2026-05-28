package io.github.genkidoudou.web.system.notice.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 通知公告写操作入参。
 */
@Data
@Schema(description = "通知公告写操作入参")
public class SysNoticeBo {

    @NotNull(message = "公告ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "公告ID（修改必填）")
    private Long noticeId;

    @NotBlank(message = "公告标题不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 50, message = "公告标题长度不能超过50", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "公告标题")
    private String noticeTitle;

    @NotBlank(message = "公告类型不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Pattern(regexp = "^[12]$", message = "公告类型必须为1或2", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "公告类型：1通知 2公告")
    private String noticeType;

    @Pattern(regexp = "^[01]$", message = "状态必须为0或1", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "状态：0正常 1关闭")
    private String status;

    @Schema(description = "公告内容（HTML，服务端消毒）")
    private String noticeContent;
}
