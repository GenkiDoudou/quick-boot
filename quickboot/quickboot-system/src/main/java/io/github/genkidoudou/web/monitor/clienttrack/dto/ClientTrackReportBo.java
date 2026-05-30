package io.github.genkidoudou.web.monitor.clienttrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 前端监控批次上报请求体（与 quick-ui {@code postTrackBatch} 对齐）。
 */
@Data
@Schema(description = "前端用户行为监控批次上报")
public class ClientTrackReportBo {

    @NotBlank(message = "reason 不能为空")
    @Size(max = 32, message = "reason 过长")
    @Schema(description = "上报触发原因：normal|error|leave|timer", example = "error")
    private String reason;

    @Size(max = 64, message = "operationId 过长")
    @Schema(description = "前端一次用户操作 ID，与 oper_log.client_operation_id 联查")
    private String operationId;

    @NotEmpty(message = "events 不能为空")
    @Size(max = 50, message = "单次上报事件数不能超过 50")
    @Schema(description = "事件数组，元素为 type/ts/page 等键值")
    private List<Map<String, Object>> events;
}
