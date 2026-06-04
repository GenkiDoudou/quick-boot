package io.github.genkidoudou.web.system.exporttask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 导出任务分页查询。
 */
@Data
@Schema(description = "导出任务列表查询")
public class ExportTaskQueryBo {

    @Min(1)
    private Integer pageNum = 1;

    @Min(1)
    private Integer pageSize = 10;

    private String bizType;
    private String status;
}
