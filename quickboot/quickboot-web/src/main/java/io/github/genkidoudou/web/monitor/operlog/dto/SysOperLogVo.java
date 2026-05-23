package io.github.genkidoudou.web.monitor.operlog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志列表/详情展示模型。
 */
@Data
@Schema(description = "操作日志行")
public class SysOperLogVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long operId;
    private String title;
    private Integer businessType;
    private String method;
    private String requestMethod;
    private Integer operatorType;
    private String operName;
    private String deptName;
    private String operUrl;
    private String operIp;
    private String operLocation;
    private String operParam;
    private String jsonResult;
    /** 与字典 {@code sys_oper_status} 一致：0 正常 1 异常。 */
    private String status;
    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime operTime;
    private Long costTime;
    private String traceId;
}
