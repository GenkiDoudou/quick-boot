package io.github.genkidoudou.web.system.importtask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 导入提交响应（同步或异步）。
 */
@Data
@Schema(description = "导入提交结果")
public class ImportSubmitResultVo {

    @Schema(description = "sync 或 async")
    private String mode;

    @Schema(description = "异步任务 ID")
    private Long taskId;

    @Schema(description = "总条数")
    private Long total;

    @Schema(description = "成功条数")
    private Long successCount;

    @Schema(description = "失败条数")
    private Long failCount;

    @Schema(description = "失败明细文件 ID")
    private Long errorFileId;
}
