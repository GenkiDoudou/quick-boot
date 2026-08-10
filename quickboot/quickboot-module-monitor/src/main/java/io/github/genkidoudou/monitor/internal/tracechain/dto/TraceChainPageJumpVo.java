package io.github.genkidoudou.monitor.internal.tracechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 页面跳转边（相邻 pageVisit 之间）。
 */
@Data
@Schema(description = "页面跳转")
public class TraceChainPageJumpVo {

    private Integer step;
    private String fromLabel;
    private String fromPath;
    private String toLabel;
    private String toPath;
    private String jumpLabel;
    private String pageVisitId;
    private Long atMs;
    private String atLabel;
}
