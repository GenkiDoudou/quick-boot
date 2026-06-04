package io.github.genkidoudou.web.monitor.tracechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后端资源节点：HTTP（api）、操作日志、慢 SQL；子节点通过 parentApiId 挂到 api 行。
 */
@Data
@Schema(description = "后端链路节点")
public class TraceChainBackendNodeVo {

    private String id;
    /** api / oper_log / slow_sql */
    private String type;
    private String label;
    private Long startMs;
    private Long endMs;
    private String status;
    private String traceId;
    /** 父 api 节点 id（oper_log、slow_sql） */
    private String parentApiId;
    private Long operId;
    private Long slowId;
    private Integer httpStatus;
    private String requestMethod;
}
