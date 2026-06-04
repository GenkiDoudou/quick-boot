package io.github.genkidoudou.web.system.importtask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 导入任务分页查询。
 */
@Data
@Schema(description = "导入任务查询")
public class ImportTaskQueryBo {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    @Schema(description = "业务编码")
    private String bizType;

    @Schema(description = "状态")
    private String status;
}
