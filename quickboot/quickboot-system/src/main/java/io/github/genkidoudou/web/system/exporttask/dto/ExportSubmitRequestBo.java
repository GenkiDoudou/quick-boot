package io.github.genkidoudou.web.system.exporttask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 提交导出任务请求体。
 */
@Data
@Schema(description = "导出提交请求")
public class ExportSubmitRequestBo {

    @NotBlank
    @Schema(description = "业务编码，如 monitor:logininfor")
    private String bizType;

    @Schema(description = "筛选条件（与业务列表查询字段一致，驼峰）")
    private Map<String, Object> queryParams;

    @Schema(description = "sync 或 async；async 强制异步")
    private String mode;

    @Schema(description = "覆盖本次同步行数上限")
    private Integer syncMaxRows;
}
