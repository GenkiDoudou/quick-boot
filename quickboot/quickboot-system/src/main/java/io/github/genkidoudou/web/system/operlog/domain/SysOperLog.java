package io.github.genkidoudou.web.system.operlog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志，与表 {@code sys_oper_log} 对应（字段语义对齐若依，含 {@code trace_id}）。
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "oper_id", type = IdType.ASSIGN_ID)
    private Long operId;

    /** 系统模块 / 标题。 */
    private String title;

    /** 业务类型，与字典 {@code sys_oper_business_type} 数值一致。 */
    private Integer businessType;

    /** 方法名（短格式，如 {@code XxxController.method}）。 */
    private String method;

    private String requestMethod;

    /** 操作者类别，与字典 {@code sys_oper_operator_type} 一致。 */
    private Integer operatorType;

    private String operName;

    private String deptName;

    private String operUrl;

    private String operIp;

    private String operLocation;

    private String operParam;

    private String jsonResult;

    /** 0 正常，1 异常；与字典 {@code sys_oper_status} 一致。 */
    private Integer status;

    private String errorMsg;

    private LocalDateTime operTime;

    private Long costTime;

    /** 与 {@link io.github.genkidoudou.common.api.TraceIds} / MDC 同源，可空。 */
    private String traceId;

    /** 前端一次用户操作 ID，来自 {@code X-Client-Operation-Id}，可空。 */
    private String clientOperationId;

    /** OAuth 客户端 ID，来自 {@code X-Client-Id}，可空。 */
    private String clientId;
}
