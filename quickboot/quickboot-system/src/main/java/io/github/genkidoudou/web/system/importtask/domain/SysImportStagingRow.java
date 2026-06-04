package io.github.genkidoudou.web.system.importtask.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Excel 导入异步暂存行，表 {@code sys_import_staging_row}。
 */
@Data
@TableName("sys_import_staging_row")
public class SysImportStagingRow implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long taskId;
    private Integer rowNo;
    private String rowJson;
    private String validateStatus;
    private String errorMsg;
    private String bizRef;
}
