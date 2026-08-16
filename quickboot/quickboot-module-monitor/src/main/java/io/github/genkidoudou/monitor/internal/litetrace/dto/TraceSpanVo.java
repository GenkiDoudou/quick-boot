package io.github.genkidoudou.monitor.internal.litetrace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 链路 span 片段视图对象。
 */
@Data
@Schema(description = "链路片段 VO")
public class TraceSpanVo {
    /** span 主键 */
    private Long spanId;
    /** 所属链路标识 */
    private String traceId;
    /** 父 span ID */
    private Long parentSpanId;
    /** 来源类型：fe_action / fe_api / sql / service 等 */
    private String sourceType;
    /** span 名称 */
    private String spanName;
    /** 服务/组件名称 */
    private String serviceName;
    /** 相对链路开始的偏移毫秒 */
    private Long startOffsetMs;
    /** 耗时毫秒 */
    private Long durationMs;
    /** 0=否 1=是 */
    private String okFlag;
    /** HTTP 状态码字符串 */
    private String statusCode;
    /** 扩展属性 JSON */
    private String attrsJson;
}
