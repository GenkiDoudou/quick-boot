package io.github.genkidoudou.web.monitor.tracechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单页访问下的前端行为事件列表（来自 sys_client_track.events_json）。
 */
@Data
@Schema(description = "行为明细分页")
public class TraceChainBehaviorPageVo {

    private String pageVisitId;
    private String pagePath;
    private String menuName;
    private List<TraceChainBehaviorEventVo> events = new ArrayList<>();
}
