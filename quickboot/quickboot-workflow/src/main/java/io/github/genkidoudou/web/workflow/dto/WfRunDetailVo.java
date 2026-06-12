package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 运行详情出参（含步骤 Trace）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "运行详情出参")
public class WfRunDetailVo extends WfRunVo {

    @Schema(description = "运行入参")
    private Map<String, Object> inputs;

    @Schema(description = "运行出参")
    private Map<String, Object> outputs;

    @Schema(description = "失败原因")
    private String errorMsg;

    @Schema(description = "步骤 Trace")
    private List<WfRunStepVo> steps = new ArrayList<>();
}
