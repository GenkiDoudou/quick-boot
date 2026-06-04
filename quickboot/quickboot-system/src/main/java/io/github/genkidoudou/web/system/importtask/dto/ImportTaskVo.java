package io.github.genkidoudou.web.system.importtask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导入任务视图。
 */
@Data
@Schema(description = "导入任务")
public class ImportTaskVo {

    private Long taskId;
    private String bizType;
    /** 上传的原始 Excel 文件名（来自 sys_file.original_name）。 */
    @Schema(description = "原始文件名")
    private String fileName;
    private String importMode;
    private String status;
    private Integer totalRows;
    private Integer successRows;
    private Integer failRows;
    private Integer processedRows;
    private Long errorFileId;
    private String errorMessage;
    private String createBy;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
