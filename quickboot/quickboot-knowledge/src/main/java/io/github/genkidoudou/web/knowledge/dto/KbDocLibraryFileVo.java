package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档库文件列表出参。
 */
@Data
@Schema(description = "文档库文件视图对象")
public class KbDocLibraryFileVo {

    @Schema(description = "文档库文件ID")
    private Long libFileId;

    @Schema(description = "所属目录ID")
    private Long folderId;

    @Schema(description = "关联 sys_file ID")
    private Long fileId;

    @Schema(description = "展示标题")
    private String title;

    @Schema(description = "扩展名")
    private String fileExt;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
