package io.github.genkidoudou.monitor.internal.clienttrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 行为轨迹中的操作批次节点（页面访问批或按钮操作批）。
 */
@Data
@Schema(description = "监控操作批次节点")
public class ClientTrackActionNodeVo {

    @Schema(description = "批次 ID")
    private Long batchId;

    @Schema(description = "operationId")
    private String operationId;

    @Schema(description = "触发操作")
    private String triggerAction;

    @Schema(description = "上报原因")
    private String reason;

    @Schema(description = "是否页面访问批")
    private Boolean pageVisitBatch;

    @Schema(description = "入库时间")
    private LocalDateTime createTime;

    @Schema(description = "批次内事件")
    private List<ClientTrackEventItemVo> events = new ArrayList<>();
}
