package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档上传成功后的同步响应。
 */
@Data
@Schema(description = "文档上传响应")
public class KbDocumentUploadVo {

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "入库任务ID")
    private Long taskId;

    @Schema(description = "关联文件ID")
    private Long fileId;

    @Schema(description = "展示标题")
    private String title;

    @Schema(description = "入库状态")
    private String docStatus;
}
