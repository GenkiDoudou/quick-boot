package io.github.genkidoudou.web.system.importtask.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Excel 导入任务，表 {@code sys_import_task}。
 */
@Data
@TableName("sys_import_task")
public class SysImportTask implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "task_id", type = IdType.ASSIGN_ID)
    private Long taskId;

    private String bizType;
    private Long sourceFileId;
    private Long errorFileId;
    private String importMode;
    private Integer syncMaxRows;
    private String duplicateStrategy;
    /** 业务上下文 JSON（如字典数据导入的 dictType）。 */
    private String contextJson;
    private String status;
    private Integer totalRows;
    private Integer successRows;
    private Integer failRows;
    private Integer processedRows;
    private String errorMessage;
    private String createBy;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
