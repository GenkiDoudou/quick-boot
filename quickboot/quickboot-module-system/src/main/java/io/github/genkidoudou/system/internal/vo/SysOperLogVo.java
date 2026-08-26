package io.github.genkidoudou.system.internal.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志分页 / 详情 / 导出模型（含查询条件字段）。
 */
@Data
@ExcelIgnoreUnannotated
public class SysOperLogVo {

  /** 操作日志主键。 */
  @ExcelProperty("日志编号")
  private Long operId;

  /** 系统模块 / 标题。 */
  @ExcelProperty("系统模块")
  private String title;

  /** 业务类型(sys_oper_business_type)。 */
  @ExcelProperty("业务类型")
  private Integer businessType;

  /** 方法名。 */
  @ExcelProperty("方法")
  private String method;

  /** HTTP 请求方式。 */
  @ExcelProperty("请求方式")
  private String requestMethod;

  /** 操作者类别(sys_oper_operator_type)。 */
  private Integer operatorType;

  /** 操作人员。 */
  @ExcelProperty("操作人员")
  private String operName;

  /** 部门名称。 */
  private String deptName;

  /** 请求 URL。 */
  @ExcelProperty("请求地址")
  private String operUrl;

  /** 操作 IP。 */
  @ExcelProperty("IP")
  private String operIp;

  /** 操作地点。 */
  private String operLocation;

  /** 请求参数。 */
  private String operParam;

  /** 响应结果。 */
  private String jsonResult;

  /** 0 正常，1 异常。 */
  @ExcelProperty("状态")
  private Integer status;

  /** 异常信息。 */
  private String errorMsg;

  /** 操作时间。 */
  @ExcelProperty("操作时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime operTime;

  /** 耗时（毫秒）。 */
  @ExcelProperty("耗时(ms)")
  private Long costTime;

  /** 链路追踪 ID。 */
  @ExcelProperty("链路ID")
  private String traceId;

  /** 前端操作 ID。 */
  @ExcelProperty("操作ID")
  private String clientOperationId;

  /** OAuth 客户端 ID。 */
  @ExcelProperty("客户端ID")
  private String clientId;

  /** 请求 User-Agent。 */
  @ExcelProperty("User-Agent")
  private String userAgent;

  /** 勾选导出 / 批量删除主键。 */
  private List<Long> ids;

  /** 操作时间起（yyyy-MM-dd）。 */
  private String beginTime;

  /** 操作时间止（yyyy-MM-dd）。 */
  private String endTime;

  /** 耗时下限（含），毫秒。 */
  private Long costTimeMin;

  /** 耗时上限（含），毫秒。 */
  private Long costTimeMax;
}
