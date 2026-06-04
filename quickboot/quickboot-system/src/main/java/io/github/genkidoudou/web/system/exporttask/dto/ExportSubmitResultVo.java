package io.github.genkidoudou.web.system.exporttask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 导出提交响应（同步或异步）。
 */
@Data
@Schema(description = "导出提交结果")
public class ExportSubmitResultVo {

    @Schema(description = "sync 或 async")
    private String mode;

    @Schema(description = "异步任务 ID")
    private Long taskId;

    @Schema(description = "异步完成后任务详情中的结果文件 ID；提交响应中为空")
    private Long resultFileId;

    @Schema(description = "导出行数")
    private Long totalRows;
}
