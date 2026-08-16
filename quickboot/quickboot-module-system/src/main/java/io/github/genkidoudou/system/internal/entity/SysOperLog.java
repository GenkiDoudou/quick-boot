package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志，与表 {@code sys_oper_log} 对应（无 BaseEntity / delFlag）。
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 操作日志主键。 */
  @TableId(value = "oper_id", type = IdType.ASSIGN_ID)
  private Long operId;

  /** 系统模块 / 标题。 */
  private String title;

  /** 业务类型，与字典 {@code sys_oper_business_type} 数值一致。 */
  private Integer businessType;

  /** 方法名（短格式，如 {@code XxxController.method}）。 */
  private String method;

  /** HTTP 请求方式。 */
  private String requestMethod;

  /** 操作者类别，与字典 {@code sys_oper_operator_type} 一致。 */
  private Integer operatorType;

  /** 操作人员。 */
  private String operName;

  /** 部门名称。 */
  private String deptName;

  /** 请求 URL。 */
  private String operUrl;

  /** 操作 IP。 */
  private String operIp;

  /** 操作地点。 */
  private String operLocation;

  /** 请求参数（截断存储）。 */
  private String operParam;

  /** 响应结果（截断存储）。 */
  private String jsonResult;

  /** 0 正常，1 异常；与字典 {@code sys_oper_status} 一致。 */
  private Integer status;

  /** 异常信息。 */
  private String errorMsg;

  /** 操作时间。 */
  private LocalDateTime operTime;

  /** 耗时（毫秒）。 */
  private Long costTime;

  /** 与 {@link io.github.genkidoudou.common.api.TraceIds} / MDC 同源，可空。 */
  private String traceId;

  /** 前端一次用户操作 ID，可空。 */
  private String clientOperationId;

  /** OAuth 客户端 ID，可空。 */
  private String clientId;

  /** 请求 User-Agent，可空。 */
  private String userAgent;
}
