package io.github.genkidoudou.web.system.exporttask.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Excel 导出任务，表 {@code sys_export_task}。
 */
@Data
@TableName("sys_export_task")
public class SysExportTask implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "task_id", type = IdType.ASSIGN_ID)
    private Long taskId;

    private String bizType;
    private String queryJson;
    private Long resultFileId;
    private String exportMode;
    private Integer syncMaxRows;
    private String status;
    private Integer totalRows;
    private Integer processedRows;
    private String errorMessage;
    private String createBy;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
