package io.github.genkidoudou.web.system.exporttask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导出任务视图。
 */
@Data
@Schema(description = "导出任务")
public class ExportTaskVo {

    private Long taskId;
    private String bizType;
    /** 导出结果 xlsx 文件名（来自 sys_file.original_name）。 */
    @Schema(description = "导出文件名")
    private String fileName;
    private String exportMode;
    private String status;
    private Integer totalRows;
    private Integer processedRows;
    private Long resultFileId;
    private String errorMessage;
    private String createBy;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
