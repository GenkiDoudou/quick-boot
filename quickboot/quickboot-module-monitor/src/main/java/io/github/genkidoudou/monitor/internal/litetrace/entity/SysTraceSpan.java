package io.github.genkidoudou.monitor.internal.litetrace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 链路片段，表 {@code sys_trace_span}。
 */
@Data
@TableName("sys_trace_span")
public class SysTraceSpan implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** span 主键 */
    @TableId(value = "span_id", type = IdType.ASSIGN_ID)
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

    /** 创建时间 */
    private LocalDateTime createTime;
}
